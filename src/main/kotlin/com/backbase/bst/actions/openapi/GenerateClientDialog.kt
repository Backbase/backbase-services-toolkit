package com.backbase.bst.actions.openapi

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

import javax.swing.JComponent

class GenerateClientDialog(project: Project, fileName: String?) : DialogWrapper(project, true) {

    var serviceName: String
    var specPath: String
    var apiPackage: String
    var modelPackage: String

    private var mainPanel: DialogPanel? = null
    var addRestClientConfiguration: Boolean = true

    init {
        title = BackbaseBundle.message("action.add.openapi.client.dialog.title")
        serviceName = SpecUtils.extractServiceName(fileName)
        if (!serviceName.endsWith("service")) {
            serviceName += "-service"
        }
        specPath = "\${project.basedir}/src/main/resources/$fileName"
        val stripServiceName = serviceName.replace("-", "")
        apiPackage = "com.backbase.$stripServiceName.api.client"
        modelPackage = "com.backbase.$stripServiceName.api.client.model"
        init()
    }


    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row (BackbaseBundle.message("action.add.openapi.client.dialog.serviceName")){
                textField().align(Align.FILL)
                    .bindText(::serviceName).focused()
            }
            row(BackbaseBundle.message("action.add.openapi.client.dialog.inputSpec")) {
                textField().align(Align.FILL)
                    .bindText(::specPath).focused()
            }
            row(BackbaseBundle.message("action.add.openapi.client.dialog.apiPackage")) {
                textField().align(Align.FILL)
                    .bindText(::apiPackage).focused()
            }
            row(BackbaseBundle.message("action.add.openapi.client.dialog.modelPackage")) {
                textField().align(Align.FILL)
                    .bindText(::modelPackage).focused()
            }
            row {
                checkBox(BackbaseBundle.message("action.add.openapi.client.dialog.generateConfig")).bindSelected(::addRestClientConfiguration)
            }
        }

        return mainPanel
    }


    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction()
    }
}