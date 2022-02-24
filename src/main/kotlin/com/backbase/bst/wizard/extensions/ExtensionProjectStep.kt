package com.backbase.bst.wizard.extensions

import com.backbase.bst.common.SsdkUtils
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent


class ExtensionProjectStep(
    private val behaviourExtensionsProjectWizard: BehaviourExtensionsProjectWizard,
    private val wizardContext: WizardContext
) : ModuleWizardStep() {

    private var groupId: String = "com.backbase"
    private var artifactId: String = "example-extention"
    private var version: String = "1.0.0-SNAPSHOT"
    private var ssdkVersion: String = ""

    private val panel: DialogPanel = panel {

        row(separated = true) {
            label("GroupId")
                .comment("The groupId of the generated Maven project")
            textField(::groupId)
        }
        row(separated = true) {
            label("ArtifactId")
                .comment("The artifactId of the generated Maven project")
            textField(::artifactId)

        }
        row(separated = true) {
            label("Version")
                .comment("The version number of the generated Maven project")
            textField(::version)

        }
        row(separated = true) {
            label("Extension Version")
                .comment("backbase-service-extension-starter-parent version ")
            comboBox(
                DefaultComboBoxModel(SsdkUtils.listVersionsSsdk().toTypedArray()),
                ::ssdkVersion
            )

        }
    }

    override fun updateStep() {
//        groupId = behaviourExtensionsProjectWizard.serviceGroupId
//        artifactId = behaviourExtensionsProjectWizard.serviceArtifactId + "-extension"
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
        wizardContext
    }
}