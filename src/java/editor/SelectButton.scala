package editor

import javax.imageio.ImageIO
import scala.swing.ToggleButton

class SelectButton extends ToggleButton{
  val notSelectedIcon = ImageIO.read(getClass.getClassLoader.getResource("icons/not.png"))

  override def paint(g: _root_.scala.swing.Graphics2D) {
    super.paint(g)

    if(!selected) {
      var x = (peer.getWidth - notSelectedIcon.getWidth) / 2
      var y = (peer.getHeight - notSelectedIcon.getHeight) / 2
      g.drawImage(notSelectedIcon, x, y, null)
    }
  }
}
