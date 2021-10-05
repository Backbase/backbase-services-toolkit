package com.github.fredysierra.backbaseservicestoolkit.services

import com.intellij.openapi.project.Project
import com.github.fredysierra.backbaseservicestoolkit.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
