package com.backbase.bst.wizard.extensions

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.BackbaseIcons
import com.backbase.bst.common.extensions.BehaviourExtensionsModuleType
import com.intellij.ide.projectWizard.ProjectSettingsStep
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.DumbAwareRunnable
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProject
import org.jetbrains.idea.maven.utils.MavenUtil
import java.io.File
import java.nio.file.Paths
import javax.swing.Icon

class BehaviourExtensionsProjectWizard : ModuleBuilder() {

    private var myAggregatorProject: MavenProject? = null

    var serviceGroupId = ""
    var serviceArtifactId = ""
    var bbVersion = "2022.02"
    var groupId: String = ""
    var artifactId: String = ""
    var version: String = "1.0.0-SNAPSHOT"
    var ssdkVersion: String = ""

    var myProjectId: MavenId? = null

    override fun getNodeIcon(): Icon = BackbaseIcons.BACKBASE_PROJECT_LOGO

    override fun getModuleType(): ModuleType<*> = BehaviourExtensionsModuleType()

    override fun getPresentableName(): String =
        BackbaseBundle.message("wizard.behaviour.extension.project.display.name")

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {

        return arrayOf(
            CaptureServiceStep(this),
            ExtensionProjectStep(this, wizardContext)
        )
    }

    override fun getBuilderId(): @NonNls String? {
        return javaClass.name
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        if (settingsStep is ProjectSettingsStep) {
            settingsStep.setNameValue(this.artifactId)
            settingsStep.setModuleName(this.artifactId)
            settingsStep.setPath(Paths.get(System.getProperty("user.home"),"IdeaProjects",this.artifactId).toString())
        }
        val nameLocationSettings = settingsStep.moduleNameLocationSettings
        if (nameLocationSettings != null && myProjectId != null && myProjectId!!.artifactId != null) {
            nameLocationSettings.moduleName = myProjectId!!.artifactId
                ?.let { StringUtil.sanitizeJavaIdentifier(it) }.toString()
            if (myAggregatorProject != null) {
                nameLocationSettings.moduleContentRoot =
                    myAggregatorProject!!.directory + "/" + myProjectId!!.artifactId
            }
        }
        return super.modifySettingsStep(settingsStep)
    }

    override fun setupRootModel(rootModel: ModifiableRootModel) {
        val project = rootModel.project
        val root: VirtualFile? = createAndGetContentEntry()
        rootModel.addContentEntry(root!!)

        if (myJdk != null) {
            rootModel.sdk = myJdk
        } else {
            rootModel.inheritSdk()
        }
        MavenUtil.runWhenInitialized(project, DumbAwareRunnable {
            BackbaseExtensionProjectBuilder(this,myProjectId!!).configure(project, root, false)
        })
    }


    private fun createAndGetContentEntry(): VirtualFile? {
        val path = FileUtil.toSystemIndependentName(contentEntryPath!!)
        File(path).mkdirs()
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
    }

}