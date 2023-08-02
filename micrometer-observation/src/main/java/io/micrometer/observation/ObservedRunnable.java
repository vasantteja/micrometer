package io.micrometer.observation;

public class ObservedRunnable implements Runnable {

    private final ObservationRegistry registry;
    private final Observation.Context context;
    private final String observationName;
    private final Runnable delegate;
    private static final String DEFAULT_OBSERVATION_NAME = "child";

    public ObservedRunnable(ObservationRegistry registry, Observation.Context context, String observationName, Runnable delegate) {
        this.registry = registry;
        this.context = context;
        this.observationName = observationName != null ? observationName : DEFAULT_OBSERVATION_NAME;
        this.delegate = delegate;
    }

    public ObservedRunnable(ObservationRegistry registry, String observationName, Runnable delegate) {
        this(registry, null, observationName, delegate);
    }

    @Override
    public void run() {
        Observation childObservation = Observation.createNotStarted(observationName, registry).parentObservation(registry.getCurrentObservation());
        try (Observation.Scope scope = childObservation.openScope()) {
            delegate.run();
        } catch (Exception | Error e) {
            childObservation.error(e);
            throw e;
        } finally {
            childObservation.stop();
        }
    }
}
