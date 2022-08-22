package com.backbase.bst.actions.extensions

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.MavenTools
import com.backbase.bst.common.extensions.BehaviourExtensionsConstants
import com.backbase.bst.common.extensions.BehaviourExtensionsUtils
import com.backbase.bst.common.extensions.RouteExtensionType
import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.ui.GotItMessage
import com.intellij.ui.awt.RelativePoint
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil
import java.util.*

class GenerateHooksAction : DumbAwareAction() {

    override fun update(e: AnActionEvent) {

        val file = MavenTools.findPomXml(e.dataContext)

        if (file == null) {
            e.presentation.isVisible = false
            return
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = MavenActionUtil.getProject(e.dataContext) ?: return
        val selectedModule = e.getData(LangDataKeys.MODULE) // If a multimodule project
        val dialog = GenerateHooksDialog(project)
        dialog.show()
        if (dialog.exitCode === DialogWrapper.CANCEL_EXIT_CODE) {
            return
        }

        val mavenModel = MavenDomUtil
            .getMavenDomProjectModel(project, MavenTools.findProjectPom(project)!!)
        val properties = createProperties(dialog.behaviorName, mavenModel!!.groupId.value.toString())

        when (dialog.selectedRouteExtensionType) {
            RouteExtensionType.SIMPLE_ROUTE_HOOK -> {
                BehaviourExtensionsUtils.addClass(
                    selectedModule!!, project, "simpleHookBehaviorExtension",
                    properties
                )
            }
            RouteExtensionType.REPLACE_ROUTE_HOOK -> {
                BehaviourExtensionsUtils.addClass(
                    selectedModule!!, project, "replaceHookRouteBuilder",
                    properties
                )
            }
            RouteExtensionType.EXTEND_ROUTE_HOOK -> {
                BehaviourExtensionsUtils.addClass(
                    selectedModule!!, project, "extendHookRouteBuilder",
                    properties
                )
                BehaviourExtensionsUtils.addClass(
                    selectedModule, project, "extendHookRouteBuilderEndpoints",
                    properties
                )
            }
        }
        val gotIt = GotItMessage.createMessage(
            BackbaseBundle.message("action.add.define.event.dialog.gotit.title"),
            BackbaseBundle.message("action.add.extensions.hooks.gotit.message")
        )

        gotIt.show(
            RelativePoint.getCenterOf(FileEditorManager.getInstance(project).selectedTextEditor!!.component),
            Balloon.Position.above
        )

    }

    fun createProperties(behaviourName: String, apiPackage: String): Properties {
        val properties = Properties()
        var serviceNameWithBehaviour = behaviourName
        if (serviceNameWithBehaviour.lastIndexOf("behavior") <= 0) {
            serviceNameWithBehaviour = "$behaviourName-behavior"
        }
        println(serviceNameWithBehaviour)
        properties.setProperty(
            BehaviourExtensionsConstants.PACKAGE_NAME,
            apiPackage
        )
        properties.setProperty(BehaviourExtensionsConstants.BEHAVIOUR_NAME, serviceNameWithBehaviour.lowercase())
        properties.setProperty(
            BehaviourExtensionsConstants.BEHAVIOUR_NAME_CAMELCASE,
            CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, serviceNameWithBehaviour.replace("-", "_"))
                .replace("_", "")
        )
        return properties
    }
}