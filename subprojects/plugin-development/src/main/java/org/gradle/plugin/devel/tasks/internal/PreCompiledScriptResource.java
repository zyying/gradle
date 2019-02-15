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

import org.gradle.api.resources.ResourceException;
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.resource.ResourceLocation;
import org.gradle.internal.resource.TextResource;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;

public class PreCompiledScriptResource implements TextResource {
    private final String displayName;
    private final HashCode hashcode;

    public PreCompiledScriptResource(String displayName, HashCode hashcode) {
        this.displayName = displayName;
        this.hashcode = hashcode;
    }

    @Override
    public ResourceLocation getLocation() {
        return new PreCompiledScriptResourceLocation(displayName);
    }

    @Nullable
    @Override
    public File getFile() {
        return null;
    }

    @Nullable
    @Override
    public Charset getCharset() {
        return null;
    }

    @Override
    public boolean isContentCached() {
        return true;
    }

    @Override
    public boolean getExists() throws ResourceException {
        return true;
    }

    @Override
    public boolean getHasEmptyContent() throws ResourceException {
        return false;
    }

    @Override
    public Reader getAsReader() throws ResourceException {
        return null;
    }

    @Override
    public String getText() throws ResourceException {
        return null;
    }

    @Override
    public HashCode getContentHash() throws ResourceException {
        return hashcode;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    private static class PreCompiledScriptResourceLocation implements ResourceLocation {
        private final String displayName;

        public PreCompiledScriptResourceLocation(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Nullable
        @Override
        public File getFile() {
            return null;
        }

        @Nullable
        @Override
        public URI getURI() {
            return null;
        }
    }
}
