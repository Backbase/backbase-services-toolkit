package com.backbase.bst.wizard

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.BackbaseIcons
import com.backbase.bst.common.BackbaseSSDKModuleType
import com.backbase.bst.common.MavenTools
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.DefaultProjectFactory
import com.intellij.openapi.project.DumbAwareRunnable
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.apache.maven.artifact.versioning.ComparableVersion
import org.jetbrains.annotations.NonNls
import org.jetbrains.idea.maven.model.MavenArtifactInfo
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProject
import org.jetbrains.idea.maven.utils.MavenUtil
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import javax.swing.Icon

class BackbaseProjectWizard : ModuleBuilder(){

    var myAggregatorProject: MavenProject? = null

    var myProjectId: MavenId? = null

    override fun getNodeIcon(): Icon = BackbaseIcons.BACKBASE_PROJECT_LOGO

    override fun getModuleType(): ModuleType<*>? = BackbaseSSDKModuleType()

    override fun getPresentableName(): String = BackbaseBundle.message("wizard.project.display.name")

    override fun createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep>? {

        val versionSsdkArtifact = listVersionsSsdk()

        return arrayOf(
            SsdkStep(this, wizardContext, ProjectId(), versionSsdkArtifact)
        )
    }

    private fun listVersionsSsdk(): List<String> {
        val ssdkArtifact = MavenTools.findVersionsArtifact(
            DefaultProjectFactory.getInstance().defaultProject,
            "com.backbase.buildingblocks",
            "service-sdk-starter-core"
        )

       //add default versions
       ssdkArtifact.versions
           .addAll(generateSsdkVersions("13.3.1", "13.3.0", "13.2.2", "13.2.1", "13.2.0"))


        val versionSsdkArtifact = ssdkArtifact
            .versions
            .distinctBy { it.version }
            .sortedWith { a1, a2 ->
                ComparableVersion(a2.version).compareTo(ComparableVersion(a1.version))
            }
            .map { a -> a.version }

        return versionSsdkArtifact
    }

    private fun generateSsdkVersions(vararg versions: String ): List<MavenArtifactInfo> {
        return versions
            .map { MavenArtifactInfo("com.backbase.buildingblocks", "service-sdk-starter-core", it, null, null) }
    }

    override fun getBuilderId(): @NonNls String? {
        return javaClass.name
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        val nameLocationSettings = settingsStep.moduleNameLocationSettings
        if (nameLocationSettings != null && myProjectId != null && myProjectId!!.artifactId != null) {
            nameLocationSettings.moduleName = myProjectId!!.artifactId
                ?.let { StringUtil.sanitizeJavaIdentifier(it) }.toString()
            if (myAggregatorProject != null) {
                nameLocationSettings.moduleContentRoot =
                    myAggregatorProject!!.getDirectory() + "/" + myProjectId!!.getArtifactId()
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
            BackbaseMavenModuleBuilder(myProjectId!!).configure(project, root, false)
        })
    }


    private fun createAndGetContentEntry(): VirtualFile? {
        val path = FileUtil.toSystemIndependentName(contentEntryPath!!)
        File(path).mkdirs()
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
    }


}