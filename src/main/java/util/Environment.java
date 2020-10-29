package util;

import java.util.List;
import java.util.Optional;

public class Environment
{
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
