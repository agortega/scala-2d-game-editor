package org.brijest.storm
package engine



import scala.annotation.switch
import scala.collection._
import model.components._



package model {
  case class PlayerId(id: Long) extends Immutable
  
  trait ClassSet[C] {
    private val classes = mutable.LinkedHashMap[String, Class[C]]()
    
    def registered: Iterable[Class[C]] = classes.values
    
    def registeredNames: Iterable[String] = classes.keys
    
    def forName(s: String) = classes.get(s)
    
    def register[T <: C: Manifest] = {
      val cls = manifest[T].erasure.asInstanceOf[Class[C]]
      classes(cls.getName) = cls
    }
    
  }
}


package object model {
  
  def within(x: Int, y: Int, w: Int, h: Int) = x >= 0 && x < w && y >= 0 && y < h
  
  def within(x: Int, y: Int, dims: (Int, Int)): Boolean = within(x, y, dims._1, dims._2)
  
  /* area */
  
  type PlaneId = Int
  
  type AreaId = Long
  
  def invalidAreaId = -1L
  
  object areaId {
    def apply(plane: PlaneId, x: Int, y: Int) = {
      assert(x >= 0 && x < 0xffff)
      assert(y >= 0 && y < 0xffff)
      assert(plane >= 0 && plane < 0x3fffffff)
      (plane.toLong << 32) + (y << 16) + x
    }
    def unapply(id: Long): Option[(PlaneId, Int, Int)] =
      if ((0x80008000 & id) == 0) Some((planeId(id), (0xffff & id).toInt, (0xffff & (id >>> 16)).toInt))
      else None
  }
  
  object planeId {
    def apply(id: AreaId) = ((id & 0x3fffffff) >>> 32).toInt
  }
  
  /* entity */
  
  type EntityId = (AreaId, Long)
  
  def invalidEntityId = (-1L, -1L)
  
  /* player */
  
  def invalidPlayerId = PlayerId(-1L)
  
  def observerPlayerId = PlayerId(-2L)
  
  def defaultPlayerId = PlayerId(0)
  
  /* terrain */
  
  lazy val NoSlot = Slot[EmptySlot](0)
  
  /* various types */
  
  type Dir = Int
  
  object Dir {
    def north = 8
    def northwest = 7
    def northeast = 9
    def east = 6
    def west = 4
    def south = 2
    def southwest = 1
    def southeast = 3

    def fromTo(pos: Pos, dir: Dir): Pos = fromTo(pos.x, pos.y, dir)
    def fromTo(x: Int, y: Int, dir: Dir): Pos = (dir: @switch) match {
      case 8 => Pos(x, y - 1)
      case 7 => Pos(x - 1, y - 1)
      case 9 => Pos(x + 1, y - 1)
      case 4 => Pos(x - 1, y)
      case 6 => Pos(x + 1, y)
      case 2 => Pos(x, y + 1)
      case 1 => Pos(x - 1, y + 1)
      case 3 => Pos(x + 1, y + 1)
      case _ => illegalarg("Invalid direction " + dir)
    }
  }
  
  /* geometry */
  
  /** Traverses the elements in the rectangle diagonal-wise, northwest to southeast.
   */
  @inline def foreachNW2SE[U](x0: Int, y0: Int, w: Int, h: Int)(f: (Int, Int) => U) = if (w == h) {
    for (i <- 0 until h; x <- 0 to i; y = i - x) f(x0 + x, y0 + y)
    for (i <- 1 until h; x <- i until h; y = h - 1 + i - x) f(x0 + x, y0 + y)
  } else if (w > h) {
    for (i <- 0 until h; x <- 0 to i; y = i - x) f(x0 + x, y0 + y)
    for (i <- 1 until (w - h); x <- i until (i + h); y = h - 1 + i - x) f(x0 + x, y0 + y)
    for (i <- (w - h) until w; x <- i until w; y = h - 1 + i - x) f(x0 + x, y0 + y)
  } else {
    for (i <- 0 until w; x <- 0 to i; y = i - x) f(x0 + x, y0 + y)
    for (i <- w until (h - w); x <- 0 until w; y = w - 1 + i - x) f(x0 + x, y0 + y)
    for (i <- (h - w) until h; y <- i until h; x = w - 1 + i - y) f(x0 + x, y0 + y)
  }
  
}
