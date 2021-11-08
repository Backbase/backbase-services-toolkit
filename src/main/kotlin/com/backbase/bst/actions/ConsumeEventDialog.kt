package com.backbase.bst.actions

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBUI
import java.awt.Color
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class ConsumeEventDialog(project : Project, private val events : List<String>) : DialogWrapper(project, true) {

    var  mainPanel : DialogPanel? = null
    var listModel = DefaultComboBoxModel(events.toTypedArray())
    val jbTable: JBList<String>

    init {
        title =BackbaseBundle.message("action.add.consume.event.dialog.title")
        jbTable = JBList(listModel)
        jbTable.autoscrolls = true
        jbTable.border=JBUI.Borders.customLine(Color.BLACK, 1)
        jbTable.selectionMode = ListSelectionModel.SINGLE_SELECTION
        jbTable.selectedIndex = 0
        init()
    }

    var eventClass: String = "";

    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row {
                label(BackbaseBundle.message("action.add.consume.event.dialog.select.event"))
            }
            row {
                jbTable().focused()
            }
        }

        return mainPanel
    }


    override fun doOKAction() {
        mainPanel!!.apply()
        eventClass = jbTable.selectedValue
        super.doOKAction();
    }
}