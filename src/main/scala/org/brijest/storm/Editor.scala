/*    ______________  ___  __  ___  _____  _____  ____  __  ____________  *\
**   / __/_  __/ __ \/ _ \/  |/  / / __/ |/ / _ \/ __ \/ / / /_  __/ __/  **
**  _\ \  / / / /_/ / , _/ /|_/ / / _//    / , _/ /_/ / /_/ / / / / _/    **
** /___/ /_/  \____/_/|_/_/  /_/ /___/_/|_/_/|_|\____/\____/ /_/ /___/    **
**                                                                        **
**                                            Storm Enroute (c) 2011      **
\*                                            www.storm-enroute.com       */

package org.brijest.storm



import org.github.scopt._
import org.eclipse.swt._
import org.eclipse.swt.custom._
import org.eclipse.swt.widgets._
import org.eclipse.swt.events._
import java.awt.image._
import javax.media.opengl._
import engine.model._
import engine.gui.iso._



object Editor {
  
  def main(args: Array[String]) {
    val config = new Config
    val parser = new EditorConfigParser(config)
    
    Initializer.default()
    
    if (parser.parse(args)) {
      startEditor(config)
    }
  }
  
  def startEditor(config: Config) {
    new Editor(config)
  }
  
}


class EditorConfigParser(config: Config) extends DefaultParser(app.editorcommand) {
  help("h", "help", "Show this help message")
  opt("width", "The width for the area, if it's newly created", { v: String => config.area.width = v.toInt })
  opt("height", "The height for the area, if it's newly created", { v: String => config.area.height = v.toInt })
  arg("<areaname>", "The name of the area, creates one if it doesn't exist.", { v: String => config.area.name = v})
}


class Editor(config: Config) extends Logging {
  var glp: GLProfile = null //GLProfile.getDefault()
  var caps: GLCapabilities = null //new GLCapabilities(glp)
  
  concurrent.ops.future {
    glp = GLProfile.getDefault()
    caps = new GLCapabilities(glp)
  }
  
