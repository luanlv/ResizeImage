name := "Resize-image-demo-app"

version := "0.11.0"

scalaVersion := "2.11.7"

val jodatime = "joda-time" % "joda-time" % "2.8.1"
val jodaConvert = "org.joda" % "joda-convert" % "1.7"

libraryDependencies ++= Seq(  javaJdbc,
  cache,
  filters,
  javaWs,
  "com.google.inject" % "guice" % "3.0",
  "javax.inject" % "javax.inject" % "1",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0.play24",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0.M2",
  jodatime,
  jodaConvert
)

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala)
