package com.jumptap.examples.asyncio;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.Clock;
import com.jumptap.examples.asyncio.wait.ArithmeticLoop;
import com.jumptap.examples.asyncio.wait.ExpensiveWait;
import com.jumptap.examples.asyncio.wait.HashWait;
import com.jumptap.examples.asyncio.wait.NoWait;

/**
 * Compare algorithms for burning CPU to make sure we've addressed JIT
 * optimizations.
 *
 * I believe we want to tune these to burn 0.01s.
 *
 * @author rgebhardt
 * @see {@link Main}
 */
public class WaitBenchmark {
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private ExpensiveWait hash = new HashWait(10000);
    private ExpensiveWait math = new ArithmeticLoop(3000);
    private ExpensiveWait none = new NoWait();

    @BeforeClass
    public static void setUp() {
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 1000, clock = Clock.NANO_TIME)
    public void hashWait() {
        hash.wait("foo");
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 1000, clock = Clock.NANO_TIME)
    public void mathWait() {
        math.wait("bar");
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 1000, clock = Clock.NANO_TIME)
    public void noWait() {
        none.wait("baz");
    }
}
