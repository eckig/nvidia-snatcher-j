package util.html;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SynchronousAjaxController extends AjaxController
{
    private static final AjaxController INSTANCE = new SynchronousAjaxController();

    private static final long serialVersionUID = -3743250509579423878L;

    private SynchronousAjaxController()
    {
    }

    public static AjaxController instance()
    {
        return INSTANCE;
    }

    @Override public boolean processSynchron(final HtmlPage page, final WebRequest request, final boolean async)
    {
        return true;
    }
}
