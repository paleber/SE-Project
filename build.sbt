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
      "org.webjars.npm" % "types__jasmine" % "2.2.26-alpha" % "test"
    )
  )

lazy val web = (project in file("web")).
  settings(commonSettings: _*).
  settings(
    name := "scongo-web"
  ).
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
//routesGenerator := InjectedRoutesGenerator