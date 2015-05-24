import com.sun.java.swing.plaf.windows.WindowsLookAndFeel
import editor.mapEditor
import fix.MapFixer
import java.awt.Color
import java.io.{FileInputStream, File}
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.border.{LineBorder, EmptyBorder}
import javax.swing.{ImageIcon, Icon, UIManager}
import maps.ELMapUtil
import scala.swing._
import scala.swing.event.{Event, SelectionChanged, Key}
import scala.swing.FileChooser.SelectionMode
import scala.swing.GridBagPanel.Fill

object Main extends SimpleSwingApplication{
  //UIManager.setLookAndFeel(new WindowsLookAndFeel)
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

  def top = new MainFrame{
    title = "Eternal Lands Map Builder for Android"
    size = new Dimension(800, 600)
    preferredSize = new Dimension(800, 600)
    minimumSize = new Dimension(800 ,600)

    menuBar = new MenuBar{
      val file = new Menu("File") {
        val newProject = new MenuItem(Action("New Project") {
          mnemonic = Key.N
          newProjectDialog.visible = true
        })
        val openProject = new MenuItem(Action("Open Project") {
          mnemonic = Key.O
          openProjectDialog.visible = true
        })
        val fixMaps = new MenuItem(Action("Apply fix to all") {
          mnemonic = Key.A
          applyFixDialog.visible = true
        })
        val save = new MenuItem(Action("Save") {
          mnemonic = Key.S
          ELMapUtil.save(mapEditor.androidMap, mapEditor.androidMap.name)
        })
        val saveAs = new MenuItem("Save As...") {

        }
        val exit = new MenuItem(Action("Exit") {
          mnemonic = Key.X
          System.exit(0)
        })

        contents += newProject
        contents += openProject
        contents += new Separator()
        contents += fixMaps
        contents += new Separator()
        contents += save
        contents += saveAs
        contents += new Separator()
        contents += exit

        mnemonic = Key.F
      }
      contents += file
    }

    contents = mapEditor
  }

  object newProjectDialog extends Dialog{
    title = "Create new project"
    modal = true
    size = new Dimension(480, 320)
    minimumSize = new Dimension(480 , 320)
    maximumSize = new Dimension(480 , 320)

