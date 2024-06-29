package be.twofold.playground.fx;

import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.shape.*;

import static java.lang.Math.*;

class Ball {
    private final DoubleProperty xVelocity; // pixels per second
    private final DoubleProperty yVelocity;
    private final ReadOnlyDoubleWrapper speed;
    private final double mass; // arbitrary units
    private final double radius; // pixels

    private final Circle view;

    public Ball(double centerX, double centerY, double radius,
                double xVelocity, double yVelocity, double mass) {

        this.view = new Circle(centerX, centerY, radius);
        this.xVelocity = new SimpleDoubleProperty(this, "xVelocity", xVelocity);
        this.yVelocity = new SimpleDoubleProperty(this, "yVelocity", yVelocity);
        this.speed = new ReadOnlyDoubleWrapper(this, "speed");
        speed.bind(Bindings.createDoubleBinding(() -> {
            final double xVel = getXVelocity();
            final double yVel = getYVelocity();
            return sqrt(xVel * xVel + yVel * yVel);
        }, this.xVelocity, this.yVelocity));
        this.mass = mass;
        this.radius = radius;
        view.setRadius(radius);
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public final double getXVelocity() {
        return xVelocity.get();
    }

    public final void setXVelocity(double xVelocity) {
        this.xVelocity.set(xVelocity);
    }

    public final DoubleProperty xVelocityProperty() {
        return xVelocity;
    }

    public final double getYVelocity() {
        return yVelocity.get();
    }

    public final void setYVelocity(double yVelocity) {
        this.yVelocity.set(yVelocity);
    }

    public final DoubleProperty yVelocityProperty() {
        return yVelocity;
    }

    public final double getSpeed() {
        return speed.get();
    }

    public final ReadOnlyDoubleProperty speedProperty() {
        return speed.getReadOnlyProperty();
    }

    public final double getCenterX() {
        return view.getCenterX();
    }

    public final void setCenterX(double centerX) {
        view.setCenterX(centerX);
    }

    public final DoubleProperty centerXProperty() {
        return view.centerXProperty();
    }

    public final double getCenterY() {
        return view.getCenterY();
    }

    public final void setCenterY(double centerY) {
        view.setCenterY(centerY);
    }

    public final DoubleProperty centerYProperty() {
        return view.centerYProperty();
    }

    public Shape getView() {
        return view;
    }
}
