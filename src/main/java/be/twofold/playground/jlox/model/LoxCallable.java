package be.twofold.playground.jlox.model;

import be.twofold.playground.jlox.runtime.*;

import java.util.*;

public abstract class LoxCallable extends LoxValue {

    public abstract int arity();

    public abstract LoxValue call(Interpreter interpreter, List<LoxValue> arguments);

}
