package com.backbase.bst.actions

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.Library
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.*
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseMotionListener
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane


class AddAnyServiceSSDKModuleDialog(project : Project, private val modules : Map<String, Library>) : DialogWrapper(project, true) {

    var  mainPanel : DialogPanel? = null


    init {
        title = BackbaseBundle.message("action.add.modules.dialog.title")
        init()
    }

    var eventClass: String = "";

    override fun createCenterPanel(): JComponent? {
        val keyByPairs = modules.keys.chunked(1)
        mainPanel = panel{


                keyByPairs.forEach {

                    val element1 = modules[it[0]]
                    row  {

                        cell {
                            checkBox(it[0], element1!!::selected)
                                .comment(element1.description, 100, false)
                                .withLeftGap()

                        }

                    }
                }
            }


        mainPanel!!.autoscrolls = true

        val doScrollRectToVisible: MouseMotionListener = object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val r = Rectangle(e.getX(), e.getY(), 1, 1)
                (e.getSource() as JPanel).scrollRectToVisible(r)
            }
        }

        mainPanel!!.addMouseMotionListener(doScrollRectToVisible)



        return JBScrollPane(mainPanel)
    }




    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction();
    }
}