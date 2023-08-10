package http.request;

import com.google.common.collect.ImmutableMap;
import cookie.Cookie;
import exception.HttpRequestException;
import http.HttpMethod;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static util.HttpRequestUtils.ERROR_PARSE_HTTP_REQUEST;

public class HttpRequest {

    private final HttpMethod method;
    private final String uri;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, Cookie> cookies;
    private final Map<String, String> body;

    public HttpRequest(final BufferedReader br) {
        try {
            String line = br.readLine();
            if (line == null) {
                throw new HttpRequestException(ERROR_PARSE_HTTP_REQUEST);
            }
            String[] requestLineSplit = line.split(" ");
            if (requestLineSplit.length != 3) {
                throw new HttpRequestException(ERROR_PARSE_HTTP_REQUEST);
            }
            String method = requestLineSplit[0];
            String uri = requestLineSplit[1];
            String httpVersion = requestLineSplit[2];
            if (HttpMethod.isGet(method)) {
                uri = uri.split("[?]")[0];
            }
            Map<String, String> headers = generateHeader(br);
            Map<String, Cookie> cookies = HttpRequestUtils.parseCookies(headers.get("Cookie"));
            headers.remove("Cookie");

            this.method = HttpMethod.toMethod(method);
            this.uri = uri;
            this.version = httpVersion;
            this.headers = headers;
            this.cookies = cookies;
            Map<String, String> body = new HashMap<>();
            if (HttpMethod.isPost(method) && headers.containsKey("Content-Length") && Integer.parseInt(headers.get("Content-Length")) > 0) {
                String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                body = HttpRequestUtils.parseQueryString(requestBody);
            } else if (HttpMethod.isGet(method) && requestLineSplit[1].contains("?")) {
                String parameters = requestLineSplit[1].split("[?]")[1];
                body = HttpRequestUtils.parseQueryString(parameters);
            }
            this.body = body;
        } catch (IOException e) {
            throw new HttpRequestException(ERROR_PARSE_HTTP_REQUEST);
        }
    }

    private static Map<String, String> generateHeader(final BufferedReader br) throws IOException {
        Map<String, String> header = new HashMap<>();
        String line;
        while (!"".equals(line = br.readLine())) {
            String[] split = line.split(": ");
            if (split.length == 2) {
                header.put(split[0], split[1]);
            }
        }
        return header;
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
