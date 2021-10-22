package com.backbase.bst.actions

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class DefineEventDialog(project : Project) : DialogWrapper(project, true) {

    var  mainPanel : DialogPanel? = null

    init {
        title = BackbaseBundle.message("action.add.define.event.dialog.title")
        init()
    }

    var eventName: String = "my-resource-created";

    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row {
                 cell {
                     label(BackbaseBundle.message("action.add.define.event.dialog.event.name"))
                     textField(::eventName)
                 }
            }
        }

        return mainPanel
    }


    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction();
    }
}