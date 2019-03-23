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

package org.gradle.workers.internal;

import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

public class WrappedActionExecutionSpec<T extends WorkParameters> extends SimpleActionExecutionSpec<T> {
    private final Class<?> wrappedImplementationClass;

    public WrappedActionExecutionSpec(Class<? extends WorkAction<T>> implementationClass, String displayName, T parameters, Class<?> wrappedImplementationClass) {
        super(implementationClass, displayName, parameters);
        this.wrappedImplementationClass = wrappedImplementationClass;
    }

    @Override
    public String getImplementationClassName() {
        return wrappedImplementationClass.getName();
    }

    public Class<?> getWrappedImplementationClass() {
        return wrappedImplementationClass;
    }
}
