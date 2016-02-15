import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.*;

public class LazyFactoryTest {

    final Supplier<Integer> supplier = new Supplier<Integer>() {
        public Integer get() {
            return 10000000;
        }
    };

    void testMultiThread(final Lazy<Integer> lazy) throws InterruptedException {

        Thread[] thread = new Thread[10];
        final Integer[] result = new Integer[10];

        for (int i = 0; i < 10; i++) {

            final int tmp = i;

            thread[tmp] = new Thread(new Runnable() {
                public void run() {
                    result[tmp] = lazy.get();
                }
            });

            thread[tmp].start();
        }

        for (int i = 0; i < 10; i++) {
            thread[i].join();
        }

        for (int i = 0; i < 9; i++) {
            assertEquals(result[i], result[9]);
        }
    }

    @Test
    public void testCreateSingleThreadLazy() throws Exception {

        final Lazy<Integer> lazy = LazyFactory.createSingleThreadLazy(supplier);
        final Integer a = lazy.get();
        final Integer b = lazy.get();

        assertEquals(a, b);
    }

    @Test
    public void testCreateSingleThreadLazyWithNull() throws Exception {

        final Supplier<Integer> nullSupplier = new Supplier<Integer>() {
            public Integer get() {
                return null;
            }
        };

        final Lazy<Integer> lazy = LazyFactory.createSingleThreadLazy(nullSupplier);
        final Integer a = lazy.get();

        assertEquals(a, null);
    }

    @Test
    public void testCreateMultiThreadLazy() throws Exception {

        final Lazy<Integer> lazy = LazyFactory.createMultiThreadLazy(supplier);

        testMultiThread(lazy);
    }

    @Test
    public void testCreateMultiThreadLockFreeLazy() throws Exception {

        final Lazy<Integer> lazy = LazyFactory.createMultiThreadLockFreeLazy(supplier);

        testMultiThread(lazy);
    }
}