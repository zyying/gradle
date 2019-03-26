/*
 * Copyright 2017 the original author or authors.
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

import com.google.common.collect.ImmutableList;
import org.gradle.api.Action;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.instantiation.InstantiatorFactory;
import org.gradle.internal.service.ServiceLookup;
import org.gradle.internal.service.ServiceLookupWithInjectionPoints;
import org.gradle.internal.service.UnknownServiceException;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerConfiguration;
import org.gradle.workers.WorkerExecutionException;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.util.Collection;
import java.util.function.Supplier;

public class DefaultWorkerServer implements WorkerProtocol {
    private final InstantiatorFactory instantiatorFactory;
    private final WorkerExecutor workerExecutor;

    @Inject
    public DefaultWorkerServer(InstantiatorFactory instantiatorFactory, WorkerExecutor workerExecutor) {
        this.instantiatorFactory = instantiatorFactory;
        this.workerExecutor = workerExecutor;
    }

    @Override
    public DefaultWorkResult execute(ActionExecutionSpec<? extends WorkParameters> spec) {
        try {
            ServiceLookup services = new WorkActionServiceLookup(spec.getParameters(), workerExecutor, instantiatorFactory);
            WorkAction action = instantiatorFactory.injectScheme().forType(spec.getImplementationClass()).newInstance(services);
            if (action instanceof WrappedWorkAction) {
                ((WrappedWorkAction) action).setWrappedImplementationClass(((WrappedActionExecutionSpec)spec).getWrappedImplementationClass());
            }
            action.execute();
            return getWorkResult(action);
        } catch (Throwable t) {
            return new DefaultWorkResult(true, t);
        }
    }

    private DefaultWorkResult getWorkResult(WorkAction<?> workAction) {
        if (workAction instanceof HasWorkResult) {
            WorkResult workResult = ((HasWorkResult) workAction).getWorkResult();
            if (workResult instanceof DefaultWorkResult) {
                return (DefaultWorkResult) workResult;
            } else {
                return new DefaultWorkResult(workResult.getDidWork(), null);
            }
        } else {
            return new DefaultWorkResult(true, null);
        }
    }

    @Override
    public String toString() {
        return "DefaultWorkerServer{}";
    }

    static class WorkActionServiceLookup extends ServiceLookupWithInjectionPoints {
        private final ImmutableList<InjectionPoint> injectionPoints;

        public WorkActionServiceLookup(final WorkParameters parameters, final WorkerExecutor workerExecutor, final InstantiatorFactory instantiatorFactory) {
            ImmutableList.Builder<InjectionPoint> builder = ImmutableList.builder();
            if (parameters != null) {
                builder.add(InjectionPoint.injectedByType(parameters.getClass(), new Supplier<Object>() {
                    @Override
                    public Object get() {
                        return parameters;
                    }
                }));
            } else {
                builder.add(InjectionPoint.injectedByType(WorkParameters.None.class, new Supplier<Object>() {
                    @Override
                    public Object get() {
                        throw new UnknownServiceException(WorkParameters.None.class, "Cannot query parameters for a work implementation without parameters.");
                    }
                }));
            }

            builder.add(InjectionPoint.injectedByType(WorkerExecutor.class, new Supplier<Object>() {
                @Override
                public Object get() {
                    if (workerExecutor != null) {
                        return workerExecutor;
                    } else {
                        return new UnavailableWorkerExecutor();
                    }
                }
            }));

            if (instantiatorFactory != null) {
                builder.add(InjectionPoint.injectedByType(InstantiatorFactory.class, new Supplier<Object>() {
                    @Override
                    public Object get() {
                        return instantiatorFactory;
                    }
                }));
            }

            this.injectionPoints = builder.build();
        }

        @Override
        public Collection<InjectionPoint> getInjectionPoints() {
            return injectionPoints;
        }
    }

    private static class UnavailableWorkerExecutor implements WorkerExecutor {
        @Override
        public void submit(Class<? extends Runnable> actionClass, Action<? super WorkerConfiguration> configAction) {
            throw unavailable();
        }

        @Override
        public void await() throws WorkerExecutionException {
            throw unavailable();
        }

        private RuntimeException unavailable() {
            return new UnsupportedOperationException("WorkerExecutor is not available when using this isolation mode.");
        }
    }
}
