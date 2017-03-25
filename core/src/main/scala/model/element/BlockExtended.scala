package model.element

import model.basic.Point

case class BlockExtended(gridExt: AnchoredGrid,
                         position: Point) {

  val block = Block(gridExt.grid, position)

}
