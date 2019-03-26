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

package org.gradle.util.internal;

import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ReachableTypes {
    private final Map<Class<?>, Set<Class<?>>> inspected = new HashMap<Class<?>, Set<Class<?>>>();

    /**
     * Returns all classes reachable from the given class, including the class itself.
     */
    public Set<Class<?>> getReachableTypes(Class<?> type) {
        Set<Class<?>> types = inspected.get(type);
        if (types == null) {
            types = new HashSet<Class<?>>();
            visit(type, types);
            inspected.put(type, types);
        }
        return types;
    }

    private void visit(Class<?> type, Set<Class<?>> types) {
        if (type.isArray()) {
            visit(type.getComponentType(), types);
            return;
        }

        if (!types.add(type) || isCoreJavaType(type)) {
            return;
        }

        for (Type superType : type.getGenericInterfaces()) {
            visit(superType, types);
        }

        for (Method method : type.getDeclaredMethods()) {
            visit(method.getGenericReturnType(), types);
            for (TypeVariable<Method> typeVariable : method.getTypeParameters()) {
                visit(typeVariable, types);
            }
        }

        for (Field field : type.getDeclaredFields()) {
            visit(field.getGenericType(), types);
        }
    }

    boolean isCoreJavaType(final Class<?> type) {
        List<String> corePackagePrefixes = Lists.newArrayList("java", "javax");
        return type.getPackage() != null && corePackagePrefixes.stream().anyMatch(new Predicate<String>() {
            @Override
            public boolean test(String corePrefix) {
                return type.getPackage().getName().startsWith(corePrefix + ".");
            }
        });
    }

    private void visit(Type type, Set<Class<?>> types) {
        if (type instanceof Class) {
            visit((Class) type, types);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            visit(parameterizedType.getRawType(), types);
            for (Type typeArg : parameterizedType.getActualTypeArguments()) {
                visit(typeArg, types);
            }
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            for (Type bound : wildcardType.getUpperBounds()) {
                visit(bound, types);
            }
            for (Type bound : wildcardType.getLowerBounds()) {
                visit(bound, types);
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            visit(arrayType.getGenericComponentType(), types);
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable) type;
            for (Type bound : typeVariable.getBounds()) {
                visit(bound, types);
            }
        }
    }
}
