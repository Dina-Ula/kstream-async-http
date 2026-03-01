package demo.rcbs;

import java.util.concurrent.*;
import java.util.function.Supplier;

/** Offloads blocking work to an executor and propagates caller TCCL. */
public final class TcclAsyncRunner {
    private final Executor executor;

    public TcclAsyncRunner(Executor executor) {
        this.executor = executor;
    }

    public <R> CompletableFuture<R> runAsync(Supplier<R> supplier) {
        final ClassLoader callerCl = Thread.currentThread().getContextClassLoader();

        return CompletableFuture.supplyAsync(() -> {
            Thread t = Thread.currentThread();
            ClassLoader old = t.getContextClassLoader();
            try {
                t.setContextClassLoader(callerCl);
                return supplier.get(); // blocking happens here (worker thread)
            } finally {
                t.setContextClassLoader(old);
            }
        }, executor);
    }
}