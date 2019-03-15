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

import org.gradle.api.Action;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

public interface ClassDetails {
    /**
     * Returns the non-private properties of this class. Includes inherited properties
     */
    Set<String> getPropertyNames();

    /**
     * Returns the non-private properties of this class. Includes inherited properties
     */
    Collection<? extends PropertyDetails> getProperties();

    /**
     * Returns the details of a non-private property of this class.
     */
    PropertyDetails getProperty(String name) throws NoSuchPropertyException;

    /**
     * Visits all methods of this class, including all inherited, overridden, private, abstract, synthetic and static methods.
     */
    void visitAllMethods(Action<? super Method> visitor);

    /**
     * Visits the non-private non-static methods of this class that are not property getter or setter methods.
     * Includes inherited methods but not overridden methods so that each method signature is visited once.
     */
    void visitInstanceMethods(Action<? super Method> visitor);

    /**
     * Visits all fields of this class, including all inherited, private, synthetic and static fields.
     */
    void visitAllFields(Action<? super Field> visitor);

    /**
     * Visits this type and each of its super types.
     */
    void visitTypes(Action<? super ClassDetails> visitor);

}
