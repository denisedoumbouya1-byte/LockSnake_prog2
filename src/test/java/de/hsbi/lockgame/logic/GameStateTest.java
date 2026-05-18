package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    private Level level(List<Pin> pins, Position snakeStart) {
        CellType[][] cells = new CellType[5][5];

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                cells[x][y] = CellType.EMPTY;
            }
        }

        return new Level(5, 5, cells, pins, snakeStart);
    }

    private Level levelWithWall(Position wall, Position snakeStart) {
        CellType[][] cells = new CellType[5][5];

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                cells[x][y] = CellType.EMPTY;
            }
        }

        cells[wall.x()][wall.y()] = CellType.WALL;

        return new Level(5, 5, cells, List.of(), snakeStart);
    }

    @Test
    void givenInitialState_whenCreated_thenValuesAreStored() {
        // given
        Position start = new Position(1, 1);
        Level level = level(List.of(), start);
        Snake snake = new Snake(List.of(start));

        // when
        GameState state = new GameState(
            level,
            snake,
            List.of(),
            GameState.Status.RUNNING,
            Direction.NONE
        );

        // then
        assertEquals(level, state.level());
        assertEquals(snake, state.snake());
        assertEquals(GameState.Status.RUNNING, state.status());
        assertEquals(Direction.NONE, state.pendingDirection());
    }

    @Test
    void givenNoDirection_whenTick_thenStateDoesNotChange() {
        // given
        Position start = new Position(1, 1);
        Level level = level(List.of(), start);
        GameState state = new GameState(
            level,
            new Snake(List.of(start)),
            List.of(),
            GameState.Status.RUNNING,
            Direction.NONE
        );

        // when
        GameState next = state.tick();

        // then
        assertSame(state, next);
    }

    @Test
    void givenDirectionRight_whenTick_thenSnakeMovesRight() {
        // given
        Position start = new Position(1, 1);
        Level level = level(List.of(), start);
        GameState state = new GameState(
            level,
            new Snake(List.of(start)),
            List.of(),
            GameState.Status.RUNNING,
            Direction.RIGHT
        );

        // when
        GameState next = state.tick();

        // then
        assertEquals(new Position(2, 1), next.snake().head());
        assertEquals(GameState.Status.RUNNING, next.status());
    }

    @Test
    void givenWallInFront_whenTick_thenSnakeIsBlocked() {
        // given
        Position start = new Position(1, 1);
        Level level = levelWithWall(new Position(2, 1), start);
        GameState state = new GameState(
            level,
            new Snake(List.of(start)),
            List.of(),
            GameState.Status.RUNNING,
            Direction.RIGHT
        );

        // when
        GameState next = state.tick();

        // then
        assertEquals(start, next.snake().head());
        assertEquals(Direction.NONE, next.pendingDirection());
        assertEquals(GameState.Status.RUNNING, next.status());
    }

    @Test
    void givenSnakeLeavesLevel_whenTick_thenGameIsLost() {
        // given
        Position start = new Position(0, 0);
        Level level = level(List.of(), start);
        GameState state = new GameState(
            level,
            new Snake(List.of(start)),
            List.of(),
            GameState.Status.RUNNING,
            Direction.LEFT
        );

        // when
        GameState next = state.tick();

        // then
        assertEquals(GameState.Status.LOST_OUT_OF_BOUNDS, next.status());
    }

    @Test
    void givenSnakeRunsIntoItself_whenTick_thenGameIsLost() {
        // given
        Level level = level(List.of(), new Position(2, 2));
        Snake snake = new Snake(List.of(
            new Position(2, 2),
            new Position(1, 2),
            new Position(1, 1)
        ));

        GameState state = new GameState(
            level,
            snake,
            List.of(),
            GameState.Status.RUNNING,
            Direction.LEFT
        );

        // when
        GameState next = state.tick();

        // then
        assertEquals(GameState.Status.LOST_SELF_COLLISION, next.status());
    }

    @Test
    void givenPinWithWrongDirection_whenTick_thenPinIsNotActivated() {
        // given
        Position start = new Position(1, 1);
        Pin pin = new Pin(new Position(2, 1), Pin.State.LOW, Direction.UP);
        Level level = level(List.of(pin), start);

        GameState state = new GameState(
            level,
            new Snake(List.of(start)),
            List.of(pin),
            GameState.Status.RUNNING,
            Direction.RIGHT
        );

        // when
        GameState next = state.tick();

        // then
        assertEquals(Pin.State.LOW, next.pins().getFirst().state());
        assertEquals(Direction.NONE, next.pendingDirection());
    }

    @Test
    void givenPinWithCorrectDirection_whenTick_thenPinIsActivated() {
        // given
        Position start = new Position(1, 1);
        Pin pin = new Pin(new Position(2, 1), Pin.State.LOW, Direction.RIGHT);
        Level level = level(List.of(pin), start);

        GameState state = new GameState(
            level,
            new Snake(List.of(start)),
            List.of(pin),
            GameState.Status.RUNNING,
            Direction.RIGHT
        );

        // when
        GameState next = state.tick();

        // then
        assertEquals(Pin.State.HIGH, next.pins().getFirst().state());
    }

    @Test
    void givenLastPinActivated_whenTick_thenGameIsWon() {
        // given
        Position start = new Position(1, 1);
        Pin pin = new Pin(new Position(2, 1), Pin.State.LOW, Direction.RIGHT);
        Level level = level(List.of(pin), start);

        GameState state = new GameState(
            level,
            new Snake(List.of(start)),
            List.of(pin),
            GameState.Status.RUNNING,
            Direction.RIGHT
        );

        // when
        GameState next = state.tick();

        // then
        assertEquals(GameState.Status.WON, next.status());
    }

    @Test
    void givenGameAlreadyWon_whenTick_thenStateDoesNotChange() {
        // given
        Position start = new Position(1, 1);
        Level level = level(List.of(), start);
        GameState state = new GameState(
            level,
            new Snake(List.of(start)),
            List.of(),
            GameState.Status.WON,
            Direction.RIGHT
        );

        // when
        GameState next = state.tick();

        // then
        assertSame(state, next);
    }
}
