package com.backbase.bst.actions.openapi

import com.google.common.base.CaseFormat
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiFile
import java.util.*

object SpecUtils {

    private val specTypesArr: Array<String> = arrayOf("-client", "-integration", "-service")

    fun extractServiceName(fileName: @NlsSafe String?): String {
        specTypesArr.forEach {
            if (fileName != null) {
                if (fileName.contains(it)) {
                    return fileName.substring(0, fileName.indexOf(it))
                }
            }
        }
        return ""
    }

    fun getSelectedFile(event: AnActionEvent): PsiFile? {
        return event.getData(LangDataKeys.PSI_FILE)
    }

    fun getFileName(event: AnActionEvent): String? {
        return getSelectedFile(event)?.virtualFile?.name
    }

    fun isFileAnOpenApiSpec(event: AnActionEvent): Boolean? {
        val selectedFile = getSelectedFile(event)
        // Return false if fileType is not yaml
        if (SpecConstants.YAML != selectedFile?.fileType?.name) return false
        // Return false if fileName is not a valid BB spec
        val fileName = selectedFile.virtualFile?.name
        var isValidBBFileName = false
        specTypesArr.forEach {
            if (fileName != null) {
                if (fileName.contains(it)) {
                    isValidBBFileName = true
                }
            }
        }
        if(!isValidBBFileName) return false
        // Check if file contains openapi
        return selectedFile?.virtualFile?.contentsToByteArray()?.toString(Charsets.UTF_8)?.contains("openapi")
    }

    fun createPropertiesForClientTemplate(serviceName: String, apiPackage: String): Properties {
        val properties = Properties();
        var serviceNameWithoutService = serviceName
        if (serviceName.lastIndexOf("service") > 0) {
            serviceNameWithoutService = serviceName.substring(0, serviceName.lastIndexOf("-service"))
        }

        println(serviceName)
        println(serviceNameWithoutService)
        properties.setProperty(SpecConstants.CLIENT_SERVICE_NAME_UPPERCASE, serviceName.replace("-".toRegex(), "_").toUpperCase())
        properties.setProperty(SpecConstants.CLIENT_SERVICE_NAME_LOWERCASE, serviceName.toLowerCase())
        properties.setProperty(SpecConstants.CLIENT_API_PACKAGE_TRIM_LAST_DOT,
                apiPackage.substring(0, apiPackage.lastIndexOf(".")).toLowerCase())
        properties.setProperty(SpecConstants.CLIENT_SERVICE_NAME_CAMELCASE,
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, serviceName.replace("-", "_")))
        properties.setProperty(SpecConstants.CLIENT_SERVICE_NAME_WITHOUT_SERVICE_CAMELCASE,
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, serviceNameWithoutService.replace("-".toRegex(), "_")))
        properties.setProperty(SpecConstants.CLIENT_SERVICE_NAME_SINGLE_WORD_WITHOUT_SERVICE_LOWERCASE,
                serviceNameWithoutService.replace("-".toRegex(), "").toLowerCase())

        return properties

    }

}