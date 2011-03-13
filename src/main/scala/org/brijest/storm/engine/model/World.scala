/*    ______________  ___  __  ___  _____  _____  ____  __  ____________  *\
**   / __/_  __/ __ \/ _ \/  |/  / / __/ |/ / _ \/ __ \/ / / /_  __/ __/  **
**  _\ \  / / / /_/ / , _/ /|_/ / / _//    / , _/ /_/ / /_/ / / / / _/    **
** /___/ /_/  \____/_/|_/_/  /_/ /___/_/|_/_/|_|\____/\____/ /_/ /___/    **
**                                                                        **
**                                            Storm Enroute (c) 2011      **
\*                                            www.storm-enroute.com       */

package org.brijest.storm.engine
package model



import org.triggerspace._



trait World {
  def name: String
  def initializeArea(t: Simulators, id: AreaId)(implicit ctx: Ctx): Area
  def initialPosition(pid: PlayerId): AreaId
  def initialPlace(s: Simulators, pid: PlayerId, area: Area, id: EntityId)(implicit ctx: Ctx): Unit
}


object World {
  
  final class DefaultWorld extends World {
    def name = "D'Falta"
    def initializeArea(s: Simulators, id: AreaId)(implicit ctx: Ctx) = s.utils.defaultArea()
    def initialPosition(pid: PlayerId) = 0L
    def initialPlace(s: Simulators, pid: PlayerId, area: Area, id: EntityId)(implicit ctx: Ctx) {
      // find a location to place him in
      val pc = s.txtors.cstruct(PlayerCharacter(pid, id))
      val (w, h) = area.terrain.dimensions
      
      val it = Iterator.range(0, w).flatMap(x => Iterator.range(0, h).map(y => (x, y)))
      while (it.hasNext) {
        val (x, y) = it.next
        if (area.terrain(x, y).walkable) {
          area.characters.put(Pos(x, y), pc)
          return;
        }
      }
      error("could not place character " + area.id())
    }
  }
  
}