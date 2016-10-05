lazy val commonSettings = Seq(
  organization := "de.htwg",
  version := "0.1",
  scalaVersion := "2.11.8",

  libraryDependencies ++= Seq(
    /*"org.scalactic" %% "scalactic" % "2.2.6",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "org.scala-lang.modules" %% "scala-swing" % "1.0.2",
    "org.json4s" %% "json4s-jackson" % "3.3.0",
    "org.mongodb" % "mongo-java-driver" % "3.2.2",
    "org.mongodb" % "bson" % "3.2.2",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.clapper" %% "grizzled-slf4j" % "1.0.2",
    "org.webjars" %% "webjars-play" % "2.5.0-2",
    "com.typesafe.akka" %% "akka-cluster-tools" % "2.4.6", */
    "com.typesafe.akka" %% "akka-actor" % "2.4.9-RC2",
    "com.typesafe.akka" %% "akka-camel" % "2.4.10"
  )

)

lazy val scongo = (project in file(".")).
  settings(commonSettings: _*)//.
  //enablePlugins(PlayScala)


lazy val core = (project in file("core")).
  settings(commonSettings: _*).
  settings(
    name := "scongo-core"
  )

lazy val web = (project in file("web")).
  settings(commonSettings: _*).
  settings(
    name := "scongo-web"
  ).
  dependsOn(core)//.
  //enablePlugins(PlayScala)