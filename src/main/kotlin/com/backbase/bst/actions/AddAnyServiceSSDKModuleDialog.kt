package com.backbase.bst.actions

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.Library
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseMotionListener
import javax.swing.JComponent
import javax.swing.JPanel

class AddAnyServiceSSDKModuleDialog(project: Project, private val modules: Map<String, Library>) :
    DialogWrapper(project, true) {

    private var mainPanel: DialogPanel? = null


    init {
        title = BackbaseBundle.message("action.add.modules.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val keyByPairs = modules.keys.chunked(1)
        mainPanel = panel {
            keyByPairs.forEach { pair ->
                val element1 = modules[pair[0]]

                element1?.let { module ->
                    row {
                        checkBox(pair[0]).bindSelected(module::selected) // Creates a checkbox with the label from pair[0] and binds it to `module.selected`
                            .comment(module.description) // Adds a comment (description)
                            .gap(RightGap.SMALL) // Use the latest gap management, replace `.withLeftGap()` with `.gap()`
                    }
                }
            }
        }

        mainPanel!!.autoscrolls = true

        val doScrollRectToVisible: MouseMotionListener = object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val r = Rectangle(e.x, e.y, 1, 1)
                (e.source as JPanel).scrollRectToVisible(r)
            }
        }

        mainPanel!!.addMouseMotionListener(doScrollRectToVisible)



        return JBScrollPane(mainPanel)
    }


    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction()
    }
}