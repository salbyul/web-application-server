package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.IOException;

public interface Controller {

    void service(final HttpRequest request, final HttpResponse response) throws IOException;
}
