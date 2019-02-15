/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.plugin.devel.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.groovy.scripts.TextResourceScriptSource;
import org.gradle.initialization.layout.BuildLayout;
import org.gradle.internal.resource.UriTextResource;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.plugin.devel.tasks.GenerateScriptPluginAdapters;
import org.gradle.plugin.devel.tasks.PreCompileGroovyScripts;
import org.gradle.plugin.devel.tasks.internal.PreCompiledScript;

import java.util.Set;
import java.util.stream.Collectors;

import static org.gradle.plugin.devel.tasks.internal.PreCompiledScript.SCRIPT_PLUGIN_EXTENSION;

public class PreCompiledScriptPluginsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaGradlePluginPlugin.class);

        GradlePluginDevelopmentExtension pluginExtension = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);

        project.afterEvaluate(p -> {
            exposeScriptsAsPlugins(pluginExtension, project.getTasks(), project.getLayout());
        });
    }

    void declarePluginMetadata(GradlePluginDevelopmentExtension pluginExtension, Set<PreCompiledScript> scriptPlugins) {
        pluginExtension.plugins(pluginDeclarations -> {
            for (PreCompiledScript scriptPlugin : scriptPlugins) {
                pluginDeclarations.create(scriptPlugin.getId(), pluginDeclaration -> {
                    pluginDeclaration.setImplementationClass(scriptPlugin.getId() + "Plugin");
                    pluginDeclaration.setId(scriptPlugin.getId());
                });
            }
        });
    }

    void exposeScriptsAsPlugins(GradlePluginDevelopmentExtension pluginExtension, TaskContainer tasks, ProjectLayout buildLayout) {
        FileTree scriptPluginFiles = pluginExtension.getPluginSourceSet().getAllSource().matching(patternFilterable -> patternFilterable.include("**/*" + SCRIPT_PLUGIN_EXTENSION));

        Set<PreCompiledScript> scriptPlugins = scriptPluginFiles.getFiles().stream()
            .map(file -> new PreCompiledScript(new TextResourceScriptSource(new UriTextResource("script", file))))
            .collect(Collectors.toSet());

        declarePluginMetadata(pluginExtension, scriptPlugins);

        Provider<Directory> baseMetadataDir = buildLayout.getBuildDirectory().dir("compiled-scripts/groovy-dsl-plugins/groovy-metadata");
        Provider<Directory> baseClassesDir = buildLayout.getBuildDirectory().dir("compiled-scripts/groovy-dsl-plugins/groovy");

        TaskProvider<PreCompileGroovyScripts> preCompileTask = tasks.register("preCompileScriptPlugins", PreCompileGroovyScripts.class, preCompileGroovyScripts -> {
            preCompileGroovyScripts.getScriptPlugins().addAll(scriptPlugins);
            preCompileGroovyScripts.setClasspath(pluginExtension.getPluginSourceSet().getCompileClasspath());
            preCompileGroovyScripts.getClassesDir().set(baseClassesDir);
            preCompileGroovyScripts.getMetadataDir().set(baseMetadataDir);
        });

        pluginExtension.getPluginSourceSet().getOutput().dir(preCompileTask.flatMap(task -> task.getClassOutputDir()));
        pluginExtension.getPluginSourceSet().getOutput().dir(preCompileTask.flatMap(task -> task.getMetadataDir()));

        Provider<Directory> generatedClassesDir = buildLayout.getBuildDirectory().dir("generated-classes/groovy-dsl-plugins/java");
        TaskProvider<GenerateScriptPluginAdapters> generateAdaptersTask = tasks.register("generateScriptPluginAdapters", GenerateScriptPluginAdapters.class, generateScriptPluginAdapters -> {
            generateScriptPluginAdapters.getScriptPlugins().addAll(scriptPlugins);
            generateScriptPluginAdapters.getMetadataDir().set(baseMetadataDir);
            generateScriptPluginAdapters.getClassesDir().set(baseClassesDir);
            generateScriptPluginAdapters.getGeneratedClassesDir().set(generatedClassesDir);
        });

        pluginExtension.getPluginSourceSet().getJava().srcDir(generateAdaptersTask.flatMap(task -> task.getGeneratedClassesDir()));
    }
}
