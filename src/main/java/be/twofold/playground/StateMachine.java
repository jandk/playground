package be.twofold.playground;

import java.util.*;

public final class StateMachine<S extends Enum<S>, E extends Enum<E>> {

    private final Map<S, Map<E, S>> transitions;
    private final S start;

    private StateMachine(Map<S, Map<E, S>> transitions, S start) {
        this.transitions = new EnumMap<>(transitions);
        this.start = start;
    }

    public static <S extends Enum<S>, T extends Enum<T>> Builder<S, T> builder(
        Class<S> stateClass, Class<T> eventClass
    ) {
        return new Builder<>(stateClass, eventClass);
    }

    public State<S, E> createState(S state) {
        return new State<>(this, state);
    }

    public S transition(S state, E event) {
        S newState = transitions
            .getOrDefault(state, Map.of())
            .get(event);

        if (newState == null) {
            throw new IllegalStateException("Cannot transition from " + state + " using " + event);
        }

        return newState;
    }

    public S transitions(Iterable<E> events) {
        S state = start;
        for (E event : events) {
            state = transition(state, event);
        }
        return state;
    }

    public Set<E> validTransitions(S state) {
        return transitions
            .getOrDefault(state, Map.of())
            .keySet();
    }

    public static final class State<S extends Enum<S>, E extends Enum<E>> {
        private final StateMachine<S, E> machine;
        private S state;

        private State(StateMachine<S, E> machine, S state) {
            this.machine = machine;
            this.state = state;
        }

        public void transition(E event) {
            state = machine.transition(state, event);
        }
    }

    public static final class Builder<S extends Enum<S>, E extends Enum<E>> {
        private final Class<E> eventClass;
        private final Map<S, Map<E, S>> transitions;
        private S start;

        private Builder(Class<S> stateClass, Class<E> eventClass) {
            this.eventClass = eventClass;
            this.transitions = new EnumMap<>(stateClass);
        }

        public Builder<S, E> add(S from, E event, S to) {
            S previous = transitions
                .computeIfAbsent(from, __ -> new EnumMap<>(eventClass))
                .put(event, to);

            if (previous != null) {
                throw new IllegalArgumentException("The transition from " + from + " to " + to + " using " + event + " already exists");
            }

            return this;
        }

        public Builder<S, E> start(S start) {
            this.start = start;
            return this;
        }

        public StateMachine<S, E> build() {
            if (start == null) {
                throw new IllegalStateException("Need a start state");
            }
            return new StateMachine<>(transitions, start);
        }
    }

}
