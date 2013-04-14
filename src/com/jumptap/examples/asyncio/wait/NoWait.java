package com.jumptap.examples.asyncio.wait;

/**
 * Baseline comparison of something that should be completely removed during
 * JIT optimizations.
 *
 * @author rgebhardt
 */
public class NoWait implements ExpensiveWait {

    @Override
    public String wait(String bogus) {
        return "";
    }

}
