package io.micrometer.observation;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;

import java.util.concurrent.*;


public class ObservedExecutor {

    private final ObservationRegistry observationRegistry;
    private final String observationName;
    private ExecutorService delegate;

    public ObservedExecutor(ObservationRegistry observationRegistry, String observationName, ExecutorService delegate) {
        this.observationRegistry = observationRegistry;
        this.observationName = observationName;
        this.delegate = delegate;
    }

    public void execute(Runnable command) {
        ContextRegistry registry = new ContextRegistry();
        ContextSnapshotFactory snapshotFactory = ContextSnapshotFactory.builder()
            .contextRegistry(registry)
            .clearMissing(false)
            .build();
        registry.registerThreadLocalAccessor(new ObservationThreadLocalAccessor(observationRegistry));
        ContextSnapshot snapshot = snapshotFactory.captureAll();

    }

    @Override
    public Future submit(Callable task) {
        return this.submit(new ObservedCallable<>(registry, null, observationName, task));
    }

}
