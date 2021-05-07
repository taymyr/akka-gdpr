dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "8.11"
dependencyOverrides += "org.junit.jupiter" % "junit-jupiter-engine" % "5.3.1"

addSbtPlugin("com.etsy"                % "sbt-checkstyle-plugin"  % "3.1.1")
addSbtPlugin("net.aichler"             % "sbt-jupiter-interface"  % "0.7.0") // JUnit 5
