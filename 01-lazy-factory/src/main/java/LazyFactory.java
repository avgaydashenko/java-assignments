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

        return new Lazy<T>() {

            private AtomicReference<T> result = null;

            public T get() {

                result.compareAndSet(null, supplier.get());
                return result.get();
            }
        };
    }
}
