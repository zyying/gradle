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

package org.gradle.internal.reflect;

public class ClassInspectorTestHelper {
    interface SomeInterface {
        String FIELD = "abc";

        Number getProp();

        void setProp(Number value);

        String getReadOnly();

        void setWriteOnly(String value);

        void get();

        void set(String value);

        void getProp(String value);

        void setProp();

        void thing();
    }

    interface SubInterface extends SomeInterface {
        String FIELD = "abc";

        Long getOther();

        void setOther(Long l);
    }

    abstract static class AbstractSuperClass implements SomeInterface {
        private Number prop;

        @Override
        public Number getProp() {
            return prop;
        }

        @Override
        public void setProp(Number prop) {
            this.prop = prop;
        }

        public void thing() {}
    }

    abstract static class AbstractSubClass extends AbstractSuperClass implements SubInterface {
        private Long other;

        @Override
        public Long getOther() {
            return other;
        }

        @Override
        public void setOther(Long other) {
            this.other = other;
        }

        public void thing() {}
    }
}
