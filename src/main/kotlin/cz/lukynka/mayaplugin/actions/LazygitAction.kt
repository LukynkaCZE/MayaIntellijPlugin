package cz.lukynka.mayaplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.File
import java.io.IOException

class LazygitAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val baseDir = project.baseDir ?: return
        val projectPath = baseDir.path
        val pb = ProcessBuilder("cmd.exe", "/c", "start", "pwsh.exe")
        pb.directory(File(projectPath))

        try {
            pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}

class RunCommandInExternalTerminalAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val baseDir = project.baseDir ?: return
        val projectPath = baseDir.path
        val cmdContent = File("$projectPath/cmd.txt")
        if(!cmdContent.exists()) return
        val content = cmdContent.readText()

        val pb = ProcessBuilder("cmd.exe", "/c", "start", "wt.exe", "new-tab",
            "PowerShell", "-NoExit", "-Command", content)
        pb.directory(File(projectPath))

        try {
            pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}