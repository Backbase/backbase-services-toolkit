package com.backbase.bst.actions


import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DefaultProjectFactory
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.DumbServiceImpl
import org.jetbrains.idea.maven.indices.MavenIndicesManager


class LoadIndexesActivity: PreloadingActivity() {
    override fun preload(indicator: ProgressIndicator) {
        Notification("Backbase notification group", "Initial plugin loading", "Ready to read indexes",
            NotificationType.INFORMATION).notify(DefaultProjectFactory.getInstance().defaultProject)

        val indices = Runnable {
            val size = MavenIndicesManager.getInstance().indices.size
            Notification("Backbase notification group", "Initial plugin loading", "This is happening. Index size $size",
                NotificationType.INFORMATION).notify(DefaultProjectFactory.getInstance().defaultProject)
        }
        DumbServiceImpl(DefaultProjectFactory.getInstance().defaultProject).smartInvokeLater(indices)

        Notification("Backbase notification group", "Initial plugin loading", "Indexes loaded",
            NotificationType.INFORMATION).notify(DefaultProjectFactory.getInstance().defaultProject)
    }
}