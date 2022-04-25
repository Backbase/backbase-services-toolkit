package com.backbase.bst.wizard

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import org.jetbrains.idea.maven.model.MavenId
import javax.swing.JComponent

class ProjectId {
    var artifactId=""
    var groupId="com.backbase"
    var version="1.0.0"
    var ssdkVersion="14.1.0"
}

class SsdkStep(
    private val backbaseModuleWizard: BackbaseProjectWizard,
    private val projectId: ProjectId = ProjectId()) : ModuleWizardStep() {

    private val panel: DialogPanel = panel {

        row {
            cell(isFullWidth = true) {
                label("ArtifactId ")
                textField(projectId::artifactId)
            }
        }
        row {
            cell(isFullWidth = true) {
                label("GroupId   ")
                textField(projectId::groupId)
            }
        }
        row {
            cell(isFullWidth = true) {
                label("Version    ")
                textField(projectId::version)
            }
        }
        row {
            cell(isFullWidth = true) {
                label("SSDK Version")
                textField(projectId::ssdkVersion)
            }
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