package editor

import scala.swing.{ListView, ScrollPane, Orientation, BoxPanel}
import java.awt.Dimension
import javax.swing.border.{LineBorder, EmptyBorder}
import maps._
import javax.swing.ImageIcon
import scala.swing.event.ButtonClicked

object toolbar extends BoxPanel(Orientation.Vertical){
  var androidMap : AndroidMap = null

  var elMap : ELMap = null
  var showEntrables = true

  var showHarvestables = true
  var showDeleted = false

  private var _selected : Attribute = null

  preferredSize = new Dimension(200, 200)
  border = new EmptyBorder(2, 2, 2, 2)

  def selected : Attribute = _selected
  def selected_=(a : Attribute) : Unit = {
    _selected = a
    objects.list.peer.setSelectedValue(a, true)
  }


  val objects = new ScrollPane(){
    val list = new ListView[Attribute]
    contents = list
  }

  val entrablesButton = new SelectButton() {
    icon = getIcon("entrables.png")
    selected = true
  }
  val harvestablesButton = new SelectButton() {
    icon = getIcon("harvestables.png")
    selected = true
  }
  val deletedButton = new SelectButton() {
    icon = getIcon("deleted.png")
    selected = false
  }

  val filters = new BoxPanel(Orientation.Horizontal) {
    contents += entrablesButton
    contents += harvestablesButton
    contents += deletedButton
  }

  val details = new BoxPanel(Orientation.Vertical) {
    preferredSize = new Dimension(200, 200)
    border = LineBorder.createBlackLineBorder
  }

  contents += objects
  contents += filters
  contents += details

  def getIcon(name : String) = new ImageIcon(getClass.getClassLoader.getResource("icons/" + name))

  def refresh(elMap: ELMap, androidMap: AndroidMap) {
    this.elMap = elMap
    this.androidMap = androidMap

    refreshObjects()
  }


  def refreshObjects() {
    if (elMap != null && androidMap != null) {
      objects.list.listData = elMap.attributes.filter(a =>
          showEntrables && androidMap.entrables.contains(a) || showEntrables && showDeleted && Entrables.isEntrable(a) ||
          showHarvestables && androidMap.harvestables.contains(a) || showHarvestables && showDeleted && Harvestables.isHarvestable(a) ||
          showDeleted && !androidMap.entrables.contains(a) && !androidMap.harvestables.contains(a))
    }
  }

  listenTo(entrablesButton)
  listenTo(harvestablesButton)
  listenTo(deletedButton)
  listenTo(objects.list.selection)

  reactions += {
    case ButtonClicked(`entrablesButton`) => {showEntrables = entrablesButton.selected; refreshObjects()}
    case ButtonClicked(`harvestablesButton`) => {showHarvestables = harvestablesButton.selected; refreshObjects()}
    case ButtonClicked(`deletedButton`) => {showDeleted = deletedButton.selected; refreshObjects()}
    case scala.swing.event.SelectionChanged(objects.list) => {
      var index = objects.list.selection.leadIndex
      var newSelection = objects.list.listData(index)
      if(newSelection != _selected) {
        publish(new editor.mapEditor.SelectionChanged(toolbar, newSelection))
        _selected = newSelection
      }
    }
  }
}
