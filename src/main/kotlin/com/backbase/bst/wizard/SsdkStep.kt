package com.backbase.bst.wizard

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.idea.maven.model.MavenId
import javax.swing.JComponent

class ProjectId {
    var artifactId=""
    var groupId="com.backbase"
    var version="1.0.0"
    var ssdkVersion=""
}

class SsdkStep(
    private val backbaseModuleWizard: BackbaseProjectWizard,
    private val projectId: ProjectId = ProjectId(),
    private val ssdkVersions : List<String>) : ModuleWizardStep() {

    private val panel: DialogPanel = panel {

        row {
                textField().bindText(projectId::artifactId).label("ArtifactId ")
        }
        row {
            textField().bindText(projectId::groupId).label("GroupId ")
        }
        row {
            textField().bindText(projectId::version).label("Version ")
        }
        row {
            comboBox(ssdkVersions).label("Select SSDK Version")
        }
    }

    override fun getComponent(): JComponent {
        return panel
    }


    override fun updateDataModel() {
        panel.apply()
        backbaseModuleWizard.myProjectId = MavenId(projectId.groupId, projectId.artifactId, projectId.version)
        backbaseModuleWizard.ssdkMavenId = MavenId(backbaseModuleWizard.ssdkMavenId.groupId,
            backbaseModuleWizard.ssdkMavenId.artifactId,
            projectId.ssdkVersion
        )
    }
}