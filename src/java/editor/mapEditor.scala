package editor

import maps._
import scala.swing._
import scala.swing.event.Event

object mapEditor extends BorderPanel{
  var androidMap : AndroidMap = null
  var elMap : ELMap = null

  add(toolbar, BorderPanel.Position.West)
  add(workspace, BorderPanel.Position.Center)

  listenTo(toolbar, mapView)
  reactions += {
    case SelectionChanged(`mapView`, a) => toolbar.selected = a
    case SelectionChanged(`toolbar`, a) => mapView.selected = a
  }


  def load(elMap : ELMap, androidMap : AndroidMap, img : Image) {
    this.androidMap = androidMap
    this.elMap = elMap

    toolbar.refresh(elMap, androidMap)
    workspace.refresh(elMap, androidMap, img)
  }

  object workspace extends ScrollPane {
    contents = mapView

    def refresh(elMap: ELMap, androidMap: AndroidMap, image: Image) {
      mapView.setUp(elMap, androidMap, image)
    }
  }

  case class SelectionChanged(component: Component, a: Attribute) extends Event {
    val attribute = a
  }

  case class SelectionDeleted(a : Attribute) extends Event {
    val attribute = a
  }
}