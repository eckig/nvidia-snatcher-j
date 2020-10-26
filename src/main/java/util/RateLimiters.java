package util;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.util.concurrent.RateLimiter;

import model.Model;
import model.Store;
import notify.INotify;

public final class RateLimiters
{
    private static final RateLimiters INSTANCE = new RateLimiters();
    private final Map<Key, RateLimiter> mRateLimiters = new ConcurrentHashMap<>();

    private RateLimiters()
    {

    }

    public static RateLimiter get(final Class<? extends INotify> pType, final Store pStore, final Model pModel)
    {
        Objects.requireNonNull(pType, "Notification type may not be null");
        Objects.requireNonNull(pStore, "Store may not be null");
        Objects.requireNonNull(pModel, "Model may not be null");
        // 1 notification per minute should be enough?  -> 1/60
        return INSTANCE.mRateLimiters.computeIfAbsent(new Key(pType.getName(), pStore, pModel),
                k -> RateLimiter.create(1.0 / 60.0));
    }

    private static class Key
    {
        private final String name;
        private final Store store;
        private final Model model;

        public Key(final String pName, final Store pStore, final Model pModel)
        {
            name = pName;
            store = pStore;
            model = pModel;
        }

        @Override
        public boolean equals(final Object pO)
        {
            if (this == pO)
            {
                return true;
            }
            if (pO == null || getClass() != pO.getClass())
            {
                return false;
            }
            final Key key = (Key) pO;
            return Objects.equals(name, key.name) &&
                    store == key.store &&
                    model == key.model;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, store, model);
        }
    }
}
