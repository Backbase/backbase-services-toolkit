import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.JTextArea

class UserMessagePanel(message: String) : JPanel() {
    init {
        layout = BorderLayout()
        val textArea = JTextArea(message)
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.isEditable = false
        textArea.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        add(textArea, BorderLayout.CENTER)
    }
}