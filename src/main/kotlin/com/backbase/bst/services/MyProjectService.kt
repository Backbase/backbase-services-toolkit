package com.backbase.bst.services

import com.intellij.openapi.project.Project
import com.backbase.bst.BackbaseBundle

class MyProjectService(project: Project) {

    init {
        println(BackbaseBundle.message("projectService", project.name))
    }
}
