import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;
import java.util.stream.IntStream;

public class VirtualThreadLogbackDeadlock {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadLogbackDeadlock.class);

    public static void main(String... args) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int workers = availableProcessors + 1;
        BlockingQueue<Long> queue = new ArrayBlockingQueue<>(workers);

        List<Thread> threads = IntStream.range(0, workers)
                .mapToObj(i -> {
                    boolean shouldPin = i != 0;
                    return Thread.ofVirtual().name(shouldPin ? "pinned-" + i : "unpinned").start(makeLogTask(queue, shouldPin));
                })
                .toList();

        for (long i = 0; ; i++) {
            try {
                log.info("Ping i={}", i);
                if (!queue.offer(i, 10, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Deadlock detected");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Runnable makeLogTask(BlockingQueue<Long> queue, boolean shouldPin) {
        return () -> logMaybePinned(queue, shouldPin);
    }

    private static void logMaybePinned(BlockingQueue<Long> queue, boolean shouldPin) {
        LongConsumer doLog = i -> log.info("Pong i={}, pinned = {}", i, shouldPin);
        while (true) {
            long i;
            try {
                i = queue.take();
            } catch (InterruptedException e) {
                break;
            }
            if (shouldPin) {
                synchronized (new Object()) {
                    doLog.accept(i);
                }
            } else {
                doLog.accept(i);
            }
        }
    }

}
