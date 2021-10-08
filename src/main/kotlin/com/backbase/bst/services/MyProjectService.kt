package com.backbase.bst.services

import com.intellij.openapi.project.Project
import com.backbase.bst.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
