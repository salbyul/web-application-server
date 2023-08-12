package controller;

import http.request.HttpRequest;
import http.response.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Servlet {

    private static final Map<String, Controller> controller = new HashMap<>();

    private static final String NOT_FOUND = "/404.html";

    static {
        controller.put("/user/create", UserCreateController.getInstance());
        controller.put("/user/login", UserLoginController.getInstance());
        controller.put("/user/list.html", UserListController.getInstance());
        controller.put(NOT_FOUND, NotFountController.getInstance());
    }

    public static void handle(final HttpRequest httpRequest, final HttpResponse httpResponse) {
        try {
            String uri = httpRequest.getUri();
            if (uri.equals("/")) {
                uri = "/index.html";
            }
            if (controller.containsKey(uri)) {
                controller.get(uri).service(httpRequest, httpResponse);
                return;
            }
            File file = new File("./webapp" + uri);
            if (file.exists()) {
                httpResponse.forward(uri);
                return;
            }
            httpResponse.forward(NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
