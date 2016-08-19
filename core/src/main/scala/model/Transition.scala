package model

/** Map the current State of a Block to the next State. */
case class Transition(rightRotation: Array[Int],
                      leftRotation: Array[Int],
                      mirrorVertical: Array[Int],
                      mirrorHorizontal: Array[Int])
