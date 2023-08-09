package util;

import java.util.Arrays;

public enum HttpMethodUtils {
    GET("GET"), POST("POST");

    private static final String ERROR_NOT_RIGHT_METHOD = "Not Right Method";
    private final String method;

    HttpMethodUtils(final String method) {
        this.method = method;
    }

    public static HttpMethodUtils getMethod(final String method) {
        return Arrays.stream(values())
                .filter(httpMethod -> httpMethod.method.equals(method))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ERROR_NOT_RIGHT_METHOD));
    }

    public boolean isGet() {
        return this == GET;
    }

    public boolean isPost() {
        return this == POST;
    }
}
