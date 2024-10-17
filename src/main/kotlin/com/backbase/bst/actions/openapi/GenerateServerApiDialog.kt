package com.backbase.bst.actions.openapi

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import java.io.File
import javax.swing.JComponent

class GenerateServerApiDialog(project: Project, file: VirtualFile) : DialogWrapper(project, true) {

    var specPath: String
    var apiPackage: String
    var modelPackage: String

    private var serviceName: String
    private var mainPanel: DialogPanel? = null

    init {
        title = BackbaseBundle.message("action.add.openapi.server.api.dialog.title")
        serviceName = SpecUtils.extractServiceNameWithType(file.name)
        val relativeSpecPath: String = File(project.basePath).toURI().relativize(File(file.canonicalPath).toURI()).path
        specPath = ".." + File.separator + relativeSpecPath
        val stripServiceName = serviceName.replace("-", "")
        apiPackage = "com.backbase.$stripServiceName.api"
        modelPackage = "com.backbase.$stripServiceName.api.model"
        init()
    }

    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row {

                textField().label(BackbaseBundle.message("action.add.openapi.server.api.dialog.serviceName"))
                    .bindText(::serviceName).focused()

            }
            row {
                textField().label(BackbaseBundle.message("action.add.openapi.server.api.dialog.inputSpec"))
                    .bindText(::specPath).focused()


            }
            row {
                textField().label(BackbaseBundle.message("action.add.openapi.server.api.dialog.apiPackage"))
                    .bindText(::apiPackage).focused()

            }
            row {
                textField().label(BackbaseBundle.message("action.add.openapi.server.api.dialog.modelPackage"))
                    .bindText(::modelPackage).focused()

            }
        }

        return mainPanel
    }

    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction()
    }

}