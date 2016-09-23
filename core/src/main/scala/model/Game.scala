package model

import engine.Point


class Game(level: Level) {

  private case class BlockState(position: Point, rotation: Int)

  private val blockStates = new Array[BlockState](level.blocks.length)

  for (i <- blockStates.indices) {
    blockStates(i) = BlockState(level.blocks(i).position, level.blocks(i).gridIndex)
  }

  def updateBlock(blockId: Int, status: Int, position: Point): Boolean = {
    blockStates(blockId) = BlockState(position, status)
    false // TODO Return, if the level is completed
  }

}
