/*    ______________  ___  __  ___  _____  _____  ____  __  ____________  *\
**   / __/_  __/ __ \/ _ \/  |/  / / __/ |/ / _ \/ __ \/ / / /_  __/ __/  **
**  _\ \  / / / /_/ / , _/ /|_/ / / _//    / , _/ /_/ / /_/ / / / / _/    **
** /___/ /_/  \____/_/|_/_/  /_/ /___/_/|_/_/|_|\____/\____/ /_/ /___/    **
**                                                                        **
**                                            Storm Enroute (c) 2011      **
\*                                            www.storm-enroute.com       */

package org.brijest.storm.engine.model.components



import collection._



package immutable {
  trait Heap[+T] extends Iterable[T] {
    def size: Int
    def head: T
  }
}


class Heap[T: Ordering, Acc] extends immutable.Heap[T] {
  val pq = mutable.PriorityQueue[T]()
  
  @inline final override def head = pq.head
  @inline final override def size = pq.size
  @inline final def iterator = pq.iterator
  
  @inline final def enqueue(elem: T)(implicit acc: Acc) = pq.enqueue(elem)
  @inline final def dequeue()(implicit acc: Acc): T = pq.dequeue()
  @inline final def clear()(implicit acc: Acc) = pq.clear()
}

