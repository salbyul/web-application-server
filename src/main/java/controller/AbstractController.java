package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.IOException;

public abstract class AbstractController implements Controller {

    @Override
    public void service(final HttpRequest request, final HttpResponse response) throws IOException {
        if (request.getMethod().isGet()) {
            doGet(request, response);
        } else if (request.getMethod().isPost()) {
            doPost(request, response);
        }
    }

    public abstract void doGet(final HttpRequest request, final HttpResponse response) throws IOException;

    public abstract void doPost(final HttpRequest request, final HttpResponse response) throws IOException;
}
