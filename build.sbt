name := "maven-proxy"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.12"
//
//libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
//
//libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.7"

libraryDependencies += "com.typesafe" % "config" % "1.3.0"



fork in run := true