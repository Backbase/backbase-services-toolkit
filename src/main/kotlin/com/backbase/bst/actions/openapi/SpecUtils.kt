package com.backbase.bst.actions.openapi

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.MavenTools
import com.google.common.base.CaseFormat
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.NotNull
import org.jetbrains.idea.maven.dom.model.MavenDomConfiguration
import org.jetbrains.idea.maven.model.MavenId
import java.util.*

object SpecUtils {

    private val specTypesArr: Array<String> = arrayOf("-client", "-integration", "-service")

    /*
     * Example:
     * input = sample-service-api-v1.yaml ; output = sample
     * input = sample-client-api-v1.yaml ; output = sample
     * */
    fun extractServiceName(fileName: String?): String {
        if (fileName != null) {
            specTypesArr.forEach {
                if (fileName.contains(it)) {
                    return fileName.substring(0, fileName.indexOf(it))
                }
            }
            return fileName
        } else {
            return ""
        }
    }

    /*
    * Example:
    * input = sample-service-api-v1.yaml ; output = sample-service
    * input = sample-client-api-v1.yaml ; output = sample-client
    * */
    fun extractServiceNameWithType(fileName: String?): String {
        if (fileName != null) {
            specTypesArr.forEach {
                if (fileName.contains(it)) {
                    return fileName.substring(0, fileName.indexOf(it) + it.length)
                }
            }
            return fileName
        } else {
            return ""
        }
    }

    private fun getSelectedFile(event: AnActionEvent): PsiFile? {
        return event.getData(LangDataKeys.PSI_FILE)
    }

    fun getFileName(event: AnActionEvent): String? {
        return getSelectedFile(event)?.virtualFile?.name
    }

    fun isFileAnOpenApiSpec(event: AnActionEvent): Boolean {
        val selectedFile = getSelectedFile(event)
        // Return false if fileType is not yaml
        return SpecConstants.YAML == selectedFile?.fileType?.name
        // Removed checks like file name , contents, because its boat-maven-plugin logic.
    }

    fun isFileAnOpenApiSpec(file: VirtualFile): Boolean {
        // Return false if fileType is not yaml
        return SpecConstants.YAML == file.fileType.name
        // Removed checks like file name , contents, because its boat-maven-plugin logic.
    }

    fun isPomFileSelected(event: AnActionEvent): Boolean {
        val selectedFile = getSelectedFile(event)
        // Return false if fileType is not pom.xml
        return SpecConstants.POM_XML == selectedFile?.name
    }

    fun createPropertiesForClientTemplate(serviceName: String, apiPackage: String): Properties {
        val properties = Properties()
        var serviceNameWithoutService = serviceName
        if (serviceName.lastIndexOf("service") > 0) {
            serviceNameWithoutService = serviceName.substring(0, serviceName.lastIndexOf("-service"))
        }

        println(serviceName)
        println(serviceNameWithoutService)
        properties.setProperty(
            SpecConstants.CLIENT_SERVICE_NAME_UPPERCASE,
            serviceName.replace("-".toRegex(), "_").lowercase()
        )
        properties.setProperty(SpecConstants.CLIENT_SERVICE_NAME_LOWERCASE, serviceName.lowercase())
        properties.setProperty(
            SpecConstants.CLIENT_API_PACKAGE_TRIM_LAST_DOT,
            apiPackage.substring(0, apiPackage.lastIndexOf(".")).lowercase()
        )
        properties.setProperty(
            SpecConstants.CLIENT_SERVICE_NAME_CAMELCASE,
            CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, serviceName.replace("-", "_"))
        )
        properties.setProperty(
            SpecConstants.CLIENT_SERVICE_NAME_WITHOUT_SERVICE_CAMELCASE,
            CaseFormat.UPPER_UNDERSCORE.to(
                CaseFormat.UPPER_CAMEL,
                serviceNameWithoutService.replace("-".toRegex(), "_")
            )
        )
        properties.setProperty(
            SpecConstants.CLIENT_SERVICE_NAME_SINGLE_WORD_WITHOUT_SERVICE_LOWERCASE,
            serviceNameWithoutService.replace("-".toRegex(), "").lowercase()
        )

        return properties

    }

    fun addAdditionalDependencies(dependencies : MutableList<MavenId>,
                                  project: Project, file: VirtualFile, dataContext: DataContext, notificationTitle:String) {

        dependencies.removeIf {
            MavenTools.findDependencyOnBom(project, file, it)
        }

        if (dependencies.isNotEmpty()) {
            WriteCommandAction.runWriteCommandAction(project) {
                MavenTools.writeDependenciesOnPom(
                    project, file, dataContext,
                    dependencies
                )
            }

            Notification(
                "Backbase notification group", notificationTitle , BackbaseBundle.message("action.add.openapi.add.dependencies"),
                NotificationType.INFORMATION
            ).notify(project)
        }
    }

    fun addElement(
        configuration: @NotNull MavenDomConfiguration,
        name: String,
        value: String
    ) {
        val createChildTag = configuration.xmlTag!!.createChildTag(name, "", value, false)
        configuration.xmlTag!!.addSubTag(createChildTag, false)
    }

}