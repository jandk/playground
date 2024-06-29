package be.twofold.playground.jlox.model;

final class LoxBool extends LoxValue {

    static final LoxBool True = new LoxBool(true);
    static final LoxBool False = new LoxBool(false);

    private final boolean value;

    private LoxBool(boolean value) {
        this.value = value;
    }


    @Override
    public boolean asBool() {
        return value;
    }


    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof LoxBool
            && ((LoxBool) obj).value == value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

}
