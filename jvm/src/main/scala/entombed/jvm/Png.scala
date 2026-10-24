package entombed.jvm

import entombed.gfx.{FrameBuffer, Palette}

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/** Writes frames as PNG files (debugging and documentation). */
object Png:
  def image(frame: FrameBuffer, palette: Palette, scale: Int = 1): BufferedImage =
    val argb = new Array[Int](FrameBuffer.Width * FrameBuffer.Height)
    frame.toArgb(palette, argb)
    val img = new BufferedImage(FrameBuffer.Width * scale, FrameBuffer.Height * scale,
      BufferedImage.TYPE_INT_RGB)
    for y <- 0 until FrameBuffer.Height * scale; x <- 0 until FrameBuffer.Width * scale do
      img.setRGB(x, y, argb((y / scale) * FrameBuffer.Width + x / scale))
    img

  def write(frame: FrameBuffer, palette: Palette, file: File, scale: Int = 2): Unit =
    file.getParentFile.mkdirs()
    ImageIO.write(image(frame, palette, scale), "png", file)
