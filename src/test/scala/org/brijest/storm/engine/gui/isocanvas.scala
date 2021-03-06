package org.brijest.storm.engine
package gui



import java.awt.Image
import java.awt.image.BufferedImage
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import iso._
import model._



class IsoCanvasTests extends WordSpec with ShouldMatchers {
  
  "IsoCanvas" should {
    
    class TestIsoCanvas extends IsoCanvas with SwingPaletteCanvas {
      val img = new BufferedImage(1280, 800, BufferedImage.TYPE_4BYTE_ABGR)
      
      class TestDrawingAdapter extends ImageDrawAdapter(img) with DrawAdapter
      
      drawing.background = false
      drawing.indices = false
      drawing.outline = true
      
      val palette = new DefaultSwingPalette
      def iwidth = img.getWidth
      def iheight = img.getHeight
    }
    
    def equalImages(a: BufferedImage, b: BufferedImage, diffname: String): Boolean = {
      val abuff = a.getData.getDataBuffer
      val braster = b.getData
      val bbuff = braster.getDataBuffer

      var same = true
      if (abuff.size != bbuff.size) return false else {
        var i = 0
        while (i < abuff.size) {
          if (abuff.getElem(i) != bbuff.getElem(i)) {
            same = false
            bbuff.setElem(i, 0xff000000)
          } else bbuff.setElem(i, 0x000000ff)
          i += 1
        }
      }
      
      b.setData(braster)
      save(diffname, b)
      same
    }
    
    def save(name: String, img: BufferedImage) {
      javax.imageio.ImageIO.write(img, "png", new java.io.File("tmp/" + name + ".png"))
    }
    
    def testAreaDisplay(area: AreaView, picname: String) {
      val canvas = new TestIsoCanvas
      
      canvas.redraw(area, IdleEngine, new canvas.TestDrawingAdapter())
      
      val result = canvas.img
      save(picname, result)
      val expected = canvas.palette.newImageFromPngStream(pngStream(picname))
      equalImages(result, expected, "diff-" + picname) should equal (true)
    }
    
    "correctly display area: empty dungeon test 1" in {
      testAreaDisplay(Area.emptyDungeonTest1(16, 16), "dungeon1")
    }

    "correctly display area: empty dungeon test 2" in {
      testAreaDisplay(Area.emptyDungeonTest2(16, 16), "dungeon2")
    }
    
    "correctly display area: empty dungeon test 3" in {
      testAreaDisplay(Area.emptyDungeonTest3(16, 16), "dungeon3")
    }
    
    "correctly display area: empty dungeon test 4" in {
      testAreaDisplay(Area.emptyDungeonTest4(16, 16), "dungeon4")
    }
  }
  
}








