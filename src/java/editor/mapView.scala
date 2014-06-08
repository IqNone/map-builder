package editor

import scala.swing._
import maps.{Attribute, AndroidMap, ELMap}
import java.awt.{BasicStroke, Color}
import scala.swing.event.{KeyPressed, MousePressed, MouseWheelMoved, MouseDragged}
import javax.imageio.ImageIO
import editor.mapEditor.{SelectionChanged, SelectionDeleted}

object mapView extends Component {
  private var elMap : ELMap = null
  private var androidMap: AndroidMap = null
  private var image : Image = null

  private var _selected : Attribute = null

  private var width: Int = 0
  private var height: Int = 0

  private var zoom: Int = 5

  private val itemImages = loadItemImages()

  focusable = true

  def selected : Attribute = _selected
  def selected_=(a : Attribute) : Unit = {
    _selected = a
    repaint()
  }

  def setUp(elMap: ELMap, androidMap: AndroidMap, image: Image) {
    this.elMap = elMap
    this.androidMap = androidMap
    this.image = image

    this.zoom = 5

    setUpDimensions()
  }

  def setUpDimensions() {
    this.width = androidMap.width * zoom
    this.height = androidMap.height * zoom

    preferredSize = new Dimension(width, height)
    peer.setSize(width, height)

    repaint()
  }

  override def paint(g: Graphics2D) {
    super.paint(g)
    if(image == null || elMap == null || androidMap == null) {
      return
    }

    g.drawImage(image, 0, 0, width, height, null)
    g.setStroke(new BasicStroke(2))

    for(entrable <- androidMap.entrables) {
      drawEntrable(g, zoom * entrable.x - 2 * zoom, height - zoom * entrable.y - 3 * zoom, 5 * zoom)
    }

    for(harvestable <- androidMap.harvestables) {
      drawHarvestable(g, harvestable.imgId, zoom * harvestable.x, height - zoom * harvestable.y - zoom, zoom)
    }

//    g.setColor(Color.WHITE)
//
//    for(i <- 1 to androidMap.width - 1) {
//      g.drawLine(0, i * zoom, width, i * zoom)
//      g.drawLine(i * zoom, 0, i * zoom, height)
//    }

    drawSelection(g)
  }

  def drawEntrable(g: swing.Graphics2D, x: Int, y: Int, r: Int) {
    g.setColor(Color.LIGHT_GRAY)
    g.fillOval(x, y, r, r)
    g.setColor(Color.BLACK)
    g.drawOval(x, y, r, r)
    g.setColor(Color.DARK_GRAY)
    g.drawOval(x + 2, y + 2, r - 4, r - 4)
  }

  def drawHarvestable(g: swing.Graphics2D, imgId: Int, x: Int, y: Int, w: Int) {
    var img = itemImages(imgId / 25)
    var row = imgId % 25 / 5
    var col = imgId % 5

    g.drawImage(img,
      x, y, x + w, y + w,
      col * 51, row * 51, (col + 1) * 51, (row + 1) * 51,
      null
    )
  }

  def drawPortal(g: swing.Graphics2D, x: Int, y: Int, r: Int) {
    g.setColor(Color.BLUE)
    g.fillOval(x, y, r, r)
    g.setColor(Color.BLACK)
    g.drawOval(x, y, r, r)
    g.setColor(Color.DARK_GRAY)
    g.drawOval(x + 2, y + 2, r - 4, r - 4)
  }

  def drawSelection(g : swing.Graphics2D) {
    if(_selected == null) {
      return
    }

    g.setColor(Color.YELLOW)

    if(androidMap.entrables.contains(_selected)) {
      g.drawRect(zoom * _selected.x - 2 * zoom, height - zoom * _selected.y - 3 * zoom, 5 * zoom, 5 * zoom)
      return
    }

    if(androidMap.harvestables.contains(_selected)) {
      g.drawRect(zoom * _selected.x, height - zoom * _selected.y - zoom, zoom, zoom)
      return
    }

    //a deleted object
    g.drawRect(zoom * _selected.x - 2 * zoom, height - zoom * _selected.y - 3 * zoom, 5 * zoom, 5 * zoom)
  }

  listenTo(mouse.wheel, mouse.clicks, mouse.moves, keys)

  var start : Point = new Point

  reactions += {
    case MouseWheelMoved(_, _, _, rotation) => {zoom -= rotation; setUpDimensions()}
    case MousePressed(_, point, _, _, _) => {
      changeSelection(findAttribute(point.x / zoom, androidMap.height - 1 - point.y / zoom))
      start.x = point.x / zoom
      start.y = androidMap.height - 1 - point.y / zoom
      requestFocus()
    }
    case MouseDragged(_, now, _) => {
      moveSelection(start, new Point(now.x / zoom, androidMap.height - 1 - now.y / zoom))
      start.x = now.x / zoom
      start.y = androidMap.height - 1 - now.y / zoom
    }
    case KeyPressed(_,_, _, _) => deleteSelected()
  }

  def changeSelection(attribute: Attribute) {
    if(attribute != _selected) {
      _selected = attribute
      publish(new SelectionChanged(mapView, attribute))
      repaint()
    }
  }

  def findAttribute(x: Int, y: Int): Attribute = {
    if (_selected != null && androidMap.entrables.contains(_selected) && verifyCoords(_selected, x, y, 5)) {
      _selected
    }

    if (_selected != null && androidMap.harvestables.contains(_selected) && verifyCoords(_selected, x, y, 1)) {
      _selected
    }

    var found = androidMap.entrables.reverse.find(attribute => verifyCoords(attribute, x, y, 5))
    if(!found.isDefined) {
      found = androidMap.harvestables.reverse.find(attribute => verifyCoords(attribute, x, y, 1))
    }

    if (found.isDefined) found.get else null
  }

  def verifyCoords(a: Attribute, x: Int, y: Int, w: Int) : Boolean =
    a.x >= x - w / 2 && a.x <= x + w / 2 &&
      a.y >= y - w / 2 && a.y <= y + w / 2

  def moveSelection(start: Point, now : Point) {
    if(_selected != null) {
      _selected.x += now.x - start.x
      _selected.y += now.y - start.y
    }
    repaint()
  }

  def deleteSelected() {
    androidMap.entrables -= _selected
    androidMap.harvestables -= _selected
    publish(new SelectionDeleted(_selected))
    _selected = null
    repaint()
  }

  def loadItemImages() : Array[Image] = {
    val imgs = new Array[Image](27)

    for(i <- 1 to 27) {
      imgs(i - 1) = ImageIO.read(this.getClass.getClassLoader.getResource("items/items" + i + ".png"))
    }

    imgs
  }
}
