package model

import engine.Point


case class BlockGrids(default: Grid,
                      rotationRight: Array[Grid],
                      rotationLeft: Array[Grid],
                      mirroringVertical: Array[Grid],
                      mirroringHorizontal: Array[Grid])


case class Block(grids: Array[BlockGrids],
                 curGrid: Int,
                 position: Point)
