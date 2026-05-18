package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import java.util.ArrayList;
import java.util.List;

public final class GameState {
    private final Level level;
    private final Snake snake;
    private final List<Pin> pins;
    private final Status status;
    private final Direction pendingDirection;

    public GameState(Level level, Snake snake, List<Pin> pins, Status status, Direction pendingDirection) {
        this.level = level;
        this.snake = snake;
        this.pins = pins;
        this.status = status;
        this.pendingDirection = pendingDirection;
    }

    public Level level() {
        return level;
    }

    public Snake snake() {
        return snake;
    }

    public List<Pin> pins() {
        return pins;
    }

    public Status status() {
        return status;
    }

    public Direction pendingDirection() {
        return pendingDirection;
    }

    public GameState tick() {
        if (!status.isRunning() || pendingDirection == Direction.NONE) {
            return this;
        }

        Position next = snake.nextHead(pendingDirection);

        if (!level.isInside(next)) {
            return new GameState(level, snake, pins, Status.LOST_OUT_OF_BOUNDS, Direction.NONE);
        }

        if (level.cellAt(next) == CellType.WALL) {
            return new GameState(level, snake, pins, status, Direction.NONE);
        }

        if (snake.occupies(next)) {
            return new GameState(level, snake, pins, Status.LOST_SELF_COLLISION, Direction.NONE);
        }

        for (int i = 0; i < pins.size(); i++) {
            Pin pin = pins.get(i);

            if (pin.position().equals(next)) {
                if (pin.state().isSet() || pin.activationDirection() != pendingDirection) {
                    return new GameState(level, snake, pins, status, Direction.NONE);
                }

                List<Pin> newPins = new ArrayList<>(pins);
                newPins.set(i, pin.withState(Pin.State.HIGH));

                boolean allSet = true;
                for (Pin p : newPins) {
                    if (!p.state().isSet()) {
                        allSet = false;
                        break;
                    }
                }

                return new GameState(
                    level,
                    snake,
                    newPins,
                    allSet ? Status.WON : Status.RUNNING,
                    Direction.NONE
                );
            }
        }

        List<Position> newBody = new ArrayList<>();
        newBody.add(next);

        List<Position> oldBody = snake.body();
        for (int i = 0; i < oldBody.size() - 1; i++) {
            newBody.add(oldBody.get(i));
        }

        Snake newSnake = new Snake(newBody);

        return new GameState(level, newSnake, pins, Status.RUNNING, pendingDirection);
    }

    public enum Status {
        RUNNING,
        WON,
        LOST_SELF_COLLISION,
        LOST_OUT_OF_BOUNDS;

        public boolean isRunning() {
            return this == RUNNING;
        }
    }
}
