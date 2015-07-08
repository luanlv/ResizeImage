name := "Resize-image-demo-app"

version := "0.11.0"

scalaVersion := "2.11.7"

play.twirl.sbt.Import.TwirlKeys.templateFormats ++= Map("stream" -> "com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamFormat")

play.twirl.sbt.Import.TwirlKeys.templateImports ++= Vector("com.ybrikman.ping.scalaapi.bigpipe.HtmlStream",
                                                            "com.ybrikman.ping.scalaapi.bigpipe._")

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(  javaJdbc,
  cache,
  filters,
  javaWs,
  "com.mohiva" %% "play-html-compressor" % "0.4.1-SNAPSHOT",
  "javax.inject" % "javax.inject" % "1",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0.play24",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.0.1",
  "com.sksamuel.scrimage" %% "scrimage-io" % "2.0.1",
  "org.apache.xmlgraphics" % "batik-codec" % "1.8",
  "com.ybrikman.ping" %% "big-pipe" % "0.0.12",
  "joda-time" % "joda-time" % "2.8.1",
  "org.joda" % "joda-convert" % "1.7"
)

routesGenerator := InjectedRoutesGenerator


lazy val root = (project in file(".")).enablePlugins(PlayScala)
