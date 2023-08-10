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

    public String getParameter(final String key) {
        return body.get(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (method.isGet()) {
            sb.append(method).append(" ").append(uri);
            if (body.size() > 0) {
                sb.append("?");
                for (String key : body.keySet()) {
                    sb.append(key).append("=").append(body.get(key)).append("&");
                }
                sb.delete(sb.length() - 1, sb.length());
            }
            sb.append(" ").append(version).append("\n");
            for (String key : headers.keySet()) {
                sb.append(key).append(": ").append(headers.get(key)).append("\n");
            }
            if (cookies.size() > 0) {
                sb.append("Cookie: ");
                for (String key : cookies.keySet()) {
                    sb.append(key).append("=").append(cookies.get(key).getValue()).append("; ");
                }
                sb.delete(sb.length() - 2, sb.length()).append("\n");
            }
            return sb.toString();
        }
        sb.append(method).append(" ").append(uri).append(" ").append(version).append("\n");
        for (String key : headers.keySet()) {
            sb.append(key).append(": ").append(headers.get(key)).append("\n");
        }
        if (cookies.size() > 0) {
            sb.append("Cookie: ");
            for (String key : cookies.keySet()) {
                sb.append(key).append("=").append(cookies.get(key).getValue()).append("; ");
            }
            sb.delete(sb.length() - 2, sb.length()).append("\n\n");
            for (String key : body.keySet()) {
                sb.append(key).append("=").append(body.get(key)).append("&");
            }
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }
}
