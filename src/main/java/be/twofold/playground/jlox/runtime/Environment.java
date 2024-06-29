package be.twofold.playground.jlox.runtime;

import be.twofold.playground.jlox.model.*;
import be.twofold.playground.jlox.parser.*;

import java.util.*;

public class Environment {
    private final Map<String, LoxValue> values = new HashMap<>();

    final Environment enclosing;

    public Environment() {
        this(null);
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    LoxValue get(Token name) {
        if (values.containsKey(name.getLexeme())) {
            return values.get(name.getLexeme());
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
    }

    public LoxValue getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    void assign(Token name, LoxValue value) {
        if (values.containsKey(name.getLexeme())) {
            values.put(name.getLexeme(), value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
    }

    void assignAt(int distance, Token name, LoxValue value) {
        ancestor(distance).values.put(name.getLexeme(), value);
    }

    public void define(String name, LoxValue value) {
        values.put(name, value);
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }
}