    contents = new BoxPanel(Orientation.Vertical){
      border = new EmptyBorder(5, 5, 5, 5)

      val standard = new CheckBox("Standard map")
      val custom = new CheckBox("Custom Map")
      val group = new ButtonGroup
      group.buttons += standard
      group.buttons += custom
      group.select(standard)

      val standardList = new ScrollPane(){
        val list = new ListView[String](standardMapsNames)
        contents = list
        preferredSize = new Dimension(360, 70)
      }

      val preview = new Label(){
        preferredSize = new Dimension(100, 100)
        border = new LineBorder(Color.gray, 1, false)
        listenTo(standardList.list.selection)
        reactions += {
          case SelectionChanged(standardList.list) => icon = loadIcon(standardList.list.selection.items(0).toString)
        }
      }

      val standardOptionsPanel = new BorderPanel() {
        preferredSize = new Dimension(470, 70)
        add(standardList, BorderPanel.Position.West)
        add(preview, BorderPanel.Position.East)
      }

      val fileChoosers: FileChooserText = new FileChooserText(Array("map (.elm.gz)", "image (optional)"))

      val okButton = new Button(Action("Ok"){setUp(); close()})
      val cancelButton = new Button(Action("Cancel"){close()})
      val buttons: BoxPanel = new BoxPanel(Orientation.Horizontal) {
        contents += okButton
        contents += cancelButton
      }

      contents += new BorderPanel(){add(standard, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(standardOptionsPanel, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(custom, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(fileChoosers, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(buttons, BorderPanel.Position.East)}

      def setUp() {
        if(standard.selected) {
          var name = standardList.list.selection.items(0).toString
          var elMap = ELMapUtil.readFromZippedMap(this.getClass.getClassLoader.getResourceAsStream("maps/" + name + ".elm.gz"), name)
          var androidMap = ELMapUtil.fromElMap(elMap)
          var image = ImageIO.read(getClass.getClassLoader.getResource("images/" + name + ".png"))
          mapEditor.load(elMap, androidMap, image)
        }
      }
    }
  }

  object openProjectDialog extends Dialog{
    title = "Open project"
    modal = true
    size = new Dimension(480, 320)
    minimumSize = new Dimension(480 , 320)
    maximumSize = new Dimension(480 , 320)

    contents = new BoxPanel(Orientation.Vertical){
      border = new EmptyBorder(5, 5, 5, 5)

      val fileChoosers: FileChooserText = new FileChooserText(Array(".elma"))
      var selectedMap: Label = new Label(){
        border = LineBorder.createBlackLineBorder()
      }

      val standard = new CheckBox("Standard map")
      val custom = new CheckBox("Custom Map")
      val group = new ButtonGroup
      group.buttons += standard
      group.buttons += custom
      group.select(standard)

      val customMapChoosers: FileChooserText = new FileChooserText(Array("map (.elm.gz)", "image (optional)"))

      val okButton = new Button(Action("Ok"){close(); openProject()})
      val cancelButton = new Button(Action("Cancel"){close()})
      val buttons: BoxPanel = new BoxPanel(Orientation.Horizontal) {
        contents += okButton
        contents += cancelButton
      }

      listenTo(fileChoosers)
      reactions += {
        case FileSelectionChanged(0, text) => {
          var selected = new File(text).getName.replace(".elma", "")
          var standard = this.getClass.getClassLoader.getResource("maps/" + selected + ".elm.gz")
          if(standard != null) {
            selectedMap.text = selected
          } else {
            selectedMap.text = "Not a known map"
          }
        }
      }

      contents += new BorderPanel(){add(fileChoosers, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(standard, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(selectedMap, BorderPanel.Position.Center)}
      contents += new BorderPanel(){add(custom, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(customMapChoosers, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(buttons, BorderPanel.Position.East)}

      def openProject() : Unit = {
        if(standard.selected) {
          var file = new File(fileChoosers.selected(0))
          var name = file.getName.replace(".elma", "")
          var elMap = ELMapUtil.readFromZippedMap(this.getClass.getClassLoader.getResourceAsStream("maps/" + name + ".elm.gz"), name)
          var androidMap = ELMapUtil.fromInputStream(new FileInputStream(file), elMap)
          var image = ImageIO.read(getClass.getClassLoader.getResource("images/" + name + ".png"))
          mapEditor.load(elMap, androidMap, image)
        }
      }
    }
  }

  object applyFixDialog extends Dialog{
    title = "Apply fix to all maps from directory"
    modal = true
    size = new Dimension(480, 320)
    minimumSize = new Dimension(480 , 320)
    maximumSize = new Dimension(480 , 320)

    contents = new BoxPanel(Orientation.Vertical){
      border = new EmptyBorder(5, 5, 5, 5)

      val textField: swing.TextField = new TextField()
      val button: swing.Button = new Button(Action("..."){
        val chooser = new FileChooser(new File("."))
        chooser.title = "Select Folder"
        chooser.fileSelectionMode = SelectionMode.DirectoriesOnly
        val result = chooser.showOpenDialog(null)
        if (result == FileChooser.Result.Approve) {
          textField.text = chooser.selectedFile.getAbsolutePath
        }
      })

      val selection = new GridBagPanel() {
        preferredSize = new Dimension(470, 30)
        add(textField,  new Constraints(){
          grid = (0, 0)
          weightx = 1
          fill = Fill.Horizontal
        })
        add(button, pair2Constraints((2, 0)))
      }

      val okButton = new Button(Action("Ok"){MapFixer.fixAllInFolder(textField.text); close()})
      val cancelButton = new Button(Action("Cancel"){close()})
      val buttons: BoxPanel = new BoxPanel(Orientation.Horizontal) {
        contents += okButton
        contents += cancelButton
      }

      contents += new BorderPanel(){add(new Label("Select folder"), BorderPanel.Position.West)}
      contents += new BorderPanel(){add(selection, BorderPanel.Position.West)}
      contents += new BorderPanel(){add(buttons, BorderPanel.Position.East)}
    }
  }

  def standardMapsNames : Array[String] = {
    new File(this.getClass.getResource("maps").toURI)
      .list()
      .filter(f => f.endsWith("gz"))
      .map(f => f.replace(".elm.gz", ""))
  }

  def loadIcon(map : String) : Icon = {
    val url: URL = this.getClass.getResource("thumbs/" + map + ".png")
    if (url == null) null else new ImageIcon(url)
  }

  case class FileSelectionChanged(index: Int, text:String) extends Event

  class FileChooserText(labels : Array[String]) extends GridBagPanel(){
    preferredSize = new Dimension(470, 50)
    val selections : Array[String] = new Array[String](labels.length)

    for(i <- 0 to labels.length - 1) {
      val textField: swing.TextField = new TextField()
      val button: swing.Button = new Button(Action("..."){
        val chooser = new FileChooser(new File("."))
        chooser.title = "Select File"
        val result = chooser.showOpenDialog(null)
        if (result == FileChooser.Result.Approve) {
          textField.text = chooser.selectedFile.getAbsolutePath
          selections(i) = textField.text
          publish(new FileSelectionChanged(i, textField.text))
        }
      })

      add(new Label(labels(i)), labelConstraint(i))
      add(textField, textConstraint(i))
      add(button, pair2Constraints((2, i)))
    }

    def selected(index : Int) = selections(index)

    def labelConstraint(row : Int) = pair2Constraints((0, row))
    def textConstraint(row: Int) : Constraints = {
      val c = new Constraints()
      c.grid = (1, row)
      c.weightx = 1
      c.fill = Fill.Horizontal
      c
    }
  }
}
