package be.twofold.playground.fx;

import javafx.animation.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AnimationTimerTest extends Application {

    private final BufferedWriter statistics;

    {
        try {
            statistics = Files.newBufferedWriter(Paths.get("C:\\Temp\\stats.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final ObservableList<Ball> balls = FXCollections.observableArrayList();
    private static final int NUM_BALLS = 10;
    private static final double MIN_RADIUS = 5;
    private static final double MAX_RADIUS = 15;
    private static final double MIN_SPEED = 50;
    private static final double MAX_SPEED = 250;
    private static final Color[] COLORS = new Color[]{
        Color.RED, Color.YELLOW, Color.GREEN, Color.BROWN, Color.BLUE, Color.PINK, Color.BLACK
    };


    private final FrameStats frameStats = new FrameStats();
    private final AudioRenderer audioRenderer = new AudioRenderer();

    @Override
    public void start(Stage primaryStage) {
        Pane ballContainer = new Pane();

        constrainBallsOnResize(ballContainer);

        ballContainer.addEventHandler(MouseEvent.MOUSE_CLICKED,
            event -> {
                if (event.getClickCount() == 2) {
                    balls.clear();
                    createBalls(NUM_BALLS, MIN_RADIUS, MAX_RADIUS, MIN_SPEED, MAX_SPEED, ballContainer.getWidth() / 2, ballContainer.getHeight() / 2);
                }
            });

        balls.addListener((ListChangeListener<Ball>) change -> {
            while (change.next()) {
                for (Ball b : change.getAddedSubList()) {
                    ballContainer.getChildren().add(b.getView());
                }
                for (Ball b : change.getRemoved()) {
                    ballContainer.getChildren().remove(b.getView());
                }
            }
        });

        createBalls(NUM_BALLS, MIN_RADIUS, MAX_RADIUS, MIN_SPEED, MAX_SPEED, 400, 300);

        BorderPane root = new BorderPane();
        Label stats = new Label();
        stats.textProperty().bind(frameStats.textProperty());

        root.setCenter(ballContainer);
        root.setBottom(stats);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        startAnimation(ballContainer);
        audioRenderer.startAudio();

    }

    private void startAnimation(Pane ballContainer) {
        LongProperty lastUpdateTime = new SimpleLongProperty(0);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long timestamp) {
                if (lastUpdateTime.get() > 0) {
                    long elapsedTime = timestamp - lastUpdateTime.get();
                    try {
                        statistics.write(elapsedTime / 1e6 + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    checkCollisions(ballContainer.getWidth(), ballContainer.getHeight());
                    updateWorld(elapsedTime);
                    audioRenderer.updateAudio(timestamp);
                    frameStats.addFrame(elapsedTime);
                }
                lastUpdateTime.set(timestamp);
            }

        };
        timer.start();
    }

    private void updateWorld(long elapsedTime) {
        // double elapsedSeconds = elapsedTime / 1e9;
        double elapsedSeconds = 1.0 / 60;
        for (Ball b : balls) {
            b.setCenterX(b.getCenterX() + elapsedSeconds * b.getXVelocity());
            b.setCenterY(b.getCenterY() + elapsedSeconds * b.getYVelocity());
        }
    }

    private void checkCollisions(double maxX, double maxY) {
        for (ListIterator<Ball> slowIt = balls.listIterator(); slowIt.hasNext(); ) {
            Ball b1 = slowIt.next();
            // check wall collisions:
            double xVel = b1.getXVelocity();
            double yVel = b1.getYVelocity();
            if ((b1.getCenterX() - b1.getRadius() <= 0 && xVel < 0)
                || (b1.getCenterX() + b1.getRadius() >= maxX && xVel > 0)) {
                b1.setXVelocity(-xVel);
            }
            if ((b1.getCenterY() - b1.getRadius() <= 0 && yVel < 0)
                || (b1.getCenterY() + b1.getRadius() >= maxY && yVel > 0)) {
                b1.setYVelocity(-yVel);
            }
            for (ListIterator<Ball> fastIt = balls.listIterator(slowIt.nextIndex()); fastIt.hasNext(); ) {
                Ball b2 = fastIt.next();
                // performance hack: both colliding(...) and bounce(...) need deltaX and deltaY, so compute them once here:
                double deltaX = b2.getCenterX() - b1.getCenterX();
                double deltaY = b2.getCenterY() - b1.getCenterY();
                if (colliding(b1, b2, deltaX, deltaY)) {
                    bounce(b1, b2, deltaX, deltaY);
                }
            }
        }
    }


    public boolean colliding(Ball b1, Ball b2, double deltaX, double deltaY) {
        // square of distance between balls is s^2 = (x2-x1)^2 + (y2-y1)^2
        // balls are "overlapping" if s^2 < (r1 + r2)^2
        // We also check that distance is decreasing, i.e.
        // d/dt(s^2) < 0:
        // 2(x2-x1)(x2'-x1') + 2(y2-y1)(y2'-y1') < 0

        double radiusSum = b1.getRadius() + b2.getRadius();
        if (deltaX * deltaX + deltaY * deltaY <= radiusSum * radiusSum) {
            return deltaX * (b2.getXVelocity() - b1.getXVelocity())
                + deltaY * (b2.getYVelocity() - b1.getYVelocity()) < 0;
        }
        return false;
    }

    private void bounce(Ball b1, Ball b2, double deltaX, double deltaY) {
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double unitContactX = deltaX / distance;
        double unitContactY = deltaY / distance;

        double xVelocity1 = b1.getXVelocity();
        double yVelocity1 = b1.getYVelocity();
        double xVelocity2 = b2.getXVelocity();
        double yVelocity2 = b2.getYVelocity();

        double u1 = xVelocity1 * unitContactX + yVelocity1 * unitContactY; // velocity of ball 1 parallel to contact vector
        double u2 = xVelocity2 * unitContactX + yVelocity2 * unitContactY; // same for ball 2

        double massSum = b1.getMass() + b2.getMass();
        double massDiff = b1.getMass() - b2.getMass();

        double v1 = (2 * b2.getMass() * u2 + u1 * massDiff) / massSum; // These equations are derived for one-dimensional collision by
        double v2 = (2 * b1.getMass() * u1 - u2 * massDiff) / massSum; // solving equations for conservation of momentum and conservation of energy

        double u1PerpX = xVelocity1 - u1 * unitContactX; // Components of ball 1 velocity in direction perpendicular
        double u1PerpY = yVelocity1 - u1 * unitContactY; // to contact vector. This doesn't change with collision
        double u2PerpX = xVelocity2 - u2 * unitContactX; // Same for ball 2....
        double u2PerpY = yVelocity2 - u2 * unitContactY;

        b1.setXVelocity(v1 * unitContactX + u1PerpX);
        b1.setYVelocity(v1 * unitContactY + u1PerpY);
        b2.setXVelocity(v2 * unitContactX + u2PerpX);
        b2.setYVelocity(v2 * unitContactY + u2PerpY);

    }

    private void createBalls(int numBalls, double minRadius, double maxRadius, double minSpeed, double maxSpeed, double initialX, double initialY) {
        Random rng = new Random();
        for (int i = 0; i < numBalls; i++) {
            double radius = minRadius + (maxRadius - minRadius) * rng.nextDouble();
            double mass = Math.pow((radius / 40), 3);

            double speed = minSpeed + (maxSpeed - minSpeed) * rng.nextDouble();
            double angle = 2 * Math.PI * rng.nextDouble();
            Ball ball = new Ball(initialX, initialY, radius, speed * Math.cos(angle),
                speed * Math.sin(angle), mass);
            ball.getView().setFill(COLORS[i % COLORS.length]);
//            ball.getView().setFill(i==0 ? RED : TRANSPARENT);
            balls.add(ball);
        }
    }

    private void constrainBallsOnResize(Pane ballContainer) {
        ballContainer.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < oldValue.doubleValue()) {
                for (Ball b : balls) {
                    double max = newValue.doubleValue() - b.getRadius();
                    if (b.getCenterX() > max) {
                        b.setCenterX(max);
                    }
                }
            }
        });

        ballContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < oldValue.doubleValue()) {
                for (Ball b : balls) {
                    double max = newValue.doubleValue() - b.getRadius();
                    if (b.getCenterY() > max) {
                        b.setCenterY(max);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
