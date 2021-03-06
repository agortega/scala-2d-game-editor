/*    ______________  ___  __  ___  _____  _____  ____  __  ____________  *\
**   / __/_  __/ __ \/ _ \/  |/  / / __/ |/ / _ \/ __ \/ / / /_  __/ __/  **
**  _\ \  / / / /_/ / , _/ /|_/ / / _//    / , _/ /_/ / /_/ / / / / _/    **
** /___/ /_/  \____/_/|_/_/  /_/ /___/_/|_/_/|_|\____/\____/ /_/ /___/    **
**                                                                        **
**                                            Storm Enroute (c) 2011      **
\*                                            www.storm-enroute.com       */

package org.brijest.storm.engine



import org.triggerspace._
import org.brijest.bufferz._
import model._



class ConsoleUI extends UI with DelegatedShell {
  
  def name = "Storm Enroute"
  
  var delegateShell: Shell = null
  
  var position = (0, 0)
  
  def updateScreen(lastActions: Iterator[(EntityId, Action)], area: AreaView) {
    // TODO
  }
  
  def getInputs(): Seq[Input] = inputs.synchronized {
    val res = inputs.toList
    inputs.clear()
    res
  }
  
  private val inputs = new collection.mutable.ArrayBuffer[Input]
  
  listen(e => {
    def mapButton(b: Mouze.Button) = b match {
      case Mouze.Left => Mouse.Left
      case Mouze.Right => Mouse.Right
      case Mouze.Middle => Mouse.Middle
    }
    
    val mapped = e match {
      case KeyPressed(c) => KeyPress(c)
      case MousePressed(x, y, button) => MouseClick(x, y, mapButton(button))
    }
    
    inputs.synchronized {
      inputs += mapped
    }
  })
  
}
