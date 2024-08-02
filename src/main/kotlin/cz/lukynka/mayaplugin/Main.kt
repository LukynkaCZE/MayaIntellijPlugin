package cz.lukynka.mayaplugin

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager

object Main {
    //uh hi? :3


}

class RecentFilesManager(private val project: Project) {

    fun getRecentFiles(): List<VirtualFile> {
        return EditorHistoryManager.getInstance(project).fileList
    }

    fun addFileToRecent(filePath: String) {
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$filePath")
        virtualFile?.let {
            FileEditorManager.getInstance(project).openFile(it, false)
            FileEditorManager.getInstance(project).closeFile(it)
        } ?: println("File not found: $filePath")
    }
}

fun getAllFilesInProject(project: Project): List<VirtualFile> {
    val fileIndex = ProjectFileIndex.getInstance(project)
    val allFiles = mutableListOf<VirtualFile>()

    fileIndex.iterateContent { virtualFile ->
        if (!virtualFile.isDirectory) {
            allFiles.add(virtualFile)
        }
        true
    }

    return allFiles
}