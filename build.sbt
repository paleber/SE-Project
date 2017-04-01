lazy val commonSettings = Seq(
  organization := "de.htwg",
  version := "0.1",
  scalaVersion := "2.11.8"
)

lazy val core = (project in file("core")).
  settings(commonSettings: _*).
  settings(
    name := "scongo-core",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.11",
      "org.json4s" %% "json4s-jackson" % "3.4.1",
      "org.scaldi" %% "scaldi" % "0.5.8",
      "org.scaldi" %% "scaldi-akka" % "0.5.8",
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )

lazy val scongo = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.11",
      "org.json4s" %% "json4s-jackson" % "3.4.1",

      //angular2 dependencies
      "org.webjars.npm" % "angular__common" % "2.2.0",
      "org.webjars.npm" % "angular__compiler" % "2.2.0",
      "org.webjars.npm" % "angular__core" % "2.2.0",
      "org.webjars.npm" % "angular__http" % "2.2.0",
      "org.webjars.npm" % "angular__forms" % "2.2.0",
      "org.webjars.npm" % "angular__router" % "3.2.0",
      "org.webjars.npm" % "angular__platform-browser-dynamic" % "2.2.0",
      "org.webjars.npm" % "angular__platform-browser" % "2.2.0",
      "org.webjars.npm" % "systemjs" % "0.19.40",
      "org.webjars.npm" % "rxjs" % "5.0.0-beta.12",
      "org.webjars.npm" % "reflect-metadata" % "0.1.8",
      "org.webjars.npm" % "zone.js" % "0.6.26",
      "org.webjars.npm" % "core-js" % "2.4.1",
      "org.webjars.npm" % "symbol-observable" % "1.0.1",

      "org.webjars.npm" % "typescript" % "2.1.4",

      //tslint dependency
      "org.webjars.npm" % "tslint-eslint-rules" % "3.1.0",
      "org.webjars.npm" % "tslint-microsoft-contrib" % "2.0.12",
      "org.webjars.npm" % "types__jasmine" % "2.2.26-alpha" % "test",

      // silhouette
      "com.mohiva" %% "play-silhouette" % "4.0.0",
      "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0",
      "com.mohiva" %% "play-silhouette-persistence" % "4.0.0",
      "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0",
      "org.webjars" %% "webjars-play" % "2.5.0-2",
      "net.codingwell" %% "scala-guice" % "4.0.1",
      "com.iheart" %% "ficus" % "1.4.0",
      "com.typesafe.play" %% "play-mailer" % "5.0.0",
      "com.enragedginger" %% "akka-quartz-scheduler" % "1.5.0-akka-2.4.x",
      "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3",
      "com.mohiva" %% "play-silhouette-testkit" % "4.0.0" % "test")
  ).
  enablePlugins(PlayScala).
  dependsOn(core).
  enablePlugins(PlayScala, SbtWeb)


dependencyOverrides += "org.webjars.npm" % "minimatch" % "3.0.0"

// use the webjars npm directory (target/web/node_modules ) for resolution of module imports of angular2/core etc
resolveFromWebjarsNodeModulesDir := true

// use the combined tslint and eslint rules plus ng2 lint rules
(rulesDirectories in tslint) := Some(List(
  tslintEslintRulesDir.value,
  ng2LintRulesDir.value
))

logLevel in tslint := Level.Debug