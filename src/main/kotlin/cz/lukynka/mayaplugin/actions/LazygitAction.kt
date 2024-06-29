package cz.lukynka.mayaplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.IOException

class LazygitAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val baseDir = project.baseDir ?: return
        val projectPath = baseDir.path
        val command = "cmd.exe /c start wt.exe new-tab -p \"Command Prompt\" -d \"$projectPath\" lazygit"

        try {
            Runtime.getRuntime().exec(command)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}