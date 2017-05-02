lazy val commonSettings = Seq(
  organization := "org.criteo",
  version := "0.1.9",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.11.8", "2.12.1")
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "slab",
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-native" % "3.4.2",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "org.mockito" % "mockito-core" % "2.7.0" % Test,
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "com.criteo.lolhttp" %% "lolhttp" % "0.3.2",
      "io.netty" % "netty-codec-http2" % "4.1.9.Final"
    ),
    publishTo := Some("Criteo thirdparty" at "http://nexus.criteo.prod/content/repositories/criteo.thirdparty"),
    credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.criteo.prod", System.getenv("MAVEN_USER"), System.getenv("MAVEN_PASSWORD"))
  )

lazy val example = (project in file("example"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.25"
    )
  )
  .settings(
    Option(System.getenv().get("GENERATE_EXAMPLE_DOC")).map { _ =>
      Seq(
        autoCompilerPlugins := true,
        addCompilerPlugin("com.criteo.socco" %% "socco-plugin" % "0.1.1"),
        scalacOptions := Seq(
          "-P:socco:out:examples",
          "-P:socco:package_scala.concurrent:http://www.scala-lang.org/api/current/"
        )
      )
    }.getOrElse(Nil): _*
  )
  .dependsOn(root)

lazy val buildWebapp = taskKey[Unit]("build webapp")

buildWebapp := {
  "npm install" !

  "npm run build -- -p --env.out=target/scala-2.11/classes" !

  "npm run build -- -p --env.out=target/scala-2.12/classes" !
}

packageBin in Compile <<= (packageBin in Compile) dependsOn buildWebapp

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case PathList("javax", "annotation", xs@_*) => MergeStrategy.first
  case other => MergeStrategy.defaultMergeStrategy(other)
}

