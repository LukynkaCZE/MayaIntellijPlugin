package cz.lukynka.mayaplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import java.awt.datatransfer.StringSelection

class TideCodecGenerateAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val editor = action.getData(CommonDataKeys.EDITOR)
        if(editor == null) {
            Messages.showInfoMessage("Could not get CommandDataKeys.EDITOR", "Codec Generator")
            return
        }

        val psiFile = action.getData(CommonDataKeys.PSI_FILE) as? KtFile // CLASS DEF NOT FOUND WHEN RUNNING
        println("${action.getData(CommonDataKeys.PSI_FILE)} - ${action.getData(CommonDataKeys.PSI_FILE)!!::class.simpleName}")
        println("Classloader: ${action.getData(CommonDataKeys.PSI_FILE)!!::class.java.classLoader}")
        if(psiFile == null) {
            Messages.showInfoMessage("Could not get CommonDataKeys.PSI_FILE", "Codec Generator")
            return
        }

        val offset = editor.caretModel.offset
        val elementAtCaret = psiFile.findElementAt(offset) ?: return

        val ktClass = PsiTreeUtil.getParentOfType(elementAtCaret, KtClass::class.java)
        if (ktClass == null || !ktClass.isData()) {
            Messages.showInfoMessage("No data class found at caret.", "Codec Generator")
            return
        }

        val params = ktClass.primaryConstructorParameters.filter { it.hasValOrVar() }
        val fieldLines = params.map { param ->
            val nameRaw = param.name ?: return@map ""
            val name = nameRaw.toSnakeCase()
            val codecType = when (val type = param.typeReference?.text) {
                "Int" -> "Codec.INT"
                "Double" -> "Codec.DOUBLE"
                "Boolean" -> "Codec.BOOLEAN"
                "String" -> "Codec.STRING"
                "Byte" -> "Codec.BYTE"
                "Short" -> "Codec.SHORT"
                "Long" -> "Codec.LONG"
                "Float" -> "Codec.FLOAT"
                "ByteArray" -> "Codec.BYTE_ARRAY"
                "ByteBuf" -> "Codec.BYTE_ARRAY"
                "IntArray" -> "Codec.INT_ARAY"
                "LongArray" -> "Codec.LONG_ARRAY"
                "UUID" -> "Codec.UUID"
                else -> {
                    val classDeclaration = psiFile.declarations.filterIsInstance<KtClass>().find { it.name == type }
                    when {
                        classDeclaration?.isEnum() == true -> "Codec.enum<$type>()"
                        classDeclaration?.getCompanionObjects()?.any { co ->
                            co.declarations.any { it.name == "CODEC" }
                        } == true -> "$type.CODEC"

                        else -> "/* TODO: Add Codec for $type */"
                    }
                }
            }
            "\"$name\", $codecType, ${ktClass.name}::${name}"
        }

        val codecString = """
            val CODEC = StructCodec.of(
                ${fieldLines.joinToString(",\n    ")}
                , ::${ktClass.name}
            )
        """.trimIndent()
        val choice = Messages.showYesNoCancelDialog(
            action.project,
            "How would you like to use the generated CODEC?\n\n$codecString",
            "Codec Generator",
            "Insert at Caret",
            "Copy to Clipboard",
            "Cancel",
            Messages.getQuestionIcon()
        )

        when (choice) {
            Messages.YES -> {
                WriteCommandAction.runWriteCommandAction(action.project) {
                    editor.document.insertString(editor.caretModel.offset, codecString)
                }
            }

            Messages.NO -> {
                CopyPasteManager.getInstance().setContents(StringSelection(codecString))
            }

            else -> {}
        }
    }

    private fun String.toSnakeCase(): String =
        replace(Regex("([a-z])([A-Z]+)"), "$1_$2")
            .replace("-", "_")
            .lowercase()
}