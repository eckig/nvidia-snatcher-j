package util;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

public class Pool<T>
{
    private final BlockingQueue<PooledImpl> mPool;
    private final int mSize;
    private final Supplier<T> mCreator;

    public Pool(final int pSize, final Supplier<T> pCreator)
    {
        mSize = pSize;
        mCreator = Objects.requireNonNull(pCreator);
        mPool = new ArrayBlockingQueue<>(pSize);
    }

    public IPooled<T> get() throws InterruptedException
    {
        if (mPool.size() < mSize)
        {
            synchronized (this)
            {
                if (mPool.size() < mSize)
                {
                    mPool.add(new PooledImpl(mCreator.get()));
                }
            }
        }
        return mPool.take();
    }

    public interface IPooled<T> extends AutoCloseable
    {
        void destroyed();

        T element();
    }

    private class PooledImpl implements IPooled<T>
    {

        private final T imElement;
        private volatile boolean imDestroyed = false;

        private PooledImpl(final T pElement)
        {
            imElement = pElement;
        }

        @Override
        public void destroyed()
        {
            imDestroyed = true;
        }

        @Override
        public T element()
        {
            return imElement;
        }

        @Override
        public void close()
        {
            if (!imDestroyed)
            {
                mPool.add(this);
            }
        }
    }
}
