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

package org.gradle.internal.service;

import com.google.common.reflect.TypeToken;
import org.gradle.api.reflect.InjectionPointQualifier;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Supplier;

abstract public class ServiceLookupWithInjectionPoints implements ServiceLookup {

    public abstract Collection<InjectionPoint> getInjectionPoints();

    @Nullable
    private Object find(Type serviceType, @Nullable Class<? extends Annotation> annotatedWith) {
        TypeToken<?> serviceTypeToken = TypeToken.of(serviceType);
        for (InjectionPoint injectionPoint : getInjectionPoints()) {
            if (annotatedWith == injectionPoint.getAnnotation() && serviceTypeToken.isSupertypeOf(injectionPoint.getInjectedType())) {
                return injectionPoint.getValueToInject();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object find(Type serviceType) throws ServiceLookupException {
        return find(serviceType, null);
    }

    @Override
    public Object get(Type serviceType) throws UnknownServiceException, ServiceLookupException {
        Object result = find(serviceType);
        if (result == null) {
            throw new UnknownServiceException(serviceType, "No service of type " + serviceType + " available.");
        }
        return result;
    }

    @Override
    public Object get(Type serviceType, Class<? extends Annotation> annotatedWith) throws UnknownServiceException, ServiceLookupException {
        Object result = find(serviceType, annotatedWith);
        if (result == null) {
            throw new UnknownServiceException(serviceType, "No service of type " + serviceType + " available.");
        }
        return result;
    }

    protected static class InjectionPoint {
        private final Class<? extends Annotation> annotation;
        private final Class<?> injectedType;
        private final Supplier<Object> valueToInject;

        public static ServiceLookupWithInjectionPoints.InjectionPoint injectedByAnnotation(Class<? extends Annotation> annotation, Supplier<Object> valueToInject) {
            return new InjectionPoint(annotation, determineTypeFromAnnotation(annotation), valueToInject);
        }

        public static InjectionPoint injectedByType(Class<?> injectedType, Supplier<Object> valueToInject) {
            return new InjectionPoint(null, injectedType, valueToInject);
        }

        private InjectionPoint(@Nullable Class<? extends Annotation> annotation, Class<?> injectedType, Supplier<Object> valueToInject) {
            this.annotation = annotation;
            this.injectedType = injectedType;
            this.valueToInject = valueToInject;
        }

        private static Class<?> determineTypeFromAnnotation(Class<? extends Annotation> annotation) {
            Class<?>[] supportedTypes = annotation.getAnnotation(InjectionPointQualifier.class).supportedTypes();
            if (supportedTypes.length != 1) {
                throw new IllegalArgumentException("Cannot determine supported type for annotation " + annotation.getName());
            }
            return supportedTypes[0];
        }

        @Nullable
        public Class<? extends Annotation> getAnnotation() {
            return annotation;
        }

        public Class<?> getInjectedType() {
            return injectedType;
        }

        public Object getValueToInject() {
            return valueToInject.get();
        }
    }
}
