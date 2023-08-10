package http;

import exception.HttpRequestException;

import java.util.Arrays;

public enum HttpMethod {
    GET("GET"), POST("POST");

    private static final String ERROR_NOT_RIGHT_METHOD = "Not Right Method";
    private final String method;

    HttpMethod(final String method) {
        this.method = method;
    }

    public static HttpMethod toMethod(final String method) {
        return Arrays.stream(values())
                .filter(httpMethod -> httpMethod.method.equals(method))
                .findAny()
                .orElseThrow(() -> new HttpRequestException(ERROR_NOT_RIGHT_METHOD));
    }

    public boolean isGet() {
        return this == GET;
    }

    public boolean isPost() {
        return this == POST;
    }

    public static boolean isGet(final String method) {
        return method.equals("GET");
    }

    public static boolean isPost(final String method) {
        return method.equals("POST");
    }
}
