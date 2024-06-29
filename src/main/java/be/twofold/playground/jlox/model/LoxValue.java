package be.twofold.playground.jlox.model;

public abstract class LoxValue {

    LoxValue() {
    }

    public static LoxValue string(String value) {
        return new LoxString(value);
    }

    public static LoxValue number(double value) {
        return new LoxNumber(value);
    }

    public static LoxValue bool(boolean value) {
        return value ? LoxBool.True : LoxBool.False;
    }

    public static LoxValue nil() {
        return LoxNil.Nil;
    }


    public final boolean isString() {
        return this instanceof LoxString;
    }

    public final boolean isNumber() {
        return this instanceof LoxNumber;
    }

    public final boolean isBool() {
        return this instanceof LoxBool;
    }

    public final boolean isNil() {
        return this instanceof LoxNil;
    }


    public String asString() {
        throw new ClassCastException(getClass().getSimpleName());
    }

    public double asNumber() {
        throw new ClassCastException(getClass().getSimpleName());
    }

    public boolean asBool() {
        throw new ClassCastException(getClass().getSimpleName());
    }

}
