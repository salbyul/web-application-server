package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotFountController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(NotFountController.class);
    private static final NotFountController controller = new NotFountController();

    private NotFountController() {}

    protected static NotFountController getInstance() {
        return controller;
    }

    @Override
    public void service(final HttpRequest request, final HttpResponse response) {
        response.forward("/404.html");
    }

    @Override
    public void doGet(final HttpRequest request, final HttpResponse response) {

    }

    @Override
    public void doPost(final HttpRequest request, final HttpResponse response) {

    }
}