  def createGLIsoUI(area: Area) = {
    val areadisplay = new GLIsoUI(area, caps)
    
    /* events */
    
    var lastpress = (0, 0);
    var mode = 'none
    
    def onMiddleDrag(p: (Int, Int)) {
      areadisplay.pos = ((areadisplay.pos._1 + lastpress._1 - p._1).toInt, (areadisplay.pos._2 + lastpress._2 - p._2).toInt);
      lastpress = p
    }
    
    def onMiddlePress(p: (Int, Int)) {
      lastpress = p
      mode = 'drag
    }
    
    def onMiddleRelease(p: (Int, Int)) {
      mode = 'none
    }
    
    /* awt events */
    
    import java.awt.event._
    
    areadisplay.addMouseListener(new MouseAdapter {
      override def mousePressed(me: MouseEvent) {
        if (me.getButton == MouseEvent.BUTTON2) onMiddlePress((me.getX, me.getY))
      }
      override def mouseReleased(me: MouseEvent) {
        if (me.getButton == MouseEvent.BUTTON2) onMiddleRelease((me.getX, me.getY))
      }
    })
    
    areadisplay.addMouseMotionListener(new MouseMotionAdapter {
      override def mouseDragged(me: MouseEvent) {
        if (mode == 'drag) onMiddleDrag((me.getX, me.getY))
        areadisplay.repaint()
      }
    })
    
    areadisplay
  }
  
  val world = config.world match {
    case Some(name) => sys.error("unsupported")
    case None => new World.Default("Untitled")
  }
  
  val displ = Display.getDefault()
  val editorwindow = new editor.EditorWindow(displ)
  editorwindow.setVisible(true)
  
  displ.asyncExec(new Runnable {
    override def run() = initialize()
  })
  
  def initialize() {
    editorwindow.setImage(new graphics.Image(displ, pngStream("lightning")))
    
    import editorwindow._
    
    def loadPlaneTable() {
      planeTable.removeAll()
      for ((id, plane) <- world.planes.toSeq.sortBy(_._1)) {
        val item = new TableItem(planeTable, SWT.NONE)
        item.setText(0, id.toString)
        item.setText(1, plane.name)
        item.setText(2, plane.details)
      }
    }
    
    def loadWorldInfo() {
      worldNameLabel.setText(world.name)
      mainPlaneCombo.removeAll()
      
      val sortedplanes = world.planes.toSeq.sortBy(_._1)
      for ((id, plane) <- sortedplanes) {
        mainPlaneCombo.add(id + ": " + plane.name)
      }
      mainPlaneCombo.select(sortedplanes.indexWhere(_._1 == world.mainPlane))
      
      totalPlanesLabel.setText(sortedplanes.size.toString)
    }
    
    /* initialize */
    
    for (cls <- Terrain.registered) {
      val inst = cls.newInstance
      val tableItem = new TableItem(terrainTable, SWT.NONE);
      val image = new graphics.Image(displ, pngStream(inst.identifier));
      tableItem.setImage(0, image)
      tableItem.setText(1, cls.getSimpleName);
      tableItem.setText(2, inst.identifier);
    }
    
    worldNameLabel.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        world.name = worldNameLabel.getText
      }
    })
    
    mainPlaneCombo.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) {
        val sel = mainPlaneCombo.getSelectionIndex
        if (sel != -1) {
          world.mainPlane = sel
        } else loadWorldInfo()
      }
    })
    
    eventHandler = new editor.EditorEventHandler {
      def event(name: String, arg: Object): Unit = (name, arg) match {
        case ("Open area", e: SelectionEvent) =>
          val selection = planeTable.getSelection
          if (selection.nonEmpty) {
            val id = selection.head.getText(0).toInt
            val plane = world.plane(id)
            val chooser = new editor.XYChooser(editorwindow, SWT.APPLICATION_MODAL)
            chooser.width = plane.size - 1
            chooser.height = plane.size - 1
            val coord = chooser.open()
            if (coord == null) return
            
            val (x, y) = (coord.x, coord.y);
            val areaid = areaId(id, x, y)
            val area = world.area(areaid) match {
              case Some(a: AreaProvider.Strict) => a.acquire()
              case Some(a) =>
                val messageBox = new MessageBox(editorwindow, SWT.ICON_WARNING | SWT.OK)
                messageBox.setText("Unknown provider")
                messageBox.setMessage("Unable to handle area provider type: " + a.name + ".")
                messageBox.open()
                return
              case None =>
                val messageBox = new MessageBox(editorwindow, SWT.ICON_WARNING | SWT.OK)
                messageBox.setText("Unknown area")
                messageBox.setMessage("There is no area with id: " + areaid + " (plane: " + id + ", x: " + x + ", y: " + y + ").")
                messageBox.open()
                return
            }
            
            val tbtmMap = new CTabItem(leftTabs, SWT.CLOSE);
            tbtmMap.setText("Area: " + world.plane(id).get.name + " at (" + x + ", " + y + ")");
            
            areaPanel = new editor.AreaPanel(leftTabs, SWT.NONE)
            tbtmMap.setControl(areaPanel)
            
            val canvasPane = createGLIsoUI(area)
            
            areaPanel.areaCanvasPane.add(canvasPane)
          }
        case ("Add plane", e: SelectionEvent) =>
          val creator = new editor.PlaneCreatorDialog(editorwindow, SWT.APPLICATION_MODAL)
          val info = creator.open()
          if (info == null) return
          
          val plane = new world.DefaultPlane(info(0).toString, info(1).asInstanceOf[Int])
          val id = world.newPlaneId()
          world.planes(id) = plane
          
          for (x <- 0 until plane.size; y <- 0 until plane.size) {
            info(2) match {
              case "Strict" => world.areas(areaId(id, x, y)) = new AreaProvider.Strict(Area.emptyDungeon(32, 32))
            }
          }
          
          loadPlaneTable()
          loadWorldInfo()
      }
    }
    
    loadPlaneTable()
    loadWorldInfo()
  }
  
  try {
    editorwindow.open()
    editorwindow.layout()
    while (!editorwindow.isDisposed()) {
      if (!displ.readAndDispatch()) {
	displ.sleep()
      }
    }
  } catch {
    case e => logger.error("exception in event dispatch thread: " + e)
  }
  
}













