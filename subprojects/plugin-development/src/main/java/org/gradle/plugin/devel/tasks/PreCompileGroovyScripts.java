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

package org.gradle.plugin.devel.tasks;

import com.google.common.collect.Sets;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.initialization.ClassLoaderScope;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.configuration.CompileOperationFactory;
import org.gradle.configuration.PreCompiledScriptTarget;
import org.gradle.configuration.ScriptTarget;
import org.gradle.groovy.scripts.internal.BuildScriptData;
import org.gradle.groovy.scripts.internal.CompileOperation;
import org.gradle.groovy.scripts.internal.DefaultScriptCompilationHandler;
import org.gradle.groovy.scripts.internal.ScriptCompilationHandler;
import org.gradle.initialization.ClassLoaderScopeRegistry;
import org.gradle.internal.Actions;
import org.gradle.model.dsl.internal.transform.ClosureCreationInterceptingVerifier;
import org.gradle.plugin.devel.tasks.internal.PreCompiledScript;
import org.gradle.plugin.management.internal.PluginRequests;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public class PreCompileGroovyScripts extends DefaultTask {
    private Set<PreCompiledScript> scriptPlugins = Sets.newHashSet();
    private final DirectoryProperty classesDir;
    private final DirectoryProperty metadataDir;
    private FileCollection classpath;

    public PreCompileGroovyScripts() {
        this.classesDir = getProject().getObjects().directoryProperty();
        this.metadataDir = getProject().getObjects().directoryProperty();
    }

    @Internal
    public Set<PreCompiledScript> getScriptPlugins() {
        return scriptPlugins;
    }

    @InputFiles
    Set<File> getScriptFiles() {
        return scriptPlugins.stream().map(scriptPlugin -> scriptPlugin.getScriptFile()).collect(Collectors.toSet());
    }

    @OutputDirectory
    public DirectoryProperty getClassesDir() {
        return classesDir;
    }

    @OutputDirectory
    public DirectoryProperty getMetadataDir() {
        return metadataDir;
    }

    @Classpath
    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @TaskAction
    void compileScripts() {
        ScriptCompilationHandler scriptCompilationHandler = getServices().get(DefaultScriptCompilationHandler.class);
        ClassLoaderScopeRegistry classLoaderScopeRegistry = getServices().get(ClassLoaderScopeRegistry.class);

        // TODO need to include classpath
        ClassLoaderScope classLoaderScope = classLoaderScopeRegistry.getCoreAndPluginsScope().createChild("pre-compiled-scripts");
        classLoaderScope.lock();
        ClassLoader classLoader = classLoaderScope.getExportClassLoader();

        CompileOperationFactory compileOperationFactory = getServices().get(CompileOperationFactory.class);
        File classesDirValue = getClassesDir().getAsFile().get();
        File metadataDirValue = getMetadataDir().getAsFile().get();

        for (PreCompiledScript scriptPlugin : scriptPlugins) {
            ScriptTarget scriptTarget = new PreCompiledScriptTarget();

            // 1st pass, compile plugin requests and store metadata
            CompileOperation<PluginRequests> pluginRequestsCompileOperation = compileOperationFactory.getPluginRequestsCompileOperation(scriptPlugin.getSource(), scriptTarget);
            scriptCompilationHandler.compileToDir(scriptPlugin.getSource(), classLoader, scriptPlugin.getPluginClassesDir(classesDirValue), scriptPlugin.getPluginMetadataDir(metadataDirValue), pluginRequestsCompileOperation, scriptTarget.getScriptClass(), Actions.doNothing());

            // 2nd pass, compile build script and store metadata
            CompileOperation<BuildScriptData> buildScriptDataCompileOperation = compileOperationFactory.getBuildScriptDataCompileOperation(scriptPlugin.getSource(), scriptTarget);
            scriptCompilationHandler.compileToDir(scriptPlugin.getSource(), classLoader, scriptPlugin.getBuildScriptClassesDir(classesDirValue), scriptPlugin.getBuildScriptMetadataDir(metadataDirValue), buildScriptDataCompileOperation, scriptTarget.getScriptClass(), ClosureCreationInterceptingVerifier.INSTANCE);
        }
    }
}
