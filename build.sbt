// in the name of ALLAH

import me.samei.sbt._

lazy val context = DefContext(
    org = "me.samei",
    singleVersion = Some("1.0.0-SNAPSHOT")
)

lazy val std = Prj("std", context)

lazy val cli = Prj("cli", context).dependsOn(std)

lazy val config = Prj("config", context).dependsOn(std)

lazy val example = Prj("example", context).dependsOn(std, cli, config)

lazy val root = Prj.root("scala-util", context).aggregate(std, cli, config, example)

