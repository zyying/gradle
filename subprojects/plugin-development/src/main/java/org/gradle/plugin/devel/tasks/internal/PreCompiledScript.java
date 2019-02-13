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

import org.gradle.groovy.scripts.ScriptSource;

import java.io.File;

public class PreCompiledScript {
    public static final String SCRIPT_PLUGIN_EXTENSION = ".gradle";
    private static final String PLUGIN_PREFIX = "cp_proj";
    private static final String BUILDSCRIPT_PREFIX = "proj";

    private final ScriptSource scriptSource;
    private final String scriptHash;

    public PreCompiledScript(ScriptSource scriptSource) {
        this.scriptSource = scriptSource;
        this.scriptHash = scriptSource.getResource().getContentHash().toString();
    }

    public File getPluginMetadataDir(File baseMetadataDir) {
        return new File(baseMetadataDir, PLUGIN_PREFIX + scriptHash + "/metadata");
    }

    public File getBuildScriptMetadataDir(File baseMetadataDir) {
        return new File(baseMetadataDir, BUILDSCRIPT_PREFIX + scriptHash + "/metadata");
    }

    public File getPluginClassesDir(File baseMetadataDir) {
        return new File(baseMetadataDir, PLUGIN_PREFIX + scriptHash + "/classes");
    }

    public File getBuildScriptClassesDir(File baseMetadataDir) {
        return new File(baseMetadataDir, BUILDSCRIPT_PREFIX + scriptHash + "/classes");
    }

    public File getScriptFile() {
        return new File(scriptSource.getFileName());
    }

    public String getId() {
        // TODO should also detect package name and add it to the id
        return getFileNameWithoutExtension();
    }

    private String getFileNameWithoutExtension() {
        String fileName = new File(scriptSource.getFileName()).getName();
        return fileName.substring(0, fileName.indexOf(SCRIPT_PLUGIN_EXTENSION));
    }

    public String getClassName() {
        // TODO should also include the package name
        return scriptSource.getClassName();
    }

    public String getGeneratedPluginClassName() {
        return getFileNameWithoutExtension() + "Plugin";
    }

    public ScriptSource getSource() {
        return scriptSource;
    }
}
