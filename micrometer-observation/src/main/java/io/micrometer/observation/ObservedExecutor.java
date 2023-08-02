package io.micrometer.observation;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshot;

import java.util.concurrent.*;


public class ObservedExecutor extends ContextExecutorService {

    /*
    Get the context snapshot from the function initialization,
     */
    private final ObservationRegistry registry;
    private final String observationName;

    public ObservedExecutor(ObservationRegistry registry, String observationName, ExecutorService delegate, ContextSnapshot snapshot) {
        super(delegate, () -> snapshot);
        this.registry = registry;
        this.observationName = observationName;
    }

    @Override
    public void execute(Runnable command) {
        this.submit(new ObservedRunnable(registry, null, observationName, command));
    }

    @Override
    public Future submit(Callable task) {
        return this.submit(new ObservedCallable<>(registry, null, observationName, task));
    }

}
