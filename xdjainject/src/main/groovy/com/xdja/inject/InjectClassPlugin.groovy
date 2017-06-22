package com.xdja.inject

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.xdja.inject.util.DataHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.xdja.inject.util.*

class InjectClassPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println ":applied InjectClass"
        project.extensions.create('InjectParam', InjectClassParams)
        Util.setProject(project)
        registerTransform(project)
        initDir(project);
        project.afterEvaluate {
            Log.setQuiet(project.InjectParam.keepQuiet);
            Log.setShowHelp(project.InjectParam.showHelp);
            Log.logHelp();
            Map<String, Map<String, Object>> taskMap = project.InjectParam.modifyTasks;
            if (taskMap != null && taskMap.size() > 0) {
                generateTasks(project, taskMap);
            }
            if (project.InjectParam.watchTimeConsume) {
                Log.info "watchTimeConsume enabled"
                project.gradle.addListener(new TimeListener())
            } else {
                Log.info "watchTimeConsume disabled"
            }
        }
    }

    def static registerTransform(Project project) {
//        def isApp = project.plugins.hasPlugin("com.android.application")
        BaseExtension android = project.extensions.getByType(BaseExtension)
        if (android instanceof LibraryExtension){
            DataHelper.ext.projectType = DataHelper.TYPE_LIB;
        } else if(android instanceof AppExtension){
            DataHelper.ext.projectType = DataHelper.TYPE_APP;
        } else {
            DataHelper.ext.projectType = -1
        }
        InjectTransform transform = new InjectTransform()
        android.registerTransform(transform)
    }

    /**
     *  初始化InjectClass的工作目录
     * @param project
     */
    static void initDir(Project project) {
        File InjectClassDir = new File(project.buildDir, "InjectClass")
        if (!InjectClassDir.exists()) {
            InjectClassDir.mkdir()
        }
        File tempDir = new File(InjectClassDir, "temp")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        DataHelper.ext.injectClassDir = InjectClassDir
        DataHelper.ext.injectClassTempDir = tempDir
    }

    /**
     *
     * @param project
     * @param taskMap
     * @return
     */

    def static generateTasks(Project project, Map<String, Map<String, Object>> taskMap) {
        project.task("InjectClass") << {
            Log.info("generate tasks")
            ModifyFiles.modify(taskMap)
        }
    }
}
