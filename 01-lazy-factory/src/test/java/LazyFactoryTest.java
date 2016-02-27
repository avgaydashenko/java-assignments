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

    private static class MyNullSupplier implements Supplier<Integer> {

        private int counter = 0;

        public Integer get() {
            counter += 1;
            if (counter == 1)
                return null;
            else
                return counter;
        }
    }

    private void testMultiThread(final Lazy<Integer> lazy, boolean nullTest) throws InterruptedException {

        Thread[] thread = new Thread[10];
        CyclicBarrier barrier = new CyclicBarrier(10);

        for (int i = 0; i < 10; i++) {

            thread[i] = new Thread(() -> {

                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

                if (nullTest) {
                    assertEquals(null, lazy.get());
                    assertEquals(null, lazy.get());
                } else {
                    assertEquals((Integer) 1, lazy.get());
                    assertEquals((Integer) 1, lazy.get());
                }
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

        final MySupplier supplier = new MySupplier();
        assertEquals(0, supplier.counter);

        final Lazy<Integer> lazy = LazyFactory.createSingleThreadLazy(supplier);

        final Integer a = lazy.get();
        final Integer b = lazy.get();

        assertEquals((Integer) 1, a);
        assertEquals(1, supplier.counter);

        assertEquals((Integer) 1, lazy.get());
        assertSame(a, b);
    }

    @Test
    public void testCreateSingleThreadLazyWithNull() throws Exception {

        final MyNullSupplier supplier = new MyNullSupplier();
        assertEquals(0, supplier.counter);

        final Lazy<Integer> lazy = LazyFactory.createSingleThreadLazy(supplier);

        final Integer a = lazy.get();
        final Integer b = lazy.get();

        assertEquals(null, a);
        assertEquals(1, supplier.counter);

        assertEquals(null, lazy.get());
        assertSame(a, b);
    }

    @Test
    public void testCreateMultiThreadLazy() throws Exception {

        final MySupplier supplier = new MySupplier();
        assertEquals(0, supplier.counter);

        final Lazy<Integer> lazy = LazyFactory.createMultiThreadLazy(supplier);

        testMultiThread(lazy, false);
        assertEquals(1, supplier.counter);
    }

    @Test
    public void testCreateMultiThreadLazyWithNull() throws Exception {

        final MyNullSupplier supplier = new MyNullSupplier();
        assertEquals(0, supplier.counter);

        final Lazy<Integer> lazy = LazyFactory.createMultiThreadLazy(supplier);

        testMultiThread(lazy, true);
        assertEquals(1, supplier.counter);
    }

    @Test
    public void testCreateMultiThreadLockFreeLazy() throws Exception {

        MySupplier supplier = new MySupplier();
        assertEquals(0, supplier.counter);

        final Lazy<Integer> lazy = LazyFactory.createMultiThreadLockFreeLazy(supplier);

        testMultiThread(lazy, false);
        assertEquals(1, supplier.counter);
    }
}