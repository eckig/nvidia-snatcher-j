package main;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gargoylesoftware.htmlunit.WebClient;

import notify.INotify;
import util.Pool;

final class ScraperEnvironment
{
    private final long mWaitTimeout;
    private final boolean mNotifyOnStatusChanged;
    private final List<INotify> mToNotify;
    private final ExecutorService mAsyncPool = Executors.newCachedThreadPool();
    private final Pool<WebClient> mWebClientPool;

    ScraperEnvironment(final long pWaitTimeout, final boolean pNotifyOnStatusChanged, final Pool<WebClient> pWebClientPool)
    {
        mToNotify = INotify.fromEnvironment();
        mWaitTimeout = pWaitTimeout;
        mNotifyOnStatusChanged = pNotifyOnStatusChanged;
        mWebClientPool = pWebClientPool;
    }

    public long waitTimeout()
    {
        return mWaitTimeout;
    }

    public boolean notifyOnStatusChanged()
    {
        return mNotifyOnStatusChanged;
    }

    public List<INotify> notifiers()
    {
        return mToNotify;
    }

    public ExecutorService asyncPool()
    {
        return mAsyncPool;
    }

    public Pool<WebClient> webClientPool()
    {
        return mWebClientPool;
    }
}
