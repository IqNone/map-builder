package maps

abstract class Attribute {
  var id : Int
  var imgId : Int
  var x : Int
  var y : Int

  var file : String

  override def toString = "%5d    %s".format(id, file)
}