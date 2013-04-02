package com.jumptap.examples.asyncio;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.jumptap.examples.asyncio.wait.ExpensiveWait;
import com.jumptap.examples.asyncio.wait.HashWait;

/**
 * Compare two algorithms which perform some CPU intensive operation
 * followed by a http call to a stub server. One algorithm uses a synchronous
 * call, the other uses async io.
 *
 * @author rgebhardt
 * @see {@link Main}
 */
public class SyncVsAsyncHttpClientBenchmark {
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private ExpensiveWait hash = new HashWait(10000);
    private ExpensiveWait math = new HashWait(10000);

    @BeforeClass
    public static void setUp() {
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 1000)
    public void syncClient() {
        hash.wait("foo");
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 1000)
    public void asyncClient() {
        math.wait("bar");
    }
}
