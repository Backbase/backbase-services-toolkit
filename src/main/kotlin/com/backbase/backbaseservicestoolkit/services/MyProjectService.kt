package com.backbase.backbaseservicestoolkit.services

import com.intellij.openapi.project.Project
import com.backbase.backbaseservicestoolkit.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
