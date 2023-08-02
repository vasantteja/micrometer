package io.micrometer.observation;

import java.util.concurrent.Callable;

public class ObservedCallable<V> implements Callable<V> {

    private final ObservationRegistry registry;
    private final Observation.Context context;
    private final String observationName;
    private final Callable<V> delegate;
    private static final String DEFAULT_OBSERVATION_NAME = "child";

    public ObservedCallable(ObservationRegistry registry, Observation.Context context, String observationName, Callable delegate) {
        this.registry = registry;
        this.context = context;
        this.observationName = observationName != null ? observationName : DEFAULT_OBSERVATION_NAME;
        this.delegate = delegate;
    }

    public V call() throws Exception {
        Observation childObservation = Observation.createNotStarted(observationName, registry).parentObservation(registry.getCurrentObservation());
        try (Observation.Scope scope = childObservation.openScope()) {
            return delegate.call();
        } catch (Exception | Error e) {
            childObservation.error(e);
            throw e;
        } finally {
            childObservation.stop();
        }
    }



}
