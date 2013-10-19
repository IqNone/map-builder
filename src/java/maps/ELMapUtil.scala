package maps

import java.io._
import java.util.zip.GZIPInputStream
import scala.collection.mutable.ArrayBuffer

object ELMapUtil {
  def readFromZippedMap (is : InputStream, name : String) : ELMap = {
    var zip = new GZIPInputStream(is)

    val map = new ELMap()

    zip.skip(4)

    map.name = name
    map.width = readInt(zip)
    map.height = readInt(zip)

    val tileMapOffset: Int = readInt(zip)
    val highMapOffset: Int = readInt(zip)
    val obj3dLen: Int = readInt(zip)
    val obj3dNo: Int = readInt(zip)
    val obj3dOffset: Int = readInt(zip)
    val obj2dLen: Int = readInt(zip)
    val obj2dNo: Int = readInt(zip)
    val obj2dOffset: Int = readInt(zip)

    skip(zip, tileMapOffset - 44)
    map.tileMap = readMatrix(zip, map.width, map.height)

    skip(zip, highMapOffset - tileMapOffset - map.width * map.height)
    map.heightMap = readMatrix(zip, map.width * 6, map.height * 6)

    skip(zip, obj3dOffset - highMapOffset - map.width * map.height * 6 * 6)
    map.attributes = read3DObjects(zip, obj3dNo, obj3dLen)

    map
  }

  def fromInputStream(is: InputStream, elMap : ELMap) : AndroidMap = {
    var in : ObjectInputStream = new ObjectInputStream(is)

    new AndroidMap {
      val b: Array[Byte] = new Array[Byte](in.readInt)
      in.readFully(b)
      name = new String(b)

      width = in.readInt
      height = in.readInt

      heightMap = new Array[Array[Byte]](height)
      for(i <- 0 to height - 1){
        heightMap(i) = new Array[Byte](width)
        in.readFully(heightMap(i))
      }

      val entrablesCount: Int = in.readInt
      entrables = new ArrayBuffer[Attribute](entrablesCount)

      for(i <- 0 to entrablesCount - 1) {
        var id = in.readInt
        val attribute: Attribute = elMap.attributes.find(a => a.id == id).get
        entrables += attribute
        attribute.x = in.readInt
        attribute.y = in.readInt
      }

      val harvestablesCount: Int = in.readInt
      harvestables = new ArrayBuffer[Attribute](harvestablesCount)
      for(i <- 0 to harvestablesCount - 1) {
        var id = in.readInt
        val attribute: Attribute = elMap.attributes.find(a => a.id == id).get
        harvestables += attribute
        attribute.x = in.readInt
        attribute.y = in.readInt
        attribute.imgId = in.readInt
      }

      val portalsCount: Int = in.readInt
      portals = new ArrayBuffer[Attribute](portalsCount)
      for(i <- 0 to portalsCount - 1) {
        var id = in.readInt
        val attribute: Attribute = elMap.attributes.find(a => a.id == id).get
        portals += attribute
        attribute.x = in.readInt
        attribute.y = in.readInt
      }
    }
  }

  def fromInputStream(is: InputStream) : AndroidMap = {
    var in : ObjectInputStream = new ObjectInputStream(is)

    new AndroidMap {
      val b: Array[Byte] = new Array[Byte](in.readInt)
      in.readFully(b)
      name = new String(b)

      width = in.readInt
      height = in.readInt

      heightMap = new Array[Array[Byte]](height)
      for(i <- 0 to height - 1){
        heightMap(i) = new Array[Byte](width)
        in.readFully(heightMap(i))
      }

      val entrablesCount: Int = in.readInt
      entrables = new ArrayBuffer[Attribute](entrablesCount)

      for(i <- 0 to entrablesCount - 1) {
        entrables += new Attribute {
          var id: Int = in.readInt
          var x: Int = in.readInt
          var y: Int = in.readInt
          var imgId: Int = _
          var file: String = _
        }
      }

      val harvestablesCount: Int = in.readInt
      harvestables = new ArrayBuffer[Attribute](harvestablesCount)
      for(i <- 0 to harvestablesCount - 1) {
        harvestables += new Attribute {
          var id: Int = in.readInt
          var x: Int = in.readInt
          var y: Int = in.readInt
          var imgId: Int = in.readInt
          var file: String = _
        }
      }


      val portalsCount: Int = in.readInt
      portals = new ArrayBuffer[Attribute](portalsCount)
      for(i <- 0 to portalsCount - 1) {
        portals += new Attribute {
          var id: Int = in.readInt
          var x: Int = in.readInt
          var y: Int = in.readInt
          var imgId: Int = _
          var file: String = _
        }
      }
    }
  }

