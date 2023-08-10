package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import cookie.Cookie;
import exception.HttpRequestException;
import http.HttpMethod;
import http.request.HttpRequest;

public class HttpRequestUtils {

    public static final String DEFAULT_URL = "/index.html";
    public static final String LOGIN_FAILED_URL = "/user/login_failed.html";
    public static final Map<HttpStatusCode, String> firstLineHttpProtocolMap = new HashMap<>();
    public static final String ERROR_PARSE_HTTP_REQUEST = "HttpRequest Parse Error";

    static {
        firstLineHttpProtocolMap.put(HttpStatusCode.OK, "HTTP/1.1 200 OK");
        firstLineHttpProtocolMap.put(HttpStatusCode.FORBIDDEN, "HTTP/1.1 403 Forbidden");
        firstLineHttpProtocolMap.put(HttpStatusCode.BAD_REQUEST, "HTTP/1.1 400 Bad Request");
        firstLineHttpProtocolMap.put(HttpStatusCode.SEE_OTHER, "HTTP/1.1 303 See Other");
    }

    public static HttpRequest generateHttpRequest(final BufferedReader br) {
        try {
            String line = br.readLine();
            if (line == null) {
                return null;
            }
            String[] requestLineSplit = line.split(" ");
            if (requestLineSplit.length != 3) {
                throw new HttpRequestException(ERROR_PARSE_HTTP_REQUEST);
            }
            String method = requestLineSplit[0];
            String url = requestLineSplit[1];
            String httpVersion = requestLineSplit[2];
            if (HttpMethod.isGet(method)) {
                url = url.split("[?]")[0];
            }
            Map<String, String> headers = generateHeader(br);
            Map<String, Cookie> cookies = HttpRequestUtils.parseCookies(headers.get("Cookie"));
            headers.remove("Cookie");
            if (HttpMethod.isPost(method) && headers.containsKey("Content-Length") && Integer.parseInt(headers.get("Content-Length")) > 0) {
                String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                Map<String, String> body = parseQueryString(requestBody);
                return new HttpRequest(HttpMethod.toMethod(method), url, httpVersion, headers, cookies, body);
            }
            if (HttpMethod.isGet(method) && requestLineSplit[1].contains("?")) {
                String parameters = requestLineSplit[1].split("[?]")[1];
                Map<String, String> body = parseQueryString(parameters);
                return new HttpRequest(HttpMethod.toMethod(method), url, httpVersion, headers, cookies, body);
            }
            return new HttpRequest(HttpMethod.toMethod(method), url, httpVersion, headers, cookies);
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

    public static String getFirstLineHttpProtocol(final HttpStatusCode code) {
        return firstLineHttpProtocolMap.get(code);
    }

    public static String getParameters(final String url) {
        return url.split("[?]")[1];
    }

    /**
     * @param queryString queryString은URL에서 ? 이후에 전달되는 field1=value1&field2=value2 형식임
     * @return
     */
    public static Map<String, String> parseQueryString(String queryString) {
        return parseValues(queryString, "&");
    }

    /**
     * @param cookies 쿠키값은 name1=value1; name2=value2 형식임
     * @return
     */
    public static Map<String, Cookie> parseCookies(String cookies) {
        Map<String, String> stringCookiesMap = parseValues(cookies, ";");
        return stringCookiesMap.entrySet().stream()
                .map(entry -> new Cookie(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Cookie::getKey, cookie -> cookie));
    }

    private static Map<String, String> parseValues(String values, String separator) {
        if (Strings.isNullOrEmpty(values)) {
            return Maps.newHashMap();
        }

        String[] tokens = values.split(separator);
        return Arrays.stream(tokens).map(t -> getKeyValue(t, "=")).filter(p -> p != null)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }

    static Pair getKeyValue(String keyValue, String regex) {
        if (Strings.isNullOrEmpty(keyValue)) {
            return null;
        }

        String[] tokens = keyValue.split(regex);
        if (tokens.length != 2) {
            return null;
        }

        return new Pair(tokens[0], tokens[1]);
    }

    public static Pair parseHeader(String header) {
        return getKeyValue(header, ": ");
    }

    public static class Pair {
        String key;
        String value;

        Pair(String key, String value) {
            this.key = key.trim();
            this.value = value.trim();
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Pair other = (Pair) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Pair [key=" + key + ", value=" + value + "]";
        }
    }
}
