package com.backbase.bst.actions.extensions

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.extensions.RouteExtensionType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import javax.swing.DefaultComboBoxModel


import javax.swing.JComponent

class GenerateHooksDialog(project: Project) : DialogWrapper(project, true) {

    private var mainPanel: DialogPanel? = null
    var behaviorName: String = "undefined"
    var selectedRouteExtensionType: RouteExtensionType = RouteExtensionType.SIMPLE_ROUTE_HOOK

    init {
        title = BackbaseBundle.message("action.add.extensions.generate.hooks.dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row {
                textField().label(BackbaseBundle.message("action.add.extensions.generate.hooks.name")).bindText(::behaviorName).focused()
            }
            row {
                comboBox(DefaultComboBoxModel(RouteExtensionType.values()))
                    .label("Route Extension Type")
                    .bindItem(:: selectedRouteExtensionType.toNullableProperty())
            }
        }
        return mainPanel
    }

    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction()
    }
}