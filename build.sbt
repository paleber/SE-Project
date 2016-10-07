lazy val commonSettings = Seq(
  organization := "de.htwg",
  version := "0.1",
  scalaVersion := "2.11.8"
)

lazy val scongo = (project in file(".")).
  settings(commonSettings: _*).
  enablePlugins(PlayScala)

lazy val core = (project in file("core")).
  settings(commonSettings: _*).
  settings(
    name := "scongo-core",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.11",
      "org.json4s" %% "json4s-jackson" % "3.4.1"
    )
  )

lazy val web = (project in file("web")).
  settings(commonSettings: _*).
  settings(
    name := "scongo-web"
  ).
  dependsOn(core).
  enablePlugins(PlayScala)
