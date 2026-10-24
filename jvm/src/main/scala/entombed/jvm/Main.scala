package entombed.jvm

import entombed.core.{GameCore, Input}
import entombed.driver.GameDriver
import entombed.gfx.FrameBuffer
import entombed.oracle.OracleCore
import entombed.save.Storage

import java.awt.event.{KeyAdapter, KeyEvent, WindowAdapter, WindowEvent}
import java.awt.image.{BufferedImage, DataBufferInt}
import java.awt.{Color, Dimension, Font, Graphics, Graphics2D, RenderingHints}
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.LockSupport
import javax.swing.{JFrame, JPanel, SwingUtilities, WindowConstants}

/** Desktop version: Swing window, keyboard as joystick, javax.sound output.
  *
  * Keys: arrows = joystick, space or Ctrl = fire, other keys are typed as on the Atari (P pause,
  * 0-9 hourglass delay, S/L the 1995 save, Q quit). Added: F5 save, F9 load, F8 god mode, F4 unlock
  * the fifth tomb, F2 Atari ST speed or 50 frames per second, F12 switch between the Scala port and
  * the original code. Option `--oracle` starts with the original code.
  */
object Main:
  @volatile private var joystick = 0
  private val typed = new ConcurrentLinkedQueue[Integer]()
  private val commands = new ConcurrentLinkedQueue[GameDriver => Unit]()
  private val storage = new FileStorage()

  private val oracle: Storage => GameCore = s => new OracleCore(s)

  def main(args: Array[String]): Unit =
    val driver =
      new GameDriver(storage, if args.contains("--oracle") then oracle else GameDriver.port)
    val audio = new JvmAudio().start()
    val panel = new ScreenPanel
    SwingUtilities.invokeAndWait(() => {
      val frame = new JFrame("Entombed")
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      frame.add(panel)
      frame.pack()
      frame.setLocationRelativeTo(null)
      frame.addKeyListener(keys)
      panel.addKeyListener(keys)
      panel.setFocusTraversalKeysEnabled(false)
      frame.addWindowListener(new WindowAdapter:
        override def windowClosing(e: WindowEvent): Unit = audio.close())
      frame.setVisible(true)
      panel.requestFocusInWindow()
    })
    gameLoop(driver, panel, audio)

  private def gameLoop(driver: GameDriver, panel: ScreenPanel, audio: JvmAudio): Unit =
    val period = 20_000_000L // 50 Hz, the PAL vertical blank of the Atari ST
    var next = System.nanoTime()
    var isOracle = false
    while true do
      var c = commands.poll()
      while c != null do
        c(driver)
        c = commands.poll()
      if switchRequested then
        switchRequested = false
        isOracle = !isOracle
        driver.restartWith(
          if isOracle then oracle else GameDriver.port,
          if isOracle then "original 68000 code" else "Scala port"
        )
      var k = typed.poll()
      while k != null do
        driver.keyTyped(k.intValue)
        k = typed.poll()
      driver.step(joystick)
      driver.drainSounds(audio.play)
      panel.present(driver.frame, driver)
      next += period
      val now = System.nanoTime()
      if next - now > 0 then LockSupport.parkNanos(next - now)
      else if now - next > 5 * period then next = now // too late: do not try to catch up

  @volatile private var switchRequested = false

  private val keys = new KeyAdapter:
    private def bit(e: KeyEvent): Int = e.getKeyCode match
      case KeyEvent.VK_UP | KeyEvent.VK_KP_UP | KeyEvent.VK_NUMPAD8       => Input.Up
      case KeyEvent.VK_DOWN | KeyEvent.VK_KP_DOWN | KeyEvent.VK_NUMPAD2   => Input.Down
      case KeyEvent.VK_LEFT | KeyEvent.VK_KP_LEFT | KeyEvent.VK_NUMPAD4   => Input.Left
      case KeyEvent.VK_RIGHT | KeyEvent.VK_KP_RIGHT | KeyEvent.VK_NUMPAD6 => Input.Right
      case KeyEvent.VK_SPACE | KeyEvent.VK_CONTROL | KeyEvent.VK_NUMPAD0  => Input.Fire
      case _                                                              => 0

    override def keyPressed(e: KeyEvent): Unit =
      joystick |= bit(e)
      e.getKeyCode match
        case KeyEvent.VK_BACK_SPACE => typed.add(8)
        case KeyEvent.VK_ENTER      => typed.add(13)
        case KeyEvent.VK_F5         => commands.add(_.save())
        case KeyEvent.VK_F9         => commands.add(_.load())
        case KeyEvent.VK_F8         => commands.add(_.toggleGodMode())
        case KeyEvent.VK_F4         => commands.add(_.unlockTomb5())
        case KeyEvent.VK_F12        => switchRequested = true
        case KeyEvent.VK_F2         => commands.add(_.toggleSpeed())
        case _                      => ()

    override def keyReleased(e: KeyEvent): Unit = joystick &= ~bit(e)

    override def keyTyped(e: KeyEvent): Unit =
      val c = e.getKeyChar
      if c >= ' ' && c < 127 then typed.add(c.toInt)

/** Displays the 320x200 frame at the largest integer scale that fits. */
final class ScreenPanel extends JPanel:
  private val images = Array.fill(2)(
    new BufferedImage(FrameBuffer.Width, FrameBuffer.Height, BufferedImage.TYPE_INT_RGB)
  )
  @volatile private var front = 0
  @volatile private var message = ""
  @volatile private var god = false
  setPreferredSize(new Dimension(FrameBuffer.Width * 3, FrameBuffer.Height * 3))
  setBackground(Color.BLACK)
  setFocusable(true)

  def present(frame: FrameBuffer, driver: GameDriver): Unit =
    val back = 1 - front
    val data = images(back).getRaster.getDataBuffer.asInstanceOf[DataBufferInt].getData
    frame.toArgb(driver.palette, data)
    front = back
    message = if driver.messageTicks > 0 then driver.message else ""
    god = driver.godMode
    repaint()

  override def paintComponent(g: Graphics): Unit =
    super.paintComponent(g)
    val g2 = g.asInstanceOf[Graphics2D]
    g2.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
    )
    val scale = math.max(1, math.min(getWidth / FrameBuffer.Width, getHeight / FrameBuffer.Height))
    val w = FrameBuffer.Width * scale
    val h = FrameBuffer.Height * scale
    val x0 = (getWidth - w) / 2
    val y0 = (getHeight - h) / 2
    g2.drawImage(images(front), x0, y0, w, h, null)
    g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 6 * scale))
    g2.setColor(new Color(0xe0b050))
    if message.nonEmpty then g2.drawString(message, x0 + 4 * scale, y0 + 10 * scale)
    if god then g2.drawString("GOD", x0 + w - 24 * scale, y0 + 10 * scale)
