package cz.lukynka.mayaplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class MyStartupActivity: ProjectActivity {

    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            val manager = RecentFilesManager(project)
            val contains = listOf(".kt", ".java", ".properties", ".gradle.kts", ".toml", ".json")
            val blacklist = listOf("out/", "build/", "run/")

            val filesToAdd = mutableListOf<String>()
            getAllFilesInProject(project).forEach all@{
                println(it.path)
                contains.forEach containsLoop@{ contains ->
                    if (it.path.contains(contains)) {
                        blacklist.forEach { blacklist -> if (it.path.contains(blacklist)) return@all }
                        filesToAdd.add(it.path)
                    }
                }

            }
            filesToAdd.forEach(manager::addFileToRecent)
        }
    }
}