package com.jumptap.examples.asyncio.wait;

/**
 * Wait in a way that is expensive on the processor.
 */
public interface ExpensiveWait {
    /**
     * Wait for a while. A while is implementation specific.
     * 
     * @param bogus
     *            A bogus parameter that should be used in wait logic.
     * @return Return a parameter. The caller should make sure to do something
     *         with this.
     */
    String wait(String bogus);
}
