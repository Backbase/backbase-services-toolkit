package com.backbase.bst.actions

import com.backbase.bst.common.Library
import com.backbase.bst.common.MavenTools
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.Nullable
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.server.MavenServerManager
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil
import kotlin.collections.HashMap

class AddAnyServiceSSDKModuleAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = MavenActionUtil.getProject(e.dataContext) ?: return

        val file: VirtualFile = MavenTools.findPomXml(e.dataContext) ?: return


        var libraries =  hashMapOf(

            "api" to Library("api", listOf("com.backbase.buildingblocks.api"), "Supplies consistent API functionality for your services."),
            "auxiliary-config" to Library("auxiliary-config", listOf("com.backbase.buildingblocks.auxiliary-config"),"Provides extra config required when building Docker images. "),
            "auth-security" to  Library("auth-security", listOf("com.backbase.buildingblocks.auth-security"),"Identifies the authentication status for a request through the system. An authentication status wraps the level and is allocated a system user."),
            "service-sdk-common-core" to Library( "service-sdk-common-core",listOf("com.backbase.buildingblocks.service-sdk-common-core"),"Common interfaces, annotations and utilities,  including generic API error exception classes mapped directly to HTTP  responses, such as BadRequestException and NotFoundException."),
            "building-blocks-common" to Library("building-blocks-common", listOf("com.backbase.buildingblocks.building-blocks-common"),"A shared library of common utility classes and interfaces for DBS services  such as: InternalRequest - ensures that once a transaction (request)   enters the system the contextual information  is not lost, and can be passed for latter processing. Models Annotations"),
            "behavior-extensions-camel" to Library("behavior-extensions-camel",listOf("com.backbase.buildingblocks.behavior-extensions-camel"),"Camel-based behavior extensions."),
            "communication" to Library("communication", listOf("com.backbase.buildingblocks.communication"),"An HTTP transport library that facilitates communication  between microservices."),
            "events" to Library( "events", listOf("com.backbase.buildingblocks.events"),"Enables eventing through an event bus, Spring Cloud Stream, and HTTPS support."),
            "jib-extras-extension" to Library("jib-extras-extension" ,listOf("com.backbase.buildingblocks.jib-extras-extension"),"Use this to move all JAR files from sub-directories in /app/extras to the base /app/extras directory."),

            "multi-tenancy" to Library("multi-tenancy", listOf("com.backbase.buildingblocks.multi-tenancy"),"Supports a single instance of software that runs on a server to serve multiple customers or tenants."),
            "multi-tenancy-liquibase" to Library("multi-tenancy-liquibase", listOf("com.backbase.buildingblocks.multi-tenancy-liquibase"),"Enables Liquibase to populate multi-tenanted databases on startup."),
            "persistence" to Library( "persistence",listOf("com.backbase.buildingblocks.persistence"),"Configures the Spring-provided persistence unit and Liquibase."),
            "production-support" to Library("production-support", listOf("com.backbase.buildingblocks.production-support"),"Enables you to perform status checks on your microservices. This includes health checks and metrics."),
            "validation" to Library("validation",listOf("com.backbase.buildingblocks.validation"),"Validate API requests inline with JSR standards."),
            "service-sdk-starter-eureka-client" to Library("service-sdk-starter-eureka-client", listOf("com.backbase.buildingblocks.service-sdk-starter-eureka-client"),"Configures and extends Eureka client side load balancer.  Provides the option to use Kubernetes service discovery instead of Eureka."),
            "service-sdk-starter-logging" to Library("service-sdk-starter-logging", listOf("com.backbase.buildingblocks.service-sdk-starter-logging"),"Configures logback formats for Spring applications."),
            "service-sdk-starter-mapping" to Library("service-sdk-starter-mapping", listOf("com.backbase.buildingblocks.service-sdk-starter-mapping"),"Supports MapStruct to manage Java Bean mapping."),
            "service-sdk-starter-security" to Library("service-sdk-starter-security", listOf("com.backbase.buildingblocks.service-sdk-starter-security"),"Configures Spring Security authentication using JSON Web Tokens. Provides optional scoped access,"),
            "service-sdk-test-utils" to Library("service-sdk-test-utils",listOf("com.backbase.buildingblocks.service-sdk-test-utils"),"Provides support for JWT-related testing.  This module has no dependencies on other Service SDK code."),
            "service-sdk-web-client" to Library("service-sdk-web-client", listOf("com.backbase.buildingblocks.service-sdk-web-client"),"Supports asynchronous calls to services through reactive processing.")
        )

        libraries = removeExistingModules(project, file, libraries) as HashMap<String, Library>

        val modulesDialog = AddAnyServiceSSDKModuleDialog(project, libraries)
        modulesDialog.show()

        if (modulesDialog.exitCode === DialogWrapper.CANCEL_EXIT_CODE) {
            return
        }

        addingMavenDependencies(project, file, e, libraries)

    }

    private fun removeExistingModules(project: Project, file: VirtualFile, libraries: Map<String, Library>): Map<String, Library> {
        return libraries.filter {  !areAlreadyInPom(project, file, it.value.artifact )}

    }

    private fun areAlreadyInPom(project: Project, file: VirtualFile, artifacts: List<String>): Boolean {
        return artifacts.all {
            isAlreadyInPom(project, file, it)
        }
    }

    private fun isAlreadyInPom(project: Project, file: VirtualFile, artifact: String): Boolean {
        val artifactId = getArtifactId(artifact)
        val groupId = getGroupId(artifact)
        val mavenId = MavenId(groupId, artifactId, "")
        return MavenTools.findDependencyOnBom(project, file, mavenId)
    }

    private fun getGroupId(artifact: String): String {
        return artifact.substringBeforeLast(".")
    }

    private fun getArtifactId(artifact: String): String {
        return artifact.substringAfterLast(".");
    }

    override fun update(e: AnActionEvent) {

        val file = MavenTools.findPomXml(e.dataContext)

        if(file == null) {
            e.presentation.isVisible = false
            return;
        }
    }

    private fun addingMavenDependencies(
        project: @Nullable Project,
        file: VirtualFile,
        e: AnActionEvent,
        libraries: HashMap<String, Library>
    ) {
        if (!MavenServerManager.getInstance().isUseMaven2) {

            actionEventDependencies(project, file, e.dataContext, libraries)
        }
        val mavenProjectManager = MavenProjectsManager.getInstance(project)
        mavenProjectManager.forceUpdateProjects(mavenProjectManager.projects)

        mavenProjectManager.waitForImportFinishCompletion()
    }

    private fun actionEventDependencies(project: Project, file: VirtualFile, dataContext: DataContext,
                                        libraries: HashMap<String, Library>) {

        WriteCommandAction.runWriteCommandAction(project) {

            libraries.filter { it.value.selected }.forEach { library ->

                library.value.artifact.forEach {
                    MavenTools.writeDependenciesOnPom(
                        project, file, dataContext,
                        listOf(
                            MavenId(getGroupId(it), getArtifactId(it), "")
                        )
                    )
                    Notification("Backbase notification group", "Adding dependency on pom", "Adding dependency $it on pom.xml",
                        NotificationType.INFORMATION).notify(project)
                }
            }

        }
    }

}