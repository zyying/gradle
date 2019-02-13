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

package org.gradle.configuration.project;

import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.cache.StringInterner;
import org.gradle.configuration.CompileOperationFactory;
import org.gradle.configuration.ScriptTarget;
import org.gradle.groovy.scripts.ScriptSource;
import org.gradle.groovy.scripts.internal.BuildScriptData;
import org.gradle.groovy.scripts.internal.BuildScriptDataSerializer;
import org.gradle.groovy.scripts.internal.BuildScriptTransformer;
import org.gradle.groovy.scripts.internal.CompileOperation;
import org.gradle.groovy.scripts.internal.FactoryBackedCompileOperation;
import org.gradle.groovy.scripts.internal.InitialPassStatementTransformer;
import org.gradle.groovy.scripts.internal.SubsetScriptTransformer;
import org.gradle.plugin.management.internal.PluginRequests;
import org.gradle.plugin.management.internal.PluginRequestsSerializer;

public class DefaultCompileOperationFactory implements CompileOperationFactory {
    private static final StringInterner INTERNER = new StringInterner();
    private static final String CLASSPATH_COMPILE_STAGE = "CLASSPATH";
    private static final String BODY_COMPILE_STAGE = "BODY";

    private final PluginRequestsSerializer pluginRequestsSerializer = new PluginRequestsSerializer();
    private final BuildScriptDataSerializer buildScriptDataSerializer = new BuildScriptDataSerializer();
    private final DocumentationRegistry documentationRegistry;

    public DefaultCompileOperationFactory(DocumentationRegistry documentationRegistry) {
        this.documentationRegistry = documentationRegistry;
    }

    public CompileOperation<PluginRequests> getPluginRequestsCompileOperation(ScriptSource scriptSource, ScriptTarget initialPassScriptTarget) {
        InitialPassStatementTransformer initialPassStatementTransformer = new InitialPassStatementTransformer(scriptSource, initialPassScriptTarget, documentationRegistry);
        SubsetScriptTransformer initialTransformer = new SubsetScriptTransformer(initialPassStatementTransformer);
        String id = INTERNER.intern("cp_" + initialPassScriptTarget.getId());
        return new FactoryBackedCompileOperation<PluginRequests>(id, CLASSPATH_COMPILE_STAGE, initialTransformer, initialPassStatementTransformer, pluginRequestsSerializer);
    }

    public CompileOperation<BuildScriptData> getBuildScriptDataCompileOperation(ScriptSource scriptSource, ScriptTarget scriptTarget) {
        BuildScriptTransformer buildScriptTransformer = new BuildScriptTransformer(scriptSource, scriptTarget);
        String operationId = scriptTarget.getId();
        return new FactoryBackedCompileOperation<BuildScriptData>(operationId, BODY_COMPILE_STAGE, buildScriptTransformer, buildScriptTransformer, buildScriptDataSerializer);
    }
}
