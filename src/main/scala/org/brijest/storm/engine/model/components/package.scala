/*    ______________  ___  __  ___  _____  _____  ____  __  ____________  *\
**   / __/_  __/ __ \/ _ \/  |/  / / __/ |/ / _ \/ __ \/ / / /_  __/ __/  **
**  _\ \  / / / /_/ / , _/ /|_/ / / _//    / , _/ /_/ / /_/ / / / / _/    **
** /___/ /_/  \____/_/|_/_/  /_/ /___/_/|_/_/|_|\____/\____/ /_/ /___/    **
**                                                                        **
**                                            Storm Enroute (c) 2011      **
\*                                            www.storm-enroute.com       */

package org.brijest.storm.engine.model



import collection._



package object components {
  
  def cell[T](v: T) = new Cell[T](v)
  def quad[T](w: Int, h: Int, default: (Int, Int) => Option[T], compress: Boolean) = new Quad(w, h, default, compress)
  def quad[T](w: Int, h: Int, d: Option[T], compress: Boolean = false): Quad[T] = quad(w, h, (x, y) => d, compress)
  def table[K, V] = new Table[K, V]
  def queue[T] = new Queue[T]
  def heap[T: Ordering] = new Heap[T]
  def set[T]: mutable.Set[T] = new mutable.HashSet[T]
  
}
