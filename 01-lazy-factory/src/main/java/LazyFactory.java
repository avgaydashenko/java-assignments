import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class LazyFactory {

    public static <T> Lazy<T> createSingleThreadLazy(final Supplier<T> supplier) {

        return new Lazy<T>() {

            private T result = null;
            private boolean isCalculated = false;

            public T get() {

                if (!isCalculated) {
                    result = supplier.get();
                    isCalculated = true;
                }

                return result;
            }
        };
    }

    public static <T> Lazy<T> createMultiThreadLazy(final Supplier<T> supplier) {

        return new Lazy<T>() {

            private volatile T result = null;
            private volatile boolean isCalculated = false;

            public T get() {

                if (!isCalculated) {
                    synchronized (this) {
                        if (!isCalculated) {
                            result = supplier.get();
                            isCalculated = true;
                        }
                    }
                }

                return result;
            }
        };
    }

    public static <T> Lazy<T> createMultiThreadLockFreeLazy(final Supplier<T> supplier) {

        return new multiThreadLockFreeLazy<>(supplier);
    }

    private static class multiThreadLockFreeLazy<T> implements Lazy<T> {

        private Supplier<T> supplier;
        private final AtomicReference<T> result = new AtomicReference<>(null);

        multiThreadLockFreeLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {

            Supplier<T> temporarySupplier = supplier;

            if (temporarySupplier == null) {
                return result.get();
            }

            if (result.compareAndSet(null, temporarySupplier.get())) {
                supplier = null;
            }
            return result.get();
        }
    }
}
