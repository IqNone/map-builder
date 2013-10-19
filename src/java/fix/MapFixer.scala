package fix

import maps.{ELMap, ELMapUtil, Harvestables, AndroidMap}
import java.io.{FileInputStream, FileFilter, File}

object MapFixer {
  /**
   * implement this function with your code!!!
   */
  def fixMap(androidMap : AndroidMap, elMap : ELMap) {
    //fix harvestable icon
    androidMap.harvestables.foreach(a => a.imgId = Harvestables.imageId(a.file))
  }

  def fixAllInFolder(folder : String) {
    var file = new File(folder)
    if(!file.isDirectory) {
      return
    }

    def filter = new FileFilter {
      def accept(f: File): Boolean = f.isFile && f.getName.endsWith(".elma")
    }

    def applyFix(f: File) {
      var name = f.getName.replace(".elma", "")
      var elMap = ELMapUtil.readFromZippedMap(this.getClass.getClassLoader.getResourceAsStream("maps/" + name + ".elm.gz"), name)
      val androidMap = ELMapUtil.fromInputStream(new FileInputStream(f), elMap)
      fixMap(androidMap, elMap)
      ELMapUtil.save(androidMap, f.getAbsolutePath.replace(".elma", ""))
    }

    file.listFiles(filter).foreach(f => {
      try {
        applyFix(f)
      } catch {
        case e : Exception => e.printStackTrace()
      }
    })
  }
}
