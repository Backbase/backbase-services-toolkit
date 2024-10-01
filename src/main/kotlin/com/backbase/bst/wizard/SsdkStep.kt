package com.backbase.bst.wizard


import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.idea.maven.model.MavenId
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class ProjectId {
    var artifactId = ""
    var groupId = "com.backbase"
    var version = "1.0.0"
    var ssdkVersion = ""
}


class SsdkStep(
    private val backbaseModuleWizard: BackbaseProjectWizard,
    private val projectId: ProjectId = ProjectId(),
    private val ssdkVersions: List<String>
) : ModuleWizardStep() {

    private val panel: DialogPanel = panel {

        row("ArtifactId ") {
            textField().bindText(projectId::artifactId)
        }
        row("GroupId ") {
            textField().bindText(projectId::groupId)
        }
        row("Version ") {
            textField().bindText(projectId::version)
        }
        // Row for SSDK Version ComboBox
        row("SSDK Version:") {
            comboBox(DefaultComboBoxModel(ssdkVersions.toTypedArray()))
            //.bindSelectedItemIn(projectId::ssdkVersion)
        }

    }


    override fun getComponent(): JComponent {
        return panel
    }


    override fun updateDataModel() {
        panel.apply()
        backbaseModuleWizard.myProjectId = MavenId(projectId.groupId, projectId.artifactId, projectId.version)
        backbaseModuleWizard.ssdkMavenId = MavenId(
            backbaseModuleWizard.ssdkMavenId.groupId,
            backbaseModuleWizard.ssdkMavenId.artifactId,
            projectId.ssdkVersion
        )
    }


}