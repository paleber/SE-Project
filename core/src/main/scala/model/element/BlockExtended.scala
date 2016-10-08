package model.element

import model.basic.Point

case class BlockExtended(gridExt: GridExtended,
                         position: Point) {

  val block = Block(gridExt.grid, position)

}
