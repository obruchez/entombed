import org.scalajs.linker.interface.ModuleKind

ThisBuild / scalaVersion := "3.9.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "org.bruchez.olivier"
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Wunused:all")

val munitVersion = "1.3.6"

lazy val commonSettings = Seq(
  libraryDependencies += "org.scalameta" %%% "munit" % munitVersion % Test,
  testFrameworks += new TestFramework("munit.Framework")
)

/** Platform-independent code: assets, graphics, YM2149 audio, game logic. */
lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "entombed-core",
    Compile / sourceGenerators += Def.task {
      val base = (ThisBuild / baseDirectory).value
      Embed.generate(
        (Compile / sourceManaged).value,
        "entombed.data",
        "EmbeddedData",
        Seq("datasBin" -> base / "assets" / "DATAS.BIN")
      )
    }.taskValue
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.NoModule)))

/** 68000 source-level interpreter running the original 1995 commented source (the "oracle"). */
lazy val oracle = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("oracle"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "entombed-oracle",
    Compile / sourceGenerators += Def.task {
      val base = (ThisBuild / baseDirectory).value
      Embed.generate(
        (Compile / sourceManaged).value,
        "entombed.oracle",
        "EmbeddedSource",
        Seq(
          "stombed1" -> base / "reference" / "smaky" / "STOMBED1.utf8.L",
          "datasA" -> base / "reference" / "smaky" / "DATASA.BIN",
          "datasB" -> base / "reference" / "smaky" / "DATASB.BIN",
          "datasC" -> base / "reference" / "smaky" / "DATASC.BIN",
          "datasD" -> base / "reference" / "smaky" / "DATASD.BIN"
        )
      )
    }.taskValue
  )

/** Desktop application (Swing, javax.sound). */
lazy val jvm = project
  .in(file("jvm"))
  .dependsOn(core.jvm, oracle.jvm)
  .settings(commonSettings)
  .settings(
    name := "entombed-jvm",
    run / fork := true,
    run / javaOptions += "-Dapple.awt.application.name=Entombed",
    Compile / mainClass := Some("entombed.jvm.Main")
  )

/** Browser application (canvas, Web Audio). */
lazy val js = project
  .in(file("js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core.js, oracle.js)
  .settings(commonSettings)
  .settings(
    name := "entombed-js",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.NoModule)),
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.1"
  )

lazy val site = taskKey[Unit]("Builds the optimised browser version into docs/ (GitHub Pages)")

lazy val root = project
  .in(file("."))
  .aggregate(core.jvm, core.js, oracle.jvm, oracle.js, jvm, js)
  .settings(
    publish / skip := true,
    site := {
      val _ = (js / Compile / fullLinkJS).value
      val out = (js / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
      val docs = baseDirectory.value / "docs"
      IO.copyFile(out / "main.js", docs / "main.js")
      val html = IO
        .read(baseDirectory.value / "js" / "index.html")
        .replace("target/scala-3.9.0/entombed-js-fastopt/main.js", "main.js")
      IO.write(docs / "index.html", html)
      streams.value.log.info(s"Browser version written to $docs")
    }
  )
