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
  trait Trie[+K, V]
}


@SerialVersionUID(1000L)
class Trie[K, V, Acc] extends immutable.Trie[K, V] {
  
  trait Node {
    def prefix: Seq[K]
    def children: Map[K, Node]
    def value: Option[V]
  }
  
  private case class Internal(val prefix: Seq[K]) extends Node {
    val children = mutable.Map[K, Internal]()
    var value: Option[V] = None
    override def toString = "Internal(" + prefix.mkString(", ") + "):" + children.mkString(", ")
  }
  
  private var root = new Internal(Nil)
  
  def tree: Node = root
  
  def update(key: Seq[K], value: V) {
    def insert(n: Internal, left: Seq[K]) {
      if (left.isEmpty) n.value = Some(value)
      else n.children.get(left.head) match {
        case Some(child) => insert(child, left.tail)
        case None =>
          n.children(left.head) = new Internal(n.prefix :+ left.head)
          insert(n.children(left.head), left.tail)
      }
    }
    insert(root, key)
  }
  
  def get(key: Seq[K]): Option[V] = {
    def find(n: Internal, left: Seq[K]): Option[V] = {
      if (left.isEmpty) n.value
      else n.children.get(left.head) match {
        case Some(child) => find(child, left.tail)
        case None => None
      }
    }
    find(root, key)
  }
  
  def apply(key: Seq[K]): V = get(key) match {
    case Some(v) => v
    case None => throw new IllegalArgumentException("no key: " + key)
  }
}
