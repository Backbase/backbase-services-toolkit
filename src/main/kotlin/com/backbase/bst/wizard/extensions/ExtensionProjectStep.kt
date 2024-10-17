package com.backbase.bst.wizard.extensions

import com.backbase.bst.common.SsdkUtils
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.idea.maven.model.MavenId
import javax.swing.DefaultComboBoxModel
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

        row ("  GroupId "){
            textField().bindText(::groupId).align(Align.FILL).comment("The groupId of the generated Maven project")
        }
        row ("  ArtifactId "){
            textField().bindText(::artifactId).align(Align.FILL)
                .comment("The artifactId of the generated Maven project")
        }
        row ("  Version "){
            textField().bindText(::version).align(Align.FILL)
                .comment("The version number of the generated Maven project")
        }
        row ("  Extension Version "){
            comboBox(DefaultComboBoxModel(SsdkUtils.listVersionsSsdk().toTypedArray()))
                .comment("Backbase service extension starter parent version ")
                .bindItem(::ssdkVersion,
                    { selectedOption -> ssdkVersion = selectedOption!! })
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
        behaviourExtensionsProjectWizard.myProjectId = MavenId(groupId, artifactId, version)
    }
}