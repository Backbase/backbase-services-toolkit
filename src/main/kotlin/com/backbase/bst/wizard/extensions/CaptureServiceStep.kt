package com.backbase.bst.wizard.extensions

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import javax.swing.JComponent


class CaptureServiceStep(
    private val behaviourExtensionsProjectWizard: BehaviourExtensionsProjectWizard
) : ModuleWizardStep() {

    private var serviceGroupId: String = ""
    private var serviceArtifactId: String = ""
    private var bbVersion: String = "2023.02-LTS"

    private val panel: DialogPanel = panel {

        row(separated = true) {
            label("ServiceGroupId")
                .comment("The groupId of the service you are creating an extension for")
            textField(::serviceGroupId)
        }

        row(separated = true) {
            label("ServiceArtifactId")
                .comment("The artifactId of the service you are creating an extension for")
            textField(::serviceArtifactId)

        }
        row(separated = true) {
            label("BBVersion")
                .comment("The version number of Backbase you want to create an extension for")
            textField(::bbVersion)

        }
        noteRow("Check <a href=\"https://community.backbase.com/\">Community</a> for service details")
    }

    override fun getComponent(): JComponent {
        return panel
    }

    override fun updateDataModel() {
        panel.apply()
        behaviourExtensionsProjectWizard.serviceGroupId = serviceGroupId
        behaviourExtensionsProjectWizard.serviceArtifactId = serviceArtifactId
        behaviourExtensionsProjectWizard.bbVersion = bbVersion
    }
}