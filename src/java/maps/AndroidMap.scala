package maps

import scala.Array
import scala.collection.mutable

class AndroidMap {
  var name = ""

  var width = 0
  var height = 0

  var heightMap : Array[Array[Byte]] = null

  var harvestables : mutable.Buffer[Attribute] = null
  var entrables : mutable.Buffer[Attribute] = null
}
