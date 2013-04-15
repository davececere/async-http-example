package com.jumptap.examples.asyncio;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.jumptap.examples.asyncio.client.AsynchronousClient;
import com.jumptap.examples.asyncio.client.HttpFacade;
import com.jumptap.examples.asyncio.client.SynchronousClient;
import com.jumptap.examples.asyncio.wait.ArithmeticLoop;
import com.jumptap.examples.asyncio.wait.ExpensiveWait;

/**
 * Compare two algorithms which perform some CPU intensive operation followed by
 * a http call to a stub server. One algorithm uses a synchronous call, the
 * other uses async io.
 * 
 * @author rgebhardt
 * @see {@link Main}
 */
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "benchmark-sync-async")
public class SyncVsAsyncHttpClientBenchmark {
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private final static int REQUESTS_PER_ROUND = 100;

    private final static int WARMUP_ROUNDS = 1000;
    private final static int BENCHMARK_ROUNDS = 100;

    private ExpensiveWait waiter = new ArithmeticLoop(1000);
    private FakeServer server = new FakeServer(10);
    private HttpFacade sync = new SynchronousClient(server.getAddress());
    private HttpFacade async = new AsynchronousClient(server.getAddress());

    private static String collectedOutput = "";

    @AfterClass
    public static void flushOutput() {
        System.out.println("Output " + collectedOutput.hashCode());
    }
    
    @Test
    @BenchmarkOptions(benchmarkRounds = BENCHMARK_ROUNDS, warmupRounds = WARMUP_ROUNDS)
    public void syncClient() {
        for (int i = 0; i < REQUESTS_PER_ROUND; i++) {
            String output = waiter.wait("foo");
            collectedOutput += output.charAt((int)(Math.random() * output.length()));
            sync.get();
        }
        while (!async.isDone());
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = BENCHMARK_ROUNDS, warmupRounds = WARMUP_ROUNDS)
    public void asyncClient() {
        for (int i = 0; i < REQUESTS_PER_ROUND; i++) {
            String output = waiter.wait("bar");
            collectedOutput += output.charAt((int)(Math.random() * output.length()));
            async.get();
        }
        while (!async.isDone());
    }
}