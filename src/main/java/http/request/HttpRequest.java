package http.request;

import com.google.common.collect.ImmutableMap;
import cookie.Cookie;
import http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private final HttpMethod method;
    private final String uri;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, Cookie> cookies;
    private final Map<String, String> body;

    public HttpRequest(final HttpMethod method, final String uri, final String version, final Map<String, String> headers, final Map<String, Cookie> cookies, final Map<String, String> body) {
        this.method = method;
        this.uri = uri;
        this.version = version;
        this.headers = headers;
        this.cookies = cookies;
        this.body = body;
    }

    public HttpRequest(final HttpMethod method, final String uri, final String version, final Map<String, String> headers, final Map<String, Cookie> cookies) {
        this(method, uri, version, headers, cookies, new HashMap<>());
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getVersion() {
        return version;
    }

    public String getHeader(final String headerName) {
        return headers.get(headerName);
    }

    public Map<String, String> getHeaders() {
        return ImmutableMap.copyOf(headers);
    }

    public Cookie getCookie(final String cookie) {
        return cookies.get(cookie);
    }

    public Map<String, Cookie> getCookies() {
        return ImmutableMap.copyOf(cookies);
    }

    public Map<String, String> getBody() {
        return ImmutableMap.copyOf(body);
    }

    public String getValue(final String key) {
        return body.get(key);
    }
}
