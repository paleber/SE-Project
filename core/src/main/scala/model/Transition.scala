package model

/** Map the current State of a Block to the next State. */
case class Transition(rightRotation: Int,
                      leftRotation: Int,
                      mirrorVertical: Int,
                      mirrorHorizontal: Int)
