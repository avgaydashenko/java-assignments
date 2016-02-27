import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class LazyFactoryTest {

    private static class MySupplier implements Supplier<Integer> {

        private int counter = 0;

        public Integer get() {
            counter += 1;
            return counter;
        }
    }

    private void testMultiThread(final Lazy<Integer> lazy) throws InterruptedException {

        Thread[] thread = new Thread[10];
        CyclicBarrier barrier = new CyclicBarrier(10);

        for (int i = 0; i < 10; i++) {

            thread[i] = new Thread(() -> {

                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

                assertEquals((Integer) 1, lazy.get());
                assertEquals((Integer) 1, lazy.get());
            });
        }

        for (int i = 0; i < 10; i++) {
            thread[i].start();
        }

        for (int i = 0; i < 10; i++) {
            thread[i].join();
        }

    }

    @Test
    public void testCreateSingleThreadLazy() throws Exception {

        MySupplier supplier = new MySupplier();

        final Lazy<Integer> lazy = LazyFactory.createSingleThreadLazy(supplier);

        assertEquals(0, supplier.counter);

        final Integer a = lazy.get();
        final Integer b = lazy.get();

        assertEquals((Integer) 1, lazy.get());
        assertSame(a, b);
    }

    @Test
    public void testCreateSingleThreadLazyWithNull() throws Exception {

        final Supplier<Integer> nullSupplier = () -> null;

        final Lazy<Integer> lazy = LazyFactory.createSingleThreadLazy(nullSupplier);

        assertNull(lazy.get());
    }

    @Test
    public void testCreateMultiThreadLazy() throws Exception {

        MySupplier supplier = new MySupplier();

        final Lazy<Integer> lazy = LazyFactory.createMultiThreadLazy(supplier);

        testMultiThread(lazy);
    }

    @Test
    public void testCreateMultiThreadLockFreeLazy() throws Exception {

        MySupplier supplier = new MySupplier();

        final Lazy<Integer> lazy = LazyFactory.createMultiThreadLockFreeLazy(supplier);

        testMultiThread(lazy);
    }
}