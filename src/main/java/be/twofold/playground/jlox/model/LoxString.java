package be.twofold.playground.jlox.model;

import be.twofold.playground.common.*;

final class LoxString extends LoxValue {

    private final String value;

    LoxString(String value) {
        this.value = Check.notNull(value);
    }


    @Override
    public String asString() {
        return value;
    }


    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof LoxString
            && value.equals(((LoxString) obj).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "LoxString(" + value + ")";
    }

}
