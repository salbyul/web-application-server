package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotFountController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(NotFountController.class);
    private static final NotFountController controller = new NotFountController();

    private NotFountController() {}

    protected static NotFountController getInstance() {
        return controller;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpResponse httpResponse) {
        httpResponse.forward("/404.html");
    }
}
