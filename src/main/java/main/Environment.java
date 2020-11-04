package main;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.Opt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.ProxyConfig;

import model.Search;
import notify.INotify;

public final class Environment
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

    public static final String ENV_LOAD_INTERVAL = "LOAD_INTERVAL";
    public static final String ENV_LOAD_PARALLELISM = "LOAD_PARALLELISM";
    public static final String ENV_LOAD_MAX_WAIT = "LOAD_MAX_WAIT";
    public static final String ENV_NOTIFY_ON_CHANGE = "NOTIFY_ON_CHANGE";
    public static final String ENV_TIME_FROM = "TIME_FROM";
    public static final String ENV_TIME_TO = "TIME_TO";
    public static final String ENV_PROXY_HTTP_HOST = "PROXY_HTTP_HOST";
    public static final String ENV_PROXY_HTTP_PORT = "PROXY_HTTP_PORT";

    private final long mWaitTimeout;
    private final boolean mNotifyOnStatusChanged;
    private final List<INotify> mToNotify;
    private final List<Search> mSearches;
    private final long mInterval;
    private final int mParallelism;
    private final LocalTime mTimeFrom;
    private final LocalTime mTimeTo;
    private final ProxyConfig mProxyConfig;

    private Environment(final long pWaitTimeout, final boolean pNotifyOnStatusChanged,
            final List<Search> pTargets, final long pInterval, final int pParallelism,
            final LocalTime pTimeFrom, final LocalTime pTimeTo,
            final ProxyConfig pProxyConfig)
    {
        mTimeFrom = pTimeFrom;
        mTimeTo = pTimeTo;
        mProxyConfig = pProxyConfig;
        mToNotify = INotify.fromEnvironment();
        mWaitTimeout = pWaitTimeout;
        mNotifyOnStatusChanged = pNotifyOnStatusChanged;
        mSearches = pTargets;
        mInterval = pInterval;
        mParallelism = pParallelism;
    }

    public static Environment fromSystemEnv()
    {
        final var interval = getLong(ENV_LOAD_INTERVAL, 20);
        final var waitTimeout = getLong(ENV_LOAD_MAX_WAIT, interval * 2);
        final var parallelism = getInt(ENV_LOAD_PARALLELISM, 2);
        final var notifyOnChange = getBoolean(ENV_NOTIFY_ON_CHANGE, false);
        final var timeFrom = getTime(ENV_TIME_FROM, LocalTime.MIN);
        final var timeTo = getTime(ENV_TIME_TO, LocalTime.MAX);
        final var proxyHttpHost = get(ENV_PROXY_HTTP_HOST).orElse(null);
        final var proxyHttpPort = getInt(ENV_PROXY_HTTP_PORT, -1);

        final ProxyConfig proxyConfig = proxyHttpPort != -1 && proxyHttpHost != null && !proxyHttpHost.isBlank() ?
                new ProxyConfig(proxyHttpHost, proxyHttpPort) : null;

        LOGGER.info(ENV_LOAD_INTERVAL + "=" + interval);
        LOGGER.info(ENV_LOAD_MAX_WAIT + "=" + waitTimeout);
        LOGGER.info(ENV_LOAD_PARALLELISM + "=" + parallelism);
        LOGGER.info(ENV_NOTIFY_ON_CHANGE + "=" + notifyOnChange);
        LOGGER.info(ENV_TIME_FROM + "=" + timeFrom);
        LOGGER.info(ENV_TIME_TO + "=" + timeTo);
        LOGGER.info(ENV_PROXY_HTTP_HOST + "=" + proxyHttpHost);
        LOGGER.info(ENV_PROXY_HTTP_PORT + "=" + proxyHttpPort);

        final var targets = Search.fromEnvironment();
        return new Environment(waitTimeout, notifyOnChange, targets, interval, parallelism, timeFrom, timeTo,
                proxyConfig);
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

    public List<Search> searches()
    {
        return mSearches;
    }

    public long interval()
    {
        return mInterval;
    }

    public int parallelism()
    {
        return mParallelism;
    }

    public LocalTime timeFrom()
    {
        return mTimeFrom;
    }

    public LocalTime timeTo()
    {
        return mTimeTo;
    }

    public Optional<ProxyConfig> proxyConfig()
    {
        return Optional.ofNullable(mProxyConfig);
    }

    public static long getLong(final String pKey, final long pDefault)
    {
        final var val = get(pKey).orElse(null);
        if (val == null)
        {
            return pDefault;
        }
        try
        {
            return Long.parseLong(val);
        }
        catch (final Exception e)
        {
            LOGGER.error("Failed to read environment value as Long: ", e);
            return pDefault;
        }
    }

    public static int getInt(final String pKey, final int pDefault)
    {
        final var val = get(pKey).orElse(null);
        if (val == null)
        {
            return pDefault;
        }
        try
        {
            return Integer.parseInt(val);
        }
        catch (final Exception e)
        {
            LOGGER.error("Failed to read environment value as Integer: ", e);
            return pDefault;
        }
    }

    public static boolean getBoolean(final String pKey, final boolean pDefault)
    {
        final var val = get(pKey).orElse(null);
        if (val == null)
        {
            return pDefault;
        }
        return val.strip().equalsIgnoreCase("on");
    }

    public static LocalTime getTime(final String pKey, final LocalTime pDefault)
    {
        final var val = get(pKey).orElse(null);
        if (val == null)
        {
            return pDefault;
        }
        try
        {
            return LocalTime.parse(val);
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to read environment value as LocalTime: ", e);
            return pDefault;
        }
    }

    public static List<String> getList(final String pKey)
    {
        return get(pKey).map(s -> s.split(",")).map(List::of).orElse(List.of());
    }

    public static Optional<String> get(final String pKey)
    {
        final String env = System.getenv(pKey);
        if (env != null && !env.isBlank())
        {
            return Optional.of(env);
        }
        final String prop = System.getProperty(pKey);
        if (prop != null && !prop.isBlank())
        {
            return Optional.of(prop);
        }
        return Optional.empty();
    }
}
