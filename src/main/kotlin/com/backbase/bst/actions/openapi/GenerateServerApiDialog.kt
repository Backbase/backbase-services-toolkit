package com.backbase.bst.actions.openapi

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.panel
import java.io.File
import javax.swing.JComponent

class GenerateServerApiDialog (project: Project, file: VirtualFile) : DialogWrapper(project, true) {

    var specPath: String
    var apiPackage: String
    var modelPackage: String

    private var serviceName: String
    private var  mainPanel : DialogPanel? = null
    private var  serviceNameTextField : CellBuilder<JBTextField>? = null
    private var  specPathTextField : CellBuilder<JBTextField>? = null
    private var  apiPackageTextField : CellBuilder<JBTextField>? = null
    private var  modelPackageTextField : CellBuilder<JBTextField>? = null

    init {
        title = BackbaseBundle.message("action.add.openapi.server.api.dialog.title")
        serviceName = SpecUtils.extractServiceNameWithType(file.name)
        val relativeSpecPath: String = File(project.basePath).toURI().relativize(File(file.canonicalPath).toURI()).path
        specPath = ".."+File.separator+relativeSpecPath
        val stripServiceName = serviceName.replace("-","")
        apiPackage = "com.backbase.$stripServiceName.api"
        modelPackage = "com.backbase.$stripServiceName.api.model"
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
        super.doOKAction()
    }

}