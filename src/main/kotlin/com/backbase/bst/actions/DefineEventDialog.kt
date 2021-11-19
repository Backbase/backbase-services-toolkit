package com.backbase.bst.actions

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class DefineEventDialog(project : Project) : DialogWrapper(project, true) {

    var  mainPanel : DialogPanel? = null

    init {
        title = BackbaseBundle.message("action.add.define.event.dialog.title")
        init()
    }

    var eventName: String = "undefined";

    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row {
                 cell {
                     label(BackbaseBundle.message("action.add.define.event.dialog.event.name"))
                     textField(::eventName).focused()
                 }
            }
        }

        return mainPanel
    }

    override fun doValidate(): ValidationInfo? {
        if (eventName == "undefined"){
            return ValidationInfo("Event Name cannot be empty")
        }


        return null;
    }

    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction();
    }




}