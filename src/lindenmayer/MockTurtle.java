package lindenmayer;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tortue "bidon" qui maintient les états correctement mais ne dessine rien
 */
public class MockTurtle implements Turtle {

    // Classe imbriquée State pour encapsuler l'état
    private static class State {
        double x, y, angle;

        State(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        State(State other) {
            this.x = other.x;
            this.y = other.y;
            this.angle = other.angle;
        }
    }

    private State currentState;
    private Deque<State> stateStack;
    private double unitStep = 1.0;
    private double unitAngle = 90.0;

    public MockTurtle() {
        currentState = new State(0, 0, 90);
        stateStack = new ArrayDeque<>();
    }

    @Override
    public void draw() {
        double radians = Math.toRadians(currentState.angle);
        currentState.x += unitStep * Math.cos(radians);
        currentState.y += unitStep * Math.sin(radians);
    }

    @Override
    public void move() {
        double radians = Math.toRadians(currentState.angle);
        currentState.x += unitStep * Math.cos(radians);
        currentState.y += unitStep * Math.sin(radians);
    }

    @Override
    public void turnR() {
        currentState.angle -= unitAngle;
    }

    @Override
    public void turnL() {
        currentState.angle += unitAngle;
    }

    @Override
    public void push() {
        stateStack.push(new State(currentState));
    }

    @Override
    public void pop() {
        if (!stateStack.isEmpty()) {
            currentState = stateStack.pop();
        }
    }

    @Override
    public void stay() {
        // Ne fait rien
    }

    @Override
    public void init(Point2D pos, double angle) {
        currentState = new State(pos.getX(), pos.getY(), angle);
        stateStack.clear();
    }

    @Override
    public Point2D getPosition() {
        return new Point2D.Double(currentState.x, currentState.y);
    }

    @Override
    public double getAngle() {
        return currentState.angle;
    }

    @Override
    public void setUnits(double step, double delta) {
        this.unitStep = step;
        this.unitAngle = delta;
    }

    @Override
    public double getUnitStep() {
        return unitStep;
    }

    @Override
    public double getUnitAngle() {
        return unitAngle;
    }
}