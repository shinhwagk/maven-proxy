name := "maven-proxy"

version := "0.9.7"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.12",
  "com.typesafe" % "config" % "1.3.0",
  "com.h2database" % "h2" % "1.4.187",
  "com.typesafe.akka" %% "akka-remote" % "2.3.12"
)