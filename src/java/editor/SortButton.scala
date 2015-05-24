package editor

import javax.imageio.ImageIO
import scala.swing.ToggleButton
import javax.swing.ImageIcon

class SortButton extends ToggleButton {
  val sortAZIcon = new ImageIcon(getClass.getClassLoader.getResource("icons/sort_AZ.png"))
  val sortZAIcon = new ImageIcon(getClass.getClassLoader.getResource("icons/sort_ZA.png"))
  val sort09Icon = new ImageIcon(getClass.getClassLoader.getResource("icons/sort_09.png"))
  val sort90Icon = new ImageIcon(getClass.getClassLoader.getResource("icons/sort_90.png"))

  var sortAscending : Boolean = true
  var sortNumeric : Boolean = true

  override def paint(g: _root_.scala.swing.Graphics2D) {
    if (sortNumeric) {
      if (sortAscending) {
        icon = sort09Icon
      } else {
        icon = sort90Icon
      }
    } else {
      if (sortAscending) {
        icon = sortAZIcon
      } else {
        icon = sortZAIcon
      }

    }

    super.paint(g)
  }

  def toggle() {
    if (sortNumeric) {
      if (sortAscending) {
        sortAscending = false
      } else {
        sortNumeric = false
        sortAscending = true
      }
    } else {
      if (sortAscending) {
        sortAscending = false
      } else {
        sortNumeric = true
        sortAscending = true
      }
    }
  }
}
