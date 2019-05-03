/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.nativeplatform.test.cpp.internal;

import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.cpp.CppComponent;
import org.gradle.language.cpp.CppPlatform;
import org.gradle.language.cpp.internal.DefaultCppBinary;
import org.gradle.language.cpp.internal.DefaultCppComponent;
import org.gradle.language.cpp.internal.NativeVariantIdentity;
import org.gradle.language.nativeplatform.internal.ConfigurableComponentWithExecutable;
import org.gradle.language.nativeplatform.internal.Names;
import org.gradle.nativeplatform.tasks.InstallExecutable;
import org.gradle.nativeplatform.tasks.LinkExecutable;
import org.gradle.nativeplatform.test.cpp.CppTestExecutable;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import javax.inject.Inject;
import java.util.concurrent.Callable;

public class DefaultCppTestExecutable extends DefaultCppBinary implements CppTestExecutable, ConfigurableComponentWithExecutable {
    private final Provider<CppComponent> testedComponent;
    private final RegularFileProperty executableFile;
    private final Property<Task> executableFileProducer;
    private final DirectoryProperty installationDirectory;
    private final TaskProvider<InstallExecutable> installTask;
    private final TaskProvider<LinkExecutable> linkTask;
    private final TaskProvider<RunTestExecutable> runTask;
    private final ConfigurableFileCollection outputs;
    private final RegularFileProperty debuggerExecutableFile;

    @Inject
    public DefaultCppTestExecutable(Names names, ObjectFactory objects, Provider<String> baseName,
                                    FileCollection sourceFiles, FileCollection componentHeaderDirs,
                                    TaskContainer tasks, ConfigurationContainer configurations, Configuration implementation,
                                    Provider<CppComponent> testedComponent,
                                    CppPlatform targetPlatform, NativeToolChainInternal toolChain, PlatformToolProvider platformToolProvider,
                                    NativeVariantIdentity identity) {
        super(names, objects, baseName, sourceFiles, componentHeaderDirs, tasks, configurations, implementation, targetPlatform, toolChain, platformToolProvider, identity);
        this.testedComponent = testedComponent;
        this.executableFile = objects.fileProperty();
        this.executableFileProducer = objects.property(Task.class);
        this.debuggerExecutableFile = objects.fileProperty();
        this.installationDirectory = objects.directoryProperty();
        this.linkTask = tasks.register(names.getTaskName("link"), LinkExecutable.class);
        this.installTask = tasks.register(names.getTaskName("install"), InstallExecutable.class);
        this.outputs = objects.fileCollection();
        this.runTask = tasks.register(names.getTaskName("run"), RunTestExecutable.class);
    }

    @Override
    public ConfigurableFileCollection getOutputs() {
        return outputs;
    }

    @Override
    public RegularFileProperty getExecutableFile() {
        return executableFile;
    }

    @Override
    public Property<Task> getExecutableFileProducer() {
        return executableFileProducer;
    }

    @Override
    public Property<RegularFile> getDebuggerExecutableFile() {
        return debuggerExecutableFile;
    }

    @Override
    public DirectoryProperty getInstallDirectory() {
        return installationDirectory;
    }

    @Override
    public TaskProvider<InstallExecutable> getInstallTask() {
        return installTask;
    }

    @Override
    public TaskProvider<LinkExecutable> getLinkTask() {
        return linkTask;
    }

    @Override
    public TaskProvider<RunTestExecutable> getRunTask() {
        return runTask;
    }

    @Override
    public FileCollection getCompileIncludePath() {
        // TODO: This should be modeled differently, perhaps as a dependency on the implementation configuration
        return super.getCompileIncludePath().plus(getProjectLayout().files(new Callable<FileCollection>() {
            @Override
            public FileCollection call() {
                CppComponent tested = testedComponent.getOrNull();
                if (tested == null) {
                    return getProjectLayout().files();
                }
                return ((DefaultCppComponent) tested).getAllHeaderDirs();
            }
        }));
    }
}
