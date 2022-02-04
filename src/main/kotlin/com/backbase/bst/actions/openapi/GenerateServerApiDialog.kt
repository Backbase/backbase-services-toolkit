package com.backbase.bst.actions.openapi

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.panel
import java.io.File
import javax.swing.JComponent

class GenerateServerApiDialog (project: Project, file: @NlsSafe VirtualFile) : DialogWrapper(project, true) {

    var serviceName: String
    var specPath: String
    var apiPackage: String
    var modelPackage: String

    var  mainPanel : DialogPanel? = null
    var  serviceNameTextField : CellBuilder<JBTextField>? = null
    var  specPathTextField : CellBuilder<JBTextField>? = null
    var  apiPackageTextField : CellBuilder<JBTextField>? = null
    var  modelPackageTextField : CellBuilder<JBTextField>? = null

    init {
        title = BackbaseBundle.message("action.add.openapi.server.api.dialog.title")
        serviceName = SpecUtils.extractServiceNameWithType(file.name)
        val relativeSpecPath: String = File(project.basePath).toURI().relativize(File(file.canonicalPath).toURI()).getPath()
        specPath = ".."+File.separator+relativeSpecPath
        var stripServiceName = serviceName.replace("-","")
        apiPackage = "com.backbase.$stripServiceName.api";
        modelPackage = "com.backbase.$stripServiceName.api.model";
        init()
    }

    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row {
                cell {
                    label(BackbaseBundle.message("action.add.openapi.server.api.dialog.serviceName"))

                    serviceNameTextField = textField(::serviceName)
                    serviceNameTextField!!.focused()
                }
            }
            row {
                cell {
                    label(BackbaseBundle.message("action.add.openapi.server.api.dialog.inputSpec"))

                    specPathTextField = textField(::specPath)
                    specPathTextField!!.focused()
                }
            }
            row {
                cell {
                    label(BackbaseBundle.message("action.add.openapi.server.api.dialog.apiPackage"))
                    apiPackageTextField = textField(::apiPackage)
                    apiPackageTextField!!.focused()
                }
            }
            row {
                cell {
                    label(BackbaseBundle.message("action.add.openapi.server.api.dialog.modelPackage"))
                    modelPackageTextField = textField(::modelPackage)
                    modelPackageTextField!!.focused()
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