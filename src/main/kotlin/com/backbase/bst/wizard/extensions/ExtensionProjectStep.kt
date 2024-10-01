package com.backbase.bst.wizard.extensions

import com.backbase.bst.common.SsdkUtils
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.idea.maven.model.MavenId
import javax.swing.JComponent


class ExtensionProjectStep(
    private val behaviourExtensionsProjectWizard: BehaviourExtensionsProjectWizard,
    private val wizardContext: WizardContext
) : ModuleWizardStep() {

    private var groupId: String = "com.backbase"
    private var artifactId: String = "example-extension"
    private var version: String = "1.0.0-SNAPSHOT"
    private var ssdkVersion: String = ""

    private val panel: DialogPanel = panel {

        row{
            textField().bindText(::groupId).label("GroupId").comment("The groupId of the generated Maven project")
        }
        row {
            textField().bindText(::artifactId).label("ArtifactId").comment("The artifactId of the generated Maven project")
        }
        row {
            textField().bindText(::version).label("Version").comment("The version number of the generated Maven project")
        }
        row {
            comboBox(SsdkUtils.listVersionsSsdk()).label("Extension Version").comment("Backbase service extension starter parent version ")// TODO
        }
    }

    override fun getComponent(): JComponent {
        return panel
    }


    override fun updateDataModel() {
        panel.apply()
        behaviourExtensionsProjectWizard.groupId = groupId
        behaviourExtensionsProjectWizard.artifactId = artifactId
        behaviourExtensionsProjectWizard.version = version
        behaviourExtensionsProjectWizard.ssdkVersion = ssdkVersion
        wizardContext.projectName = artifactId
        behaviourExtensionsProjectWizard.myProjectId = MavenId(groupId,artifactId,version)
    }
}