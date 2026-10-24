package entombed.jvm

import entombed.core.{GameCore, Input}
import entombed.gfx.FrameBuffer
import entombed.oracle.OracleCore

import java.awt.event.{KeyAdapter, KeyEvent, WindowAdapter, WindowEvent}
import java.awt.image.{BufferedImage, DataBufferInt}
import java.awt.{Color, Dimension, Graphics, Graphics2D, RenderingHints}
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.LockSupport
import javax.swing.{JFrame, JPanel, SwingUtilities, WindowConstants}

/** Desktop version: Swing window, keyboard as joystick, javax.sound output.
  *
  * Keys: arrows = joystick, space or Ctrl = fire, other keys are typed as on the Atari
  * (P pause, 0-9 hourglass delay, S/L save/load the game, Q quit).
  */
object Main:
  @volatile private var joystick = 0
  private val typed = new ConcurrentLinkedQueue[Integer]()
  private val storage = new FileStorage()

  def main(args: Array[String]): Unit =
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
      frame.addWindowListener(new WindowAdapter:
        override def windowClosing(e: WindowEvent): Unit = audio.close()
      )
      frame.setVisible(true)
      panel.requestFocusInWindow()
    })
    gameLoop(panel, audio)

  private def newCore(): GameCore = new OracleCore(storage)

  private def gameLoop(panel: ScreenPanel, audio: JvmAudio): Unit =
    var core = newCore()
    val period = 20_000_000L // 50 Hz
    var next = System.nanoTime()
    while true do
      val key = Option(typed.poll()).map(_.intValue).getOrElse(0)
      core.step(Input(joystick, key))
      core.drainSounds(audio.play)
      panel.present(core.frame, core)
      if core.finished then core = newCore()
      next += period
      val now = System.nanoTime()
      if next - now > 0 then LockSupport.parkNanos(next - now)
      else if now - next > 5 * period then next = now // too late: do not try to catch up

  private val keys = new KeyAdapter:
    private def bit(e: KeyEvent): Int = e.getKeyCode match
      case KeyEvent.VK_UP | KeyEvent.VK_KP_UP | KeyEvent.VK_NUMPAD8       => Input.Up
      case KeyEvent.VK_DOWN | KeyEvent.VK_KP_DOWN | KeyEvent.VK_NUMPAD2   => Input.Down
      case KeyEvent.VK_LEFT | KeyEvent.VK_KP_LEFT | KeyEvent.VK_NUMPAD4   => Input.Left
      case KeyEvent.VK_RIGHT | KeyEvent.VK_KP_RIGHT | KeyEvent.VK_NUMPAD6 => Input.Right
      case KeyEvent.VK_SPACE | KeyEvent.VK_CONTROL | KeyEvent.VK_NUMPAD0  => Input.Fire
      case _                                                               => 0

    override def keyPressed(e: KeyEvent): Unit =
      joystick |= bit(e)
      e.getKeyCode match
        case KeyEvent.VK_BACK_SPACE => typed.add(8)
        case KeyEvent.VK_ENTER      => typed.add(13)
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
  setPreferredSize(new Dimension(FrameBuffer.Width * 3, FrameBuffer.Height * 3))
  setBackground(Color.BLACK)
  setFocusable(true)

  def present(frame: FrameBuffer, core: GameCore): Unit =
    val back = 1 - front
    val data = images(back).getRaster.getDataBuffer.asInstanceOf[DataBufferInt].getData
    frame.toArgb(core.palette, data)
    front = back
    repaint()

  override def paintComponent(g: Graphics): Unit =
    super.paintComponent(g)
    val g2 = g.asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
    val scale = math.max(1, math.min(getWidth / FrameBuffer.Width, getHeight / FrameBuffer.Height))
    val w = FrameBuffer.Width * scale
    val h = FrameBuffer.Height * scale
    g2.drawImage(images(front), (getWidth - w) / 2, (getHeight - h) / 2, w, h, null)
