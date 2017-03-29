package persistence

object LevelManagerOld {

  /*
  private implicit val formats = Serialization.formats(NoTypeHints)

  private case class MapItem(plan: Plan,
                             level: Option[Level] = None)

  private val levelMap = {
    val dirGrids = new File("core/src/main/resources/levels")
    val map = mutable.Map.empty[String, MapItem]
    for (file <- dirGrids.listFiles()) {
      val source = Source.fromFile(file).mkString
      val plan = read[Plan](source)
      val name = file.getName.replace(".json", "")
      GridManager.gridMap += ((name, GridManager.MapItem(plan.board)))
      for (b <- plan.variants.indices) {
        map.update(name + (b + 'a').toChar, MapItem(plan))
      }
    }
    map
  }

  val levels: Map[String, List[String]] = {

    def filterLevels(form: Int, numberBlocks: Int) =
      levelMap.filter(_._2.plan.board.form == form).filter(_._2.plan.variants.head.size == numberBlocks).keys.toList.sorted

    Map(
      "easy" -> filterLevels(4, 3),
      "normal" -> filterLevels(4, 4),
      "difficult" -> filterLevels(6, 3),
      "extreme" -> filterLevels(6, 4))

  }

  def load(levelName: String): Option[Level] = {
    val item = levelMap.get(levelName)
    if (item.isEmpty) {
      return None
    }

    if (item.get.level.isDefined) {
      return item.get.level
    }

    val variant = {
      try {
        item.get.plan.variants(levelName.last - 'a')
      } catch {
        case e: IndexOutOfBoundsException => return None
      }
    }

    val blocks = ListBuffer.empty[AnchoredGrid]
    for (block <- variant) {
      blocks += GridManager.load(block)
    }

    val name = levelName.substring(0, levelName.length - 1)
    val board = GridManager.load(name)

    val level = Some(Level(
      category = "TODO - category",
      name = levelName,
      width = item.get.plan.size,
      height = item.get.plan.size * 0.625,
      form = 0, // TODO
      board = board,
      blocks = blocks.toList
    ))
    levelMap.update(levelName, item.get.copy(level = level))
    level
  }*/
}