  def fromElMap(elMap : ELMap) : AndroidMap = {
    new AndroidMap {
      name = elMap.name

      width = elMap.width * 6
      height = elMap.height * 6

      heightMap = elMap.heightMap

      harvestables = elMap.attributes.filter(a => Harvestables.isHarvestable(a)).toBuffer
      entrables = elMap.attributes.filter(a => Entrables.isEntrable(a) || a.id == 385 || a.id == 8).toBuffer
      portals = elMap.attributes.filter(a => a.file == "portal1.e3d").toBuffer

      harvestables.foreach(a => a.imgId = Harvestables.imageId(a.file))
    }
  }

  def save(map: AndroidMap, filename: String) {
    var out :ObjectOutputStream = new ObjectOutputStream(new FileOutputStream(filename + ".elma"))
    out.writeInt(map.name.length)
    out.write(map.name.getBytes("UTF-8"))
    out.writeInt(map.width)
    out.writeInt(map.height)
    for(row <- map.heightMap) {
       out.write(row)
    }
    out.writeInt(map.entrables.size)
    for(entrable <- map.entrables) {
      out.writeInt(entrable.id)
      out.writeInt(entrable.x)
      out.writeInt(entrable.y)
    }
    out.writeInt(map.harvestables.size)
    for(harvestable <- map.harvestables) {
      out.writeInt(harvestable.id)
      out.writeInt(harvestable.x)
      out.writeInt(harvestable.y)
      out.writeInt(harvestable.imgId)
    }
    out.writeInt(map.portals.size)
    for(portal <- map.portals) {
      out.writeInt(portal.id)
      out.writeInt(portal.x)
      out.writeInt(portal.y)
    }
    out.close()
  }

  private def skip(is: InputStream, n: Int) {
    val skip: Long = is.skip(n)
    if (skip != n) {
      throw new RuntimeException("not enough data left")
    }
  }

  private def readInt(is: InputStream): Int = {
    val bytes: Array[Byte] = new Array[Byte](4)
    val read: Int = is.read(bytes, 0, 4)

    if (read < 4) {
      throw new RuntimeException("not enough data left")
    }

    bytes(0) & 0xff |
    (bytes(1) & 0xff) << 8 |
    (bytes(2) & 0xff) << 16 |
    (bytes(3) & 0xff) << 24
  }

  private def readMatrix(is: InputStream, width: Int, height: Int): Array[Array[Byte]] = {
    val m: Array[Array[Byte]] = new Array[Array[Byte]](height)

    for(row <- 0 to height - 1){
        m(row) = new Array[Byte](width)
        readAll(is, m(row))
    }

    m
  }

  private def read3DObjects(is: InputStream, obj3dNo: Int, obj3dLen: Int) = {
    val attributes : Array[Attribute] = new Array[Attribute](obj3dNo)

    val buffer: Array[Byte] = new Array[Byte](obj3dLen)

    for(i <- 0 to obj3dNo - 1) {
      readAll(is, buffer)
      val file: String = getName(buffer)
      attributes(i) = createMapObject(buffer, i, file)
    }

    attributes
  }

  private def createMapObject(buffer: Array[Byte], objId: Int, fileName: String): Attribute = {
    new Attribute {
      var id = objId
      var imgId = 0
      var x = 2 * nextFloat(buffer, 80).asInstanceOf[Int]
      var y = 2 * nextFloat(buffer, 84).asInstanceOf[Int]
      var file = fileName
    }
  }

  private def getName(buffer: Array[Byte]): String = {
    val start: Int = findLast(buffer, '/'.asInstanceOf[Byte], 0, 79)
    val end: Int = findFirst(buffer, 0.asInstanceOf[Byte], 0, 79)
    new String(buffer, start + 1, end - start - 1)
  }

  private def findFirst(buffer: Array[Byte], value: Byte, start: Int, end: Int): Int = {
    for(i <-  start to end) {
      if (buffer(i) == value) {
        return i
      }
    }

    -1
  }

  private def findLast(buffer: Array[Byte], value: Byte, start: Int, end: Int): Int = {
    for (i <- end to start by -1) {
      if (buffer(i) == value) {
        return i
      }
    }

    -1
  }

  private def readAll(is: InputStream, dest: Array[Byte]) {
    var left: Int = dest.length
    while (left > 0) {
      val read: Int = is.read(dest, dest.length - left, left)
      if (read == -1) {
        throw new RuntimeException("not enough data left")
      }
      left -= read
    }
  }

  private def nextFloat(bytes: Array[Byte], start: Int): Float = {
    java.lang.Float.intBitsToFloat(
        bytes(start) & 0xff |
        (bytes(start + 1) & 0xff) << 8 |
        (bytes(start + 2) & 0xff) << 16 |
        (bytes(start + 3) & 0xff) << 24)
  }
}
