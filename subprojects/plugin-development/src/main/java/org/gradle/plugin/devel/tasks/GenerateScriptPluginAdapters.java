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
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.UncheckedException;
import org.gradle.plugin.devel.tasks.internal.PreCompiledScript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class GenerateScriptPluginAdapters extends DefaultTask {
    private final Set<PreCompiledScript> scriptPlugins = Sets.newHashSet();
    private final DirectoryProperty generatedClassesDir;
    private final DirectoryProperty metadataDir;
    private final DirectoryProperty classesDir;

    public GenerateScriptPluginAdapters() {
        this.generatedClassesDir = getProject().getObjects().directoryProperty();
        this.metadataDir = getProject().getObjects().directoryProperty();
        this.classesDir = getProject().getObjects().directoryProperty();
    }

    @InputFiles
    Set<File> getScriptFiles() {
        return scriptPlugins.stream().map(scriptPlugin -> scriptPlugin.getScriptFile()).collect(Collectors.toSet());
    }

    @OutputDirectory
    public DirectoryProperty getGeneratedClassesDir() {
        return generatedClassesDir;
    }

    @Internal
    public Set<PreCompiledScript> getScriptPlugins() {
        return scriptPlugins;
    }

    @Internal
    public DirectoryProperty getMetadataDir() {
        return metadataDir;
    }

    @Internal
    public DirectoryProperty getClassesDir() {
        return classesDir;
    }

    @TaskAction
    void generateScriptPluginAdapters() {
        File metadataDirValue = metadataDir.getAsFile().get();
        File classesDirValue = classesDir.getAsFile().get();

        for (PreCompiledScript scriptPlugin : scriptPlugins) {
            generateScriptPluginAdapter(scriptPlugin, classesDirValue, metadataDirValue);
        }
    }

    void generateScriptPluginAdapter(PreCompiledScript scriptPlugin, File baseClassesDir, File baseMetadataDir) {
        // TODO should take package into account
        File outputFile = generatedClassesDir.file(scriptPlugin.getGeneratedPluginClassName() + ".java").get().getAsFile();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile.toURI()))) {
            writer.write("import org.gradle.api.Project;\n");
            writer.write("import org.gradle.api.internal.project.ProjectInternal;\n");
            writer.write("import org.gradle.plugin.devel.tasks.internal.PreCompiledScriptRunner;\n");
            writer.write("import java.io.File;\n");
            writer.write("/**\n");
            writer.write(" * Precompiled " + scriptPlugin.getScriptFile().getName() + " script plugin.\n");
            writer.write(" **/\n");
            writer.write("public class " + scriptPlugin.getGeneratedPluginClassName() + " implements org.gradle.api.Plugin<org.gradle.api.Project> {\n");
            writer.write("  public void apply(Project project) {\n");
            writer.write("      PreCompiledScriptRunner.apply(\n");
            writer.write("          (ProjectInternal)project,");
            writer.write("         new File(\""+ scriptPlugin.getScriptFile().getAbsolutePath() + "\"),\n");
            writer.write("         new File(\""+ baseClassesDir.getAbsolutePath() + "\"),\n");
            writer.write("         new File(\""+ baseMetadataDir.getAbsolutePath() + "\")\n");
            writer.write("      );\n");
            writer.write("  }\n");
            writer.write("}\n");
        } catch (IOException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }
}
