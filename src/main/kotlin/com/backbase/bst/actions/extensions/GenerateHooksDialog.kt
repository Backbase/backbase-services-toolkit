package com.backbase.bst.actions.extensions

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.extensions.RouteExtensionType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class GenerateHooksDialog(project: Project) : DialogWrapper(project, true) {

    private var mainPanel: DialogPanel? = null
    private var textField: CellBuilder<JBTextField>? = null
    var behaviorName: String = "undefined"
    var selectedRouteExtensionType: RouteExtensionType = RouteExtensionType.SIMPLE_ROUTE_HOOK

    init {
        title = BackbaseBundle.message("action.add.extensions.generate.hooks.dialog.title")
        init()
    }


    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row {
                label(BackbaseBundle.message("action.add.extensions.generate.hooks.name"))
                textField = textField(::behaviorName)
                textField!!.focused()

            }
            row {
                label("Route Extension Type")
                comboBox(DefaultComboBoxModel(RouteExtensionType.values()), ::selectedRouteExtensionType)

            }
        }

        return mainPanel
    }


    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction()
    }
}