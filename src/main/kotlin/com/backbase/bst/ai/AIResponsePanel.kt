import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JEditorPane
import javax.swing.JPanel

class AIResponsePanel(message: String) : JPanel() {
    init {
        layout = BorderLayout()
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val document = parser.parse(message)
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        text-align: justify;
                    }
                    pre {
                        position: relative;
                        padding: 10px;
                        background: #f5f5f5;
                        border: 1px solid #ddd;
                    }
                    pre code {
                        display: block;
                    }
                </style>
                
                
            </head>
            <body>
                <h><b>Backbase AI</b></h> 
                ${renderer.render(document)}
            </body>
            </html>
        """.trimIndent()
        System.out.println(htmlContent);
        val editorPane = JEditorPane("text/html", htmlContent)
        editorPane.isEditable = false
        editorPane.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        add(editorPane, BorderLayout.CENTER)
    }
}