// in the name of ALLAH

import me.samei.sbt._

lazy val context = DefContext(org = "me.samei")

lazy val std = Prj("std", context)

lazy val example = Prj("std-example", context).dependsOn(std)

lazy val root = Prj.root("projects", "?", context).aggregate(std, example)

