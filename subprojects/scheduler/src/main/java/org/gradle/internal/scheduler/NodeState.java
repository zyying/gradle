/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.internal.scheduler;

public enum NodeState {
    /**
     * Node can be executed once all its constraints have been fulfilled, but can later be cancelled.
     */
    RUNNABLE,

    /**
     * Node must be executed once all its constraints have been fulfilled, and cannot be cancelled.
     */
    MUST_RUN,

    /**
     * Node should not be executed, unless later re-activated as a finalizer.
     */
    CANCELLED,

    /**
     * Node or one of its dependencies has failed to execute, and should not be re-activated.
     */
    FAILED
}