package http.response;

import cookie.Cookie;
import util.HttpStatusCode;
import util.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static util.HttpRequestUtils.*;

public class HttpResponse {

    private static final Map<HttpStatusCode, String> firstLineHttpProtocolMap = new HashMap<>();
    private static final String BASE_PATH = "./webapp";
    private static final String DEFAULT_CONTENT_LENGTH = "0";
    private static final String DELIMITER = ": ";
    private static final String NEXT_LINE = "\n";

    private HttpStatusCode code;
    private final Map<String, String> headers;
    private final Map<String, Cookie> cookies;
    private final OutputStream out;
    private String body;

    static {
        firstLineHttpProtocolMap.put(HttpStatusCode.OK, "HTTP/1.1 200 OK");
        firstLineHttpProtocolMap.put(HttpStatusCode.FORBIDDEN, "HTTP/1.1 403 Forbidden");
        firstLineHttpProtocolMap.put(HttpStatusCode.BAD_REQUEST, "HTTP/1.1 400 Bad Request");
        firstLineHttpProtocolMap.put(HttpStatusCode.SEE_OTHER, "HTTP/1.1 303 See Other");
    }

    public HttpResponse(final OutputStream out) {
        this(HttpStatusCode.OK, out);
    }

    public HttpResponse(final HttpStatusCode code, final OutputStream out) {
        this.cookies = new HashMap<>();
        this.headers = new HashMap<>();
        this.code = code;
        this.out = out;
        headers.put(CONTENT_LENGTH, DEFAULT_CONTENT_LENGTH);
    }

    public void setCode(final HttpStatusCode code) {
        this.code = code;
    }

    public void addCookie(final Cookie cookie) {
        this.cookies.put(cookie.getKey(), cookie);
    }

    public void forward(final String path) {
        try {
            BufferedReader br;
            String nextPath = path.equals("/") ? BASE_PATH + DEFAULT_URL : path;

            if (!nextPath.startsWith(BASE_PATH)) {
                nextPath = BASE_PATH + path;
            }
            if (this.body == null) {
                byte[] body;
                body = Files.readAllBytes(new File(nextPath).toPath());
                br = new BufferedReader(new InputStreamReader(new FileInputStream(nextPath)));
                this.headers.put(CONTENT_LENGTH, String.valueOf(body.length));
                this.body = IOUtils.readData(br, body.length);
            }
            out.write(getHeader().getBytes(StandardCharsets.UTF_8));
            out.write(this.body.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void redirect(final String path) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(firstLineHttpProtocolMap.get(HttpStatusCode.SEE_OTHER)).append(NEXT_LINE)
                    .append(LOCATION).append(DELIMITER)
                    .append(path).append(NEXT_LINE);
            if (cookies.size() > 0) {
                sb.append(SET_COOKIE).append(DELIMITER);
                for (String key : cookies.keySet()) {
                    sb.append(cookies.get(key)).append(NEXT_LINE);
                }
            }
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBody(final String body) {
        this.body = body;
        this.headers.put(CONTENT_LENGTH, String.valueOf(this.body.getBytes(StandardCharsets.UTF_8).length));
    }

    private String getHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append(firstLineHttpProtocolMap.get(this.code)).append(NEXT_LINE);
        for (String key : headers.keySet()) {
            if (key.equals(CONTENT_TYPE)) {
                sb.append(key).append(DELIMITER).append(headers.get(key)).append(";charset=utf-8\n");
                continue;
            }
            sb.append(key).append(DELIMITER).append(headers.get(key)).append(NEXT_LINE);
        }
        if (cookies.size() > 0) {
            sb.append(SET_COOKIE).append(DELIMITER);
            for (String key : cookies.keySet()) {
                sb.append(cookies.get(key)).append(NEXT_LINE);
            }
        }
        return sb.append(NEXT_LINE).toString();
    }

    public void addHeader(final String key, final String value) {
        this.headers.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(firstLineHttpProtocolMap.get(this.code)).append(NEXT_LINE);
        for (String key : headers.keySet()) {
            sb.append(key).append(DELIMITER).append(headers.get(key)).append(NEXT_LINE);
        }
        if (cookies.size() > 0) {
            sb.append(SET_COOKIE).append(DELIMITER);
            for (String key : cookies.keySet()) {
                sb.append(cookies.get(key)).append(NEXT_LINE);
            }
        }
        if (body != null) {
            sb.append(NEXT_LINE).append(this.body);
        }
        return sb.toString();
    }
}
