package com.backbase.bst.ai

import AIResponsePanel
import UserMessagePanel
import com.azure.core.credential.AzureKeyCredential
import com.azure.search.documents.SearchClientBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import java.awt.Component
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile
import com.azure.search.documents.models.SearchOptions
import com.azure.search.documents.util.SearchPagedIterable
import javax.swing.*

class ChatToolWindowFactory : ToolWindowFactory {
    private val client = OkHttpClient()
    private val apiUrl = "https://oai.stg.azure.backbase.eu/openai/deployments/gpt-4o/chat/completions?api-version=2024-02-01"
    private val apiKey = "hereyourkey"
    private val messageHistory = mutableListOf<JsonObject>()


    private val azureEndpoint = "https://backbase-io-ai-search.search.windows.net";
    private val azureIndexName = "hereyourindex";
    private val azureApiKey = "hereyourkey";

    //initialize messageHistory
    init {
        messageHistory.add(JsonObject().apply {
            addProperty("role", "system")
            addProperty(
                "content",
                """
                You are an Backbase AI assistant working inside a Intellij as a coding assistant. 
                You will try to answer using first Backbase context. Service SDK (SSDK) is the library used by most of the services.
                You focus is the Backbase platform and the Backbase Service SDK. e.g. Events are part of Service SDK and are used to communicate between services.
                Developers love explanations and examples. If you can provide code examples, that would be great. 
                If you cannot find anything in the context of Backbase, just say that you don't have the information. 
                If present, use currentFile tag represent the current file that the developer is currently visualizing in Intellij.
                """.trimMargin()
            )
        })
    }

    fun getAzureSearchResults(query: String): SearchPagedIterable {
        val searchClient = SearchClientBuilder()
            .endpoint(azureEndpoint)
            .indexName(azureIndexName)
            .credential(AzureKeyCredential(azureApiKey))
            .buildClient();
        val options = SearchOptions();
        options.setTop(5);
        return searchClient.search(query, options, null)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatPanel = JPanel(BorderLayout())
        val chatArea = JPanel()
        chatArea.layout = BoxLayout(chatArea, BoxLayout.Y_AXIS)
        val chatScrollPane = JBScrollPane(chatArea)
        val inputField = JTextField()
        val sendButton = JButton("Send")

        sendButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent?) {
                val message = inputField.text
                if (message.isNotEmpty()) {
                    val userMessagePanel = UserMessagePanel("You: $message")
                    userMessagePanel.alignmentX = Component.LEFT_ALIGNMENT
                    chatArea.add(userMessagePanel)
                    inputField.text = ""

                    // Get the currently selected file
                    val project = ProjectManager.getInstance().openProjects.firstOrNull()
                    val selectedFile: VirtualFile? = project?.let { FileEditorManager.getInstance(it).selectedFiles.firstOrNull() }

                    // Read the content of the selected file
                    val fileContent = selectedFile?.contentsToByteArray()?.toString(Charsets.UTF_8)

                    // Construct the updated message
                    var updatedMessage = buildString {
                        append("<prompt>$message</prompt>")
                        if (fileContent != null) {
                            append("<currentFile>$fileContent</currentFile>")
                        }
                    }
                    //get the search results from azure search for RAG
                    val searchResults = getAzureSearchResults(updatedMessage)
                    //for each search result, add to the updated message
                    searchResults.forEach {
                        updatedMessage += "<backbaseDoc>${it.getDocument(Map::class.java).get("content")}</backbaseDoc>"
                    }
                    // Add the message to the history
                    messageHistory.add(JsonObject().apply {
                        addProperty("role", "user")
                        addProperty("content", updatedMessage)
                    })


                    sendMessageToApi(chatArea)
                    chatArea.revalidate()
                }
            }
        })

        val inputPanel = JPanel(BorderLayout())
        inputPanel.add(inputField, BorderLayout.CENTER)
        inputPanel.add(sendButton, BorderLayout.EAST)

        chatPanel.add(chatScrollPane, BorderLayout.CENTER)
        chatPanel.add(inputPanel, BorderLayout.SOUTH)

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(chatPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun sendMessageToApi(chatArea: JPanel) {
        val json = JsonObject().apply {
            add("messages", Gson().toJsonTree(messageHistory))
            addProperty("temperature", 0.7)
            addProperty("top_p", 0.95)
            addProperty("frequency_penalty", 0)
            addProperty("presence_penalty", 0)
            addProperty("max_tokens", 800)
            add("stop", null)
            addProperty("stream", false)
        }

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Content-Type", "application/json")
            .addHeader("Api-Key", apiKey)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorPanel = AIResponsePanel("Error: ${e.message}")
                errorPanel.alignmentX = Component.LEFT_ALIGNMENT
                chatArea.add(errorPanel)
                chatArea.revalidate()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val responseJson = Gson().fromJson(responseBody, JsonObject::class.java)
                    val reply = responseJson.getAsJsonArray("choices")
                        .get(0).asJsonObject
                        .getAsJsonObject("message")
                        .get("content").asString

                    // Add the AI's reply to the message history
                    messageHistory.add(JsonObject().apply {
                        addProperty("role", "assistant")
                        addProperty("content", reply)
                    })

                    val aiResponsePanel = AIResponsePanel("$reply")
                    aiResponsePanel.alignmentX = Component.LEFT_ALIGNMENT
                    chatArea.add(aiResponsePanel)
                    chatArea.revalidate()
                }
            }
        })
    }
}