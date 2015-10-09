name := "crowdop-web"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.apache.commons" % "commons-csv" % "1.2",
  "org.json" % "json" % "20141113"
)     

play.Project.playJavaSettings
