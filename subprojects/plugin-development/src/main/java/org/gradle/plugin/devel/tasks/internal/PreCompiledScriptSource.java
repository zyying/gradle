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
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.resource.TextResource;

public class PreCompiledScriptSource implements ScriptSource {
    private final String className;
    private final String fileName;
    private final String displayName;
    private final PreCompiledScriptResource resource;

    public PreCompiledScriptSource(String fileName, String className, String displayName, String hashCode) {
        this.className = className;
        this.fileName = fileName;
        this.displayName = displayName;
        this.resource = new PreCompiledScriptResource(displayName, HashCode.fromString(hashCode));
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public TextResource getResource() {
        return resource;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
