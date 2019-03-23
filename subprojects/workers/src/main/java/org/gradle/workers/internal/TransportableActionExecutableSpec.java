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

import org.gradle.internal.Cast;
import org.gradle.internal.instantiation.InstantiatorFactory;
import org.gradle.internal.state.Managed;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

public class TransportableActionExecutableSpec<T extends WorkParameters> implements ActionExecutionSpec<T> {
    private final Class<? extends WorkAction<T>> implementationClass;
    private final Class<?> wrappedImplementationClass;
    private final String displayName;
    private final Object managedObjectState;
    private final Class<T> publicType;

    public TransportableActionExecutableSpec(WrappedActionExecutionSpec<T> spec) {
        implementationClass = spec.getImplementationClass();
        wrappedImplementationClass = spec.getWrappedImplementationClass();
        displayName = spec.getDisplayName();
        managedObjectState = ((Managed)spec.getParameters()).unpackState();
        publicType = Cast.uncheckedCast(((Managed) spec.getParameters()).publicType());
    }

    @Override
    public Class<? extends WorkAction<T>> getImplementationClass() {
        return implementationClass;
    }

    @Override
    public String getImplementationClassName() {
        return wrappedImplementationClass.getName();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public T getParameters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Instantiate an {@link ActionExecutionSpec} with managed objects constructed from the extracted state stored in this object.
     */
    ActionExecutionSpec<T> withManagedObject(InstantiatorFactory instantiatorFactory) {
        Managed managed = Cast.uncheckedCast(instantiatorFactory.injectScheme().forType(publicType).newInstance());
        T parameters = managed.managedFactory().fromState(publicType, managedObjectState);
        return new WrappedActionExecutionSpec<T>(implementationClass, displayName, parameters, wrappedImplementationClass);
    }

    /**
     * Construct a transportable {@link ActionExecutionSpec} that extracts the state of managed objects and can be reconstituted in another context.
     */
    public static <S extends WorkParameters> TransportableActionExecutableSpec<S> withoutManagedObject(WrappedActionExecutionSpec<S> simpleSpec) {
        return new TransportableActionExecutableSpec<S>(simpleSpec);
    }
}
