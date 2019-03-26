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

package org.gradle.util.internal

import spock.lang.Specification

import static org.gradle.util.internal.ReachableTypesTestHelper.*

class ReachableTypesTest extends Specification {
    def reachableTypes = new ReachableTypes()

    def "inspects type and caches result"() {
        expect:
        def types = reachableTypes.getReachableTypes(ReachableTypesTestHelper.ThingImpl)
        assertMatches types, [ReachableTypesTestHelper, ReachableTypesTestHelper.ThingImpl, ReachableTypesTestHelper.AnotherThing, Thing, SuperThing, GenericThing, Item1, Item2, Item3, Item4, Item5, Item6, Runnable, List, Map, Set, String, int] as Set

        def types2 = reachableTypes.getReachableTypes(ReachableTypesTestHelper.ThingImpl)
        types2.is(types)
    }

    def "inspects cyclic types"() {
        expect:
        def types = reachableTypes.getReachableTypes(Parent)
        types == [Parent, Child] as Set

        def types2 = reachableTypes.getReachableTypes(Child)
        types2 == types
    }

    void assertMatches(Set list1, Set list2) {
        assert (list1 - list2).empty && (list2 - list1).empty
    }
}
