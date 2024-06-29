package be.twofold.playground.jlox.model;

final class LoxNil extends LoxValue {

    static final LoxNil Nil = new LoxNil();

    private LoxNil() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LoxNil;
    }

    @Override
    public int hashCode() {
        return LoxNil.class.hashCode();
    }

    @Override
    public String toString() {
        return "LoxNull";
    }

}
