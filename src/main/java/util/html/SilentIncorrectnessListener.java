package util.html;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;

public class SilentIncorrectnessListener implements IncorrectnessListener
{
    private static final IncorrectnessListener INSTANCE = new SilentIncorrectnessListener();

    private SilentIncorrectnessListener()
    {
    }

    public static IncorrectnessListener instance()
    {
        return INSTANCE;
    }

    @Override public void notify(final String message, final Object origin)
    {
        // silent
    }
}
