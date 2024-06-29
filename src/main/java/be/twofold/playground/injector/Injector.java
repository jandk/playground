package be.twofold.playground.injector;

import jakarta.inject.*;

import java.lang.reflect.*;
import java.util.*;

public class Injector {

    private final Set<Class<?>> requested = new HashSet<>();
    private final Set<Class<?>> instantiable = new HashSet<>();
    private final Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <T> T getInstance(Class<T> clazz) {
        if (providers.containsKey(clazz)) {
            return clazz.cast(providers.get(clazz).get());
        }
        if (requested.contains(clazz)) {
            if (!providers.containsKey(clazz)) {
                throw new IllegalStateException("Circular dependency detected");
            }
        } else {
            requested.add(clazz);
        }

        Constructor<T> constructor = findConstructor(clazz);

        Provider<T> provider;
        if (clazz.isAnnotationPresent(Singleton.class)) {
            T instance = instantiate(constructor);
            provider = () -> instance;
        } else {
            provider = () -> instantiate(constructor);
        }

        T result = provider.get();
        providers.put(clazz, provider);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> findConstructor(Class<T> clazz) {
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();

        if (constructors.length == 0) {
            throw new IllegalArgumentException("No public constructors found");
        }
        if (constructors.length == 1) {
            return constructors[0];
        }

        List<Constructor<T>> annotated = Arrays.stream(constructors)
            .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
            .toList();

        if (annotated.size() == 0) {
            throw new IllegalArgumentException("Found multiple public constructors, but none annotated with @Inject");
        }
        if (annotated.size() != 1) {
            throw new IllegalArgumentException("Found multiple public constructors annotated with @Inject");
        }
        return annotated.get(0);
    }

    private <T> T instantiate(Constructor<T> constructor) {
        Object[] arguments = Arrays.stream(constructor.getParameters())
            .map(parameter -> getInstance(parameter.getType()))
            .toArray();

        try {
            return constructor.newInstance(arguments);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Could not create instance", e);
        }
    }

}
