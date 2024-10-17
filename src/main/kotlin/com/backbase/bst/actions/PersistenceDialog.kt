package com.backbase.bst.actions

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

import javax.swing.JComponent

class PersistenceDialog(project : Project) : DialogWrapper(project, true) {

    private var  mainPanel : DialogPanel? = null

    init {
        title = BackbaseBundle.message("action.add.persistence.support.dialog.title")
        init()
    }

    var addPomDependencies: Boolean = false
    var addLiquibaseFile: Boolean = false
    var addApplicationClassPersistenceSupport: Boolean = false




    override fun createCenterPanel(): JComponent? {
        mainPanel = panel{
            row {
                checkBox(BackbaseBundle.message("action.add.persistence.support.dialog.pom")).bindSelected(::addPomDependencies)
            }
            row {
                checkBox(BackbaseBundle.message("action.add.persistence.support.dialog.liquibase")).bindSelected(::addLiquibaseFile)
            }
            row {
                checkBox(BackbaseBundle.message("action.add.persistence.support.dialog.annotation")).bindSelected(::addApplicationClassPersistenceSupport)
               }
        }

        return mainPanel
    }


    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction()
    }
}