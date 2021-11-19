package com.backbase.bst.actions

import com.backbase.bst.common.FileTools
import com.backbase.bst.common.MavenTools
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil


class ConsumeEventAction : DumbAwareAction(){


    override fun actionPerformed(e: AnActionEvent) {
        val project = MavenActionUtil.getProject(e.dataContext) ?: return

        val eventClass = JavaPsiFacade.getInstance(project).findClass("com.backbase.buildingblocks.persistence.model.Event",
            GlobalSearchScope.allScope(project))

        fun getEntireName (psiClass: PsiClass) : String {
            val packageName = (psiClass.containingFile as PsiJavaFile).packageName
            return "$packageName.${psiClass.name}"
        }

        val events = ClassInheritorsSearch.search(eventClass!!, GlobalSearchScope.allScope(project),
            true, true, true)
            .map { getEntireName(it)}.toList()

        val consumeEventDialog = ConsumeEventDialog(project, events)

        consumeEventDialog.show()

        if (consumeEventDialog.exitCode === DialogWrapper.CANCEL_EXIT_CODE) {
            return
        }

        val eventClassToHandle = consumeEventDialog.eventClass


        addEventHandler(e, project, eventClassToHandle)


    }

    private fun addEventHandler(e: AnActionEvent, project: Project, eventClass: String) {
        val justClass = eventClass.substringAfterLast("." )
        val eventHandlerName = justClass.plus("Handler")
        val eventHandlerTemplate = FileTemplateManager.getInstance(project).getTemplate("myresourcecreatedeventhandler")


        val file: VirtualFile = MavenTools.findPomXml(e.dataContext) ?: return

        val mavenModel = MavenDomUtil
            .getMavenDomProjectModel(project, file)



        val packageName = mavenModel!!.groupId.value + "." +  project.name.toLowerCase() + ".events"
        val directoryPath = packageName.replace(".", "/")
        val directory = VfsUtil.createDirectories(project.basePath + "/src/main/java/" + directoryPath)
        val psiDirectory = PsiManager.getInstance(project).findDirectory(directory)

        val fileTemplateManager = FileTemplateManager.getInstance(project)
        val properties = fileTemplateManager.defaultProperties
        properties.setProperty("PACKAGE_NAME", packageName)
        properties.setProperty("EVENT_NAME_HANDLER", eventHandlerName)
        properties.setProperty("EVENT_NAME", eventClass);


        val javaCodeStyleManager = JavaCodeStyleManager.getInstance(project)
        val codeStyleManager = CodeStyleManager.getInstance(project)

        WriteCommandAction.runWriteCommandAction(project) {
            val psiJavaFile = FileTools.createFileFromTemplate(
                eventHandlerName, eventHandlerTemplate, psiDirectory!!, "", true, HashMap<String, String>(),
                properties
            )
            javaCodeStyleManager.shortenClassReferences(psiJavaFile as PsiJavaFile)
            codeStyleManager.reformat(psiJavaFile)
            Notification("Backbase notification group", "Consume an Event", "Creating class $eventHandlerName",
                NotificationType.INFORMATION).notify(project)
        }
    }


    override fun update(e: AnActionEvent) {

        val file = MavenTools.findPomXml(e.dataContext)

        if(file == null) {
            e.presentation.isVisible = false
            return;
        }

        val eventClass = JavaPsiFacade.getInstance(e.project!!).findClass("com.backbase.buildingblocks.persistence.model.Event",
            GlobalSearchScope.allScope(e.project!!))

        if( eventClass == null) {
            e.presentation.isVisible = false
            return;
        }

        val events = ClassInheritorsSearch.search(eventClass!!, GlobalSearchScope.allScope(e.project!!),
            true, true, true)

        if( !events.any()) {
            e.presentation.isVisible = false
            return;
        }
    }
}