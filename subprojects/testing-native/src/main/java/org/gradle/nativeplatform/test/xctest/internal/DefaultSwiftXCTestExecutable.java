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

package org.gradle.nativeplatform.test.xctest.internal;

import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.cpp.internal.NativeVariantIdentity;
import org.gradle.language.nativeplatform.internal.ConfigurableComponentWithExecutable;
import org.gradle.language.nativeplatform.internal.Names;
import org.gradle.language.swift.SwiftPlatform;
import org.gradle.nativeplatform.tasks.InstallExecutable;
import org.gradle.nativeplatform.tasks.LinkExecutable;
import org.gradle.nativeplatform.test.xctest.SwiftXCTestExecutable;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import javax.inject.Inject;

public class DefaultSwiftXCTestExecutable extends DefaultSwiftXCTestBinary implements SwiftXCTestExecutable, ConfigurableComponentWithExecutable {
    private final TaskProvider<LinkExecutable> linkTask;
    private final TaskProvider<InstallExecutable> installTask;
    private final Property<Task> executableFileProducer;
    private final ConfigurableFileCollection files;
    private final RegularFileProperty debuggerExecutableFile;

    @Inject
    public DefaultSwiftXCTestExecutable(Names names, ObjectFactory objectFactory, Provider<String> module,
                                        boolean testable,
                                        FileCollection source,
                                        TaskContainer tasks,
                                        ConfigurationContainer configurations, Configuration implementation,
                                        SwiftPlatform targetPlatform, NativeToolChainInternal toolChain, PlatformToolProvider platformToolProvider,
                                        NativeVariantIdentity identity) {
        super(names, objectFactory, module, testable, source, tasks, configurations, implementation, targetPlatform, toolChain, platformToolProvider, identity);
        this.debuggerExecutableFile = objectFactory.fileProperty();
        this.executableFileProducer = objectFactory.property(Task.class);
        this.linkTask = tasks.register(names.getTaskName("link"), LinkExecutable.class);
        this.installTask = tasks.register(names.getTaskName("install"), InstallExecutable.class);
        this.files = objectFactory.fileCollection();
    }

    @Override
    public Property<RegularFile> getDebuggerExecutableFile() {
        return debuggerExecutableFile;
    }

    @Override
    public Property<Task> getExecutableFileProducer() {
        return executableFileProducer;
    }

    @Override
    public TaskProvider<LinkExecutable> getLinkTask() {
        return linkTask;
    }

    @Override
    public TaskProvider<InstallExecutable> getInstallTask() {
        return installTask;
    }

    @Override
    public ConfigurableFileCollection getOutputs() {
        return files;
    }
}
