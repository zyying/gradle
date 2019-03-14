/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.internal.reflect;

import org.gradle.api.Transformer;
import org.gradle.api.internal.GeneratedSubclass;
import org.gradle.cache.internal.CrossBuildInMemoryCache;
import org.gradle.cache.internal.CrossBuildInMemoryCacheFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ClassInspector {
    private final CrossBuildInMemoryCache<Class<?>, MutableClassDetails> cache;
    private final ClassDetailsClassTransformer classTransformer = new ClassDetailsClassTransformer();

    public ClassInspector(CrossBuildInMemoryCacheFactory cacheFactory) {
        this.cache = cacheFactory.newClassCache();
    }

    /**
     * Extracts a view of the given class. Ignores private methods.
     */
    public ClassDetails inspect(Class<?> type) {
        return cache.get(type, classTransformer);
    }

    private void visitGraph(Class<?> type, MutableClassDetails classDetails) {
        if (GeneratedSubclass.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Should not inspect a generated subclass.");
        }

        inspectClass(type, classDetails);
        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
            mergeClassDetails(superclass, classDetails);
        }
        for (Class<?> superInterface : type.getInterfaces()) {
            mergeClassDetails(superInterface, classDetails);
        }
    }

    private void inspectClass(Class<?> type, MutableClassDetails classDetails) {
        for (Method method : type.getDeclaredMethods()) {
            classDetails.method(method);

            if (Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            PropertyAccessorType accessorType = PropertyAccessorType.of(method);
            if (accessorType == PropertyAccessorType.GET_GETTER || accessorType == PropertyAccessorType.IS_GETTER) {
                String propertyName = accessorType.propertyNameFor(method);
                classDetails.property(propertyName).addGetter(method);
            } else if (accessorType == PropertyAccessorType.SETTER) {
                String propertyName = accessorType.propertyNameFor(method);
                classDetails.property(propertyName).addSetter(method);
            } else {
                classDetails.instanceMethod(method);
            }
        }
        for (Field field : type.getDeclaredFields()) {
            classDetails.field(field);
        }
    }

    private void mergeClassDetails(Class<?> type, MutableClassDetails classDetails) {
        MutableClassDetails details = cache.get(type, classTransformer);
        details.applyTo(classDetails);
    }

    private class ClassDetailsClassTransformer implements Transformer<MutableClassDetails, Class<?>> {
        @Override
        public MutableClassDetails transform(Class<?> type) {
            MutableClassDetails classDetails = new MutableClassDetails(type);
            visitGraph(type, classDetails);
            return classDetails;
        }
    }
}
