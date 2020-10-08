name := "Payments"

version := "0.1"

scalaVersion := "2.13.3"

val AkkaVersion = "2.6.9"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.0"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"