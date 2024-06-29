package be.twofold.playground.jlox.model;

final class LoxNumber extends LoxValue {

    private final double value;

    LoxNumber(double value) {
        this.value = value;
    }


    @Override
    public double asNumber() {
        return value;
    }


    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof LoxNumber
            && Double.compare(((LoxNumber) obj).value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

}
