package model

import engine.Point


class Game(level: Level) {

  private val blocks = level.blocks.toArray

  def updateBlock(blockId: Int, status: Int, position: Point): Boolean = {
    // blockStates(blockId) = BlockState(position, status) // TODO
    false // TODO Return, if the level is completed
  }

}
