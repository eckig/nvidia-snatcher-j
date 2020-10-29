package util;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

public class Pool<T>
{
    private final BlockingQueue<IPooled<T>> mPool;
    private final Supplier<T> mCreator;

    public Pool(final int pSize, final Supplier<T> pCreator)
    {
        mCreator = Objects.requireNonNull(pCreator);
        mPool = new ArrayBlockingQueue<>(pSize);
        for (int i = 0; i < pSize; i++)
        {
            mPool.add(new LazyPooledImpl());
        }
    }

    public IPooled<T> get() throws InterruptedException
    {
        return mPool.take();
    }

    private T create()
    {
        return mCreator.get();
    }

    public interface IPooled<T> extends AutoCloseable
    {
        void destroyed();

        T element();
    }

    private class LazyPooledImpl implements IPooled<T>
    {

        private volatile T imElement;
        private volatile boolean imDestroyed = false;

        @Override
        public void destroyed()
        {
            imDestroyed = true;
        }

        @Override
        public T element()
        {
            if (imElement == null && !imDestroyed)
            {
                synchronized (this)
                {
                    if (imElement == null)
                    {
                        imElement = create();
                    }
                }
            }
            return imElement;
        }

        @Override
        public void close()
        {
            mPool.add(!imDestroyed ? this : new LazyPooledImpl());
        }
    }
}
