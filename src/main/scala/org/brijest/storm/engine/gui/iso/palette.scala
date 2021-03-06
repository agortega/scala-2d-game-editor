package org.brijest.storm.engine
package gui.iso



import org.apache.commons.io.IOUtils
import java.awt.color.ColorSpace
import java.awt.image._
import java.nio.ByteBuffer
import javax.media.opengl._
import org.brijest.storm.util._
import collection._
import model._



trait PaletteCanvas extends Canvas {
  
  trait Palette {
    
    trait SpriteOps {
      def img: Img = image(0)
      def image(frame: Int): Img
      def width: Int
      def height: Int
      def frames: Int
      def animated: Boolean
      def xoffset = 0
      def yoffset = 3
    }
    
    type Sprite >: Null <: SpriteOps

    def bufferedImage(name: String) = javax.imageio.ImageIO.read(pngStream(name))
    
    def newImageFromPngStream(stream: java.io.InputStream) = toImg(javax.imageio.ImageIO.read(stream))
    
    def newImage(name: String) = toImg(bufferedImage(name))
    
    def newSprite(imgs: Seq[Img]): Sprite
    
    def toImg(img: BufferedImage): Img
    
    def width(img: Img): Int
    
    def height(img: Img): Int
    
    def NullSprite: Sprite
    
    def findSprite(name: String): Sprite = {
      val pngimage = new com.sixlegs.png.AnimatedPngImage()
      val tmpfile = java.io.File.createTempFile("storm", "tmp")
      tmpfile.deleteOnExit()
      val pngis = pngStream(name)
      assert(pngis != null, "sprite " + name + " does not exist")
      val tmpfos = new java.io.FileOutputStream(tmpfile)
      try {
        IOUtils.copy(pngis, tmpfos)
      } finally {
        pngis.close()
        tmpfos.close()
      }
      val images = pngimage.readAllFrames(tmpfile)
      val s = newSprite(images.map(toImg(_)))
      
      s
    }
    
    def sprite(c: Character): Sprite = c match {
      case NoCharacter => NullSprite
      case c => findSprite(c.identifier)
    }
    
    def top(c: Character): Sprite = c match {
      case NoCharacter => NullSprite
      case c => findSprite(c.topIdentifier)
    }
    
    def sprite(e: Effect) = NullSprite
    
    def sprite(t: Slot) = findSprite(t.identifier)
    
    def wall(t: Slot) = findSprite(t.wallIdentifier)
    
    def walltop(t: Slot) = findSprite(t.topIdentifier)
    
    def edges(t: Slot) = findSprite(t.edgeIdentifier)
    
    def maxSpriteHeight = Sprites.maxheight
    
  }

  trait Caching extends Palette {
    val spritemap = mutable.Map[String, Sprite]()
    
    def addCache(name: String, c: Sprite) {
      if (!spritemap.contains(name)) {
        spritemap(name) = c
      }
    }
    
    def getCache(name: String): Sprite = if (spritemap.contains(name)) spritemap(name) else null
    
    abstract override def findSprite(name: String): Sprite = {
      val cached = getCache(name)
      if (cached != null) cached else {
        val s = super.findSprite(name)
        addCache(name, s)
        s
      }
    }

  }

  val palette: Palette
  
}


trait GLPaletteCanvas extends PaletteCanvas {
  case class Img()(val width: Int, val height: Int, val texWidth: Int, val texHeight: Int, val data: ByteBuffer) {
    private var stamp = -1L
    var texno = -1
    
    def cache(newstamp: Long, gl: GL2) = if (stamp != newstamp) {
      // TODO free resources
      gl.glGenTextures(1, texptr, 0)
      gl.glBindTexture(GL.GL_TEXTURE_2D, texptr(0))
      gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
      gl.glTexEnvf(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE)
      gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, data)
      stamp = newstamp
      texno = texptr(0)
    }
  }
  
  val texptr = new Array[Int](1)
  
  final class GLPalette extends Palette with Caching {
    
    class Sprite(val images: Seq[Img]) extends SpriteOps {
      def image(frame: Int) = images(frame)
      def animated = false
      def frames = images.length
      def width = images.head.width
      def height = images.head.height
    }
    
    def newSprite(imgs: Seq[Img]) = new Sprite(imgs)
    
    def toImg(img: BufferedImage) = {
      val iw = img.getWidth(null)
      val ih = img.getHeight(null)
      val w = ceilpow2(iw)
      val h = ceilpow2(ih)
      val raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, iw, ih, 4, null)
      val colorModel = new ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
      	Array(8, 8, 8, 8),
	      true,
	      false,
	      java.awt.Transparency.TRANSLUCENT,
	      DataBuffer.TYPE_BYTE)
      val nimg = new BufferedImage(colorModel, raster, false, null)
      
      nimg.createGraphics.drawImage(img, null, null)
      
      val data = ByteBuffer.wrap(raster.getDataBuffer.asInstanceOf[DataBufferByte].getData)
      
      data.position(0)
      data.mark()
      
      Img()(iw, ih, w, h, data)
    }
    
    def width(img: Img) = img.width
    
    def height(img: Img) = img.height
    
    object NullSprite extends Sprite(null) {
      override def width = 0
      override def height = 0
    }
    
  }

}


trait SwingPaletteCanvas extends PaletteCanvas {

  type Img = BufferedImage

  class DefaultSwingPalette extends Palette with Caching {

    /* types */

    class Sprite(val images: Seq[Img]) extends SpriteOps {
      def image(frame: Int) = images(frame)
      def animated = false
      def frames = images.length
      def width = images.head.getWidth
      def height = images.head.getHeight
    }

    object NullSprite extends Sprite(null) {
      override def width = 0
      override def height = 0
    }

    def newSprite(imgs: Seq[Img]) = new Sprite(imgs)

    def toImg(img: BufferedImage) = img

    def width(img: Img) = img.getWidth(null)

    def height(img: Img) = img.getHeight(null)

  }

}