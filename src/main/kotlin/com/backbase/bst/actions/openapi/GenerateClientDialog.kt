package com.backbase.bst.actions.openapi

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.FileTools
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class GenerateClientDialog(project: Project, fileName: @NlsSafe String?) : DialogWrapper(project, true) {

    var serviceName: String
    var specPath: String
    var apiPackage: String
    var modelPackage: String

    var  mainPanel : DialogPanel? = null
    var  serviceNameTextField : CellBuilder<JBTextField>? = null
    var  specPathTextField : CellBuilder<JBTextField>? = null
    var  apiPackageTextField : CellBuilder<JBTextField>? = null
    var  modelPackageTextField : CellBuilder<JBTextField>? = null
    var addRestClientConfiguration: Boolean = true;

    init {
        title = BackbaseBundle.message("action.add.openapi.client.dialog.title")
        serviceName = SpecUtils.extractServiceName(fileName)
        if(!serviceName.endsWith("service")){
            serviceName += "-service"
        }
        specPath = "\${project.basedir}/src/main/resources/$fileName";
        var stripServiceName = serviceName.replace("-","")
        apiPackage = "com.backbase.$stripServiceName.api.client";
        modelPackage = "com.backbase.$stripServiceName.api.client.model";
        init()
    }


    override fun createCenterPanel(): JComponent? {
        mainPanel = panel {
            row {
                cell {
                    label(BackbaseBundle.message("action.add.openapi.client.dialog.serviceName"))

                    serviceNameTextField = textField(::serviceName)
                    serviceNameTextField!!.focused()
                }
            }
            row {
                cell {
                    label(BackbaseBundle.message("action.add.openapi.client.dialog.inputSpec"))

                    specPathTextField = textField(::specPath)
                    specPathTextField!!.focused()
                }
            }
            row {
                cell {
                    label(BackbaseBundle.message("action.add.openapi.client.dialog.apiPackage"))
                    apiPackageTextField = textField(::apiPackage)
                    apiPackageTextField!!.focused()
                }
            }
            row {
                cell {
                    label(BackbaseBundle.message("action.add.openapi.client.dialog.modelPackage"))
                    modelPackageTextField = textField(::modelPackage)
                    modelPackageTextField!!.focused()
                }
            }
            row {
                checkBox(BackbaseBundle.message("action.add.openapi.client.dialog.generateConfig"),::addRestClientConfiguration)
            }
        }

        return mainPanel
    }


    override fun doOKAction() {
        mainPanel!!.apply()
        super.doOKAction();
    }
}