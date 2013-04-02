package com.jumptap.examples.asyncio.wait;

import java.util.Date;


/**
 * Loop a certain number of time performing math.
 */
public class ArithmeticLoop implements ExpensiveWait {
    private int max;

    /**
     * Create a new waiter that will do math until max is reached.
     * 
     * @param max The max amount of operations performed.
     */
    public ArithmeticLoop(int max) {
        this.max = max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String wait(String bogus) {
        for (int i = 0; i < max; i++) {
            bogus += new Date().getTime() % i;
        }

        return bogus;
    }
}
