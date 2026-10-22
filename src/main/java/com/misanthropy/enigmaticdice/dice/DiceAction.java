package com.misanthropy.enigmaticdice.dice;

public interface DiceAction {
    default boolean canRun(DiceContext context) {
        return true;
    }

    Result run(DiceContext context);

    enum Result {
        DONE,
        PENDING,
        FAILED
    }
}
