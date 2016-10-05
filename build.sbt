lazy val commonSettings = Seq(
  organization := "de.htwg",
  version := "0.1",
  scalaVersion := "2.11.8",

  libraryDependencies ++= Seq(
    /*"org.scalactic" %% "scalactic" % "2.2.6",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "org.json4s" %% "json4s-jackson" % "3.3.0",
    "org.mongodb" % "mongo-java-driver" % "3.2.2",
    "org.mongodb" % "bson" % "3.2.2",*/
    "com.typesafe.akka" %% "akka-actor" % "2.4.11"
  )

)

lazy val scongo = (project in file(".")).
  settings(commonSettings: _*).
  enablePlugins(PlayScala)

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
  dependsOn(core).
  enablePlugins(PlayScala)