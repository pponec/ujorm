package org.ujorm.tools.thread;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/** Tested on JRE 1.8.0_231 */
public class SampleTest {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Before
    public void tearUp() {
        String msg;
        msg = String.format(">>> Free memory: %.1f/%.1f MB.",
                Runtime.getRuntime().freeMemory() / 1_000_000f,
                Runtime.getRuntime().maxMemory() / 1_000_000f);
        System.out.println(msg);
    }

    /** 1_000 jobs take 32 seconds on ForkJoinPool with 1_000 parallelism.
     * 10_000 jobs take 313 seconds on ForkJoinPool with 10_000 parallelism,
     * 32_767 jobs take 313 seconds on ForkJoinPool with 32_767 parallelism,
     */
    @Test
    public void parallelStreamTest() throws InterruptedException, ExecutionException {
        System.out.println("parallelStreamTest");

        final int parallelism = 100; // 32_767;
        final Duration jobDuration = Duration.ofSeconds(1);
        final ForkJoinPool threadPool = new ForkJoinPool(parallelism);
        final int sum = threadPool.submit(() -> Collections.nCopies(parallelism, 1)
                        .parallelStream()
                        .map(t -> job(t, jobDuration))
                        .mapToInt(i -> i)
                        .sum()
        ).get();

        threadPool.shutdown();
        System.out.println(sum);
    }

    /** 1_000 jobs take 1.2 seconds by 1_000 threads,
     * 10_000 jobs take 9.2 seconds by 10_000 threads,
     * 32_767 threads throws java.lang.OutOfMemoryError.
     */
    @Test
    public void completableFutureTest() {

        final int parallelism = 1_000; // Short.MAX_VALUE; // 10_000;
        final Duration jobDuration = Duration.ofSeconds(1);
        final ExecutorService threadPool = Executors.newFixedThreadPool(parallelism);
        final int sum = Collections.nCopies(parallelism, 1)
                .stream()
                .map(t -> CompletableFuture.supplyAsync(() -> job(t, jobDuration), threadPool))
                .collect(Collectors.toList()).stream() // For parallel processing!
                .map(CompletableFuture::join)
                .mapToInt(i -> i)
                .sum();
        threadPool.shutdown();
        System.out.println(sum);
    }
    /** 1_000 jobs take 1.2 seconds by 1_000 threads,
     * 10_000 jobs take 9.2 seconds by 10_000 threads,
     * 32_767 threads throws java.lang.OutOfMemoryError.
     */
    @Test
    public void runnableTest() {

        final int parallelism = 10_000; // Short.MAX_VALUE; // 10_000;
        final Duration jobDuration = Duration.ofSeconds(1);
        final AtomicInteger sum = new AtomicInteger();
        final ThreadGroup tg = new ThreadGroup ("myThreadGroup");

        Collections.nCopies(parallelism, 1)
                .stream()
                .forEach(i -> new Thread(tg, () -> sum.addAndGet(job(i, jobDuration)))
                .start());

        while (tg.activeCount() > 0) {
            job(1, Duration.ofMillis(100));
        }

        System.out.println(sum);
    }

    /** 1_000 jobs take 127 seconds on DEFAULT ForkJoinPool. */
    @Ignore
    @Test
    public void defaultThreadPoolTest() {

        final int parallelism = 1_000;
        final Duration jobDuration = Duration.ofSeconds(1);
        final int sum = Collections.nCopies(parallelism, 1)
                .parallelStream()
                .map(t -> job(t, jobDuration))
                .mapToInt(i -> i)
                .sum();

        System.out.println(sum);
    }

    private int job(int value, Duration duration) {
        try {
            logger.log(Level.FINEST, "Sleeping {0} sec", duration.getSeconds());
            Thread.sleep(duration.toMillis());
            return value;
        } catch (InterruptedException e) {
            throw new IllegalStateException("An interruption of the test", e);
        }
    }

    /** ReactiveX / RxJava */
    @Test
    public void testRxJavaMethod() throws InterruptedException {
        List<Integer> nums = IntStream.rangeClosed(1, 10)
                .boxed()
                .collect(Collectors.toList());
        AsyncStreamBuilder<String> builder = new AsyncStreamBuilder<>(nums.size());

        Flowable.fromIterable(nums)
                .parallel()
                .runOn(Schedulers.io(), nums.size())
                .map(v -> {
                    Thread.sleep(1_000);
                    return "item-" + v;
                })
                .sequential()
                .blockingSubscribe(t -> builder.add(t));

        builder.stream()
                .forEach(v -> System.out.println(">>> result: " +  v));
    }
}
