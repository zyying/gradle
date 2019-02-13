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

package org.gradle.plugin.devel.tasks.internal;

import org.gradle.api.internal.initialization.ClassLoaderIds;
import org.gradle.api.internal.initialization.ClassLoaderScope;
import org.gradle.api.internal.initialization.ScriptHandlerFactory;
import org.gradle.api.internal.initialization.ScriptHandlerInternal;
import org.gradle.api.internal.initialization.loadercache.ClassLoaderId;
import org.gradle.api.internal.plugins.PluginManagerInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.configuration.CompileOperationFactory;
import org.gradle.configuration.DefaultScriptTarget;
import org.gradle.configuration.ScriptTarget;
import org.gradle.groovy.scripts.BasicScript;
import org.gradle.groovy.scripts.ScriptRunner;
import org.gradle.groovy.scripts.ScriptSource;
import org.gradle.groovy.scripts.TextResourceScriptSource;
import org.gradle.groovy.scripts.internal.BuildScriptData;
import org.gradle.groovy.scripts.internal.CompileOperation;
import org.gradle.groovy.scripts.internal.ScriptCompilationHandler;
import org.gradle.groovy.scripts.internal.ScriptRunnerFactory;
import org.gradle.internal.resource.UriTextResource;
import org.gradle.plugin.management.internal.PluginRequestInternal;
import org.gradle.plugin.management.internal.PluginRequests;
import org.gradle.plugin.management.internal.autoapply.AutoAppliedPluginHandler;
import org.gradle.plugin.use.internal.PluginRequestApplicator;

import java.io.File;

public class PreCompiledScriptRunner {
    public static void apply(ProjectInternal project,
                      File scriptFile,
                      File classesDir,
                      File metadataDir) {
        CompileOperationFactory compileOperationFactory = project.getServices().get(CompileOperationFactory.class);
        ScriptCompilationHandler scriptCompilationHandler = project.getServices().get(ScriptCompilationHandler.class);
        ScriptRunnerFactory scriptRunnerFactory = project.getServices().get(ScriptRunnerFactory.class);
        ScriptHandlerFactory scriptHandlerFactory = project.getServices().get(ScriptHandlerFactory.class);
        AutoAppliedPluginHandler autoAppliedPluginHandler = project.getServices().get(AutoAppliedPluginHandler.class);
        PluginRequestApplicator pluginRequestApplicator = project.getServices().get(PluginRequestApplicator.class);

        // TODO is this the right scope?
        ClassLoaderScope classLoaderScope = project.getClassLoaderScope().createChild("pre-compiled-script");
        classLoaderScope.lock();
        ClassLoader classLoader = classLoaderScope.getExportClassLoader();

        ScriptSource scriptSource = new TextResourceScriptSource(new UriTextResource("script", scriptFile));
        PreCompiledScript scriptPlugin = new PreCompiledScript(scriptSource);
        ScriptTarget scriptTarget = new DefaultScriptTarget(project);

        // Pass 1, apply plugins
        CompileOperation<PluginRequests> pluginRequestsCompileOperation = compileOperationFactory.getPluginRequestsCompileOperation(scriptPlugin.getSource(), scriptTarget);
        ClassLoaderId classLoaderId = ClassLoaderIds.buildScript(scriptPlugin.getSource().getFileName(), pluginRequestsCompileOperation.getId());
        ScriptRunner<? extends BasicScript, PluginRequests> initialRunner = scriptRunnerFactory.create(scriptCompilationHandler.loadFromDir(scriptPlugin.getSource(), scriptPlugin.getSource().getResource().getContentHash(), classLoader, scriptPlugin.getPluginClassesDir(classesDir), scriptPlugin.getPluginMetadataDir(metadataDir), pluginRequestsCompileOperation, scriptTarget.getScriptClass(), classLoaderId), scriptSource, classLoader);
        // TODO should use script services here
        initialRunner.run(project, project.getServices());
        PluginRequests initialPluginRequests = initialRunner.getData();
        PluginRequests mergedPluginRequests = autoAppliedPluginHandler.mergeWithAutoAppliedPlugins(initialPluginRequests, project);

        ScriptHandlerInternal scriptHandler = scriptHandlerFactory.create(scriptSource, classLoaderScope);
        PluginManagerInternal pluginManager = scriptTarget.getPluginManager();
        pluginRequestApplicator.applyPlugins(mergedPluginRequests, scriptHandler, pluginManager, classLoaderScope);

        // Pass 2, execute script
        CompileOperation<BuildScriptData> buildScriptDataCompileOperation = compileOperationFactory.getBuildScriptDataCompileOperation(scriptPlugin.getSource(), scriptTarget);
        ScriptRunner<? extends BasicScript, BuildScriptData> runner = scriptRunnerFactory.create(scriptCompilationHandler.loadFromDir(scriptSource, scriptSource.getResource().getContentHash(), classLoader, scriptPlugin.getBuildScriptClassesDir(classesDir), scriptPlugin.getBuildScriptMetadataDir(metadataDir),buildScriptDataCompileOperation, scriptTarget.getScriptClass(), classLoaderId), scriptSource, classLoader);
        if (runner.getRunDoesSomething()) {
            // TODO should use script services here
            runner.run(project, project.getServices());
        }
    }
}
