package http.response;

import cookie.Cookie;
import db.DataBase;
import model.User;
import util.HttpStatusCode;
import util.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void forward(final String path) throws IOException {
        BufferedReader br;
        byte[] body;
        String nextPath = path.equals("/") ? BASE_PATH + DEFAULT_URL : path;

        if (!nextPath.startsWith(BASE_PATH)) {
            nextPath = BASE_PATH + path;
        }
        if (nextPath.equals(BASE_PATH +"/user/list.html")) {
            body = getBody(nextPath);
        } else {
            body = Files.readAllBytes(new File(nextPath).toPath());
        }
        br = new BufferedReader(new InputStreamReader(new FileInputStream(nextPath)));
        this.headers.put(CONTENT_LENGTH, String.valueOf(body.length));
        this.body = IOUtils.readData(br, body.length);
        out.write(getHeader().getBytes(StandardCharsets.UTF_8));
        out.write(body);
    }

    public void redirect(final String path) throws IOException {
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
    }

    private byte[] getBody(final String url) throws IOException {
        byte[] body;
        List<String> strings = Files.readAllLines(new File(url).toPath());
        boolean isDynamicSection = false;
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (isDynamicSection) {
                List<User> allUser = new ArrayList<>(DataBase.findAll());
                for (int i = 1; i <= allUser.size(); i++) {
                    User user = allUser.get(i - 1);
                    sb.append("<tr>\n<th scope=\"row\">")
                            .append(i)
                            .append("</tr> <td>")
                            .append(user.getUserId())
                            .append("</td> <td>")
                            .append(user.getName())
                            .append("</td> <td>")
                            .append(user.getEmail())
                            .append("</td> <td> <a href=\"#\" class=\"btn btn-success\" roll=\"button\">수정</a></td>\n</tr>\n");
                }
                isDynamicSection = false;
            }
            if (string.contains("<tbody>")) {
                isDynamicSection = true;
                continue;
            }
            sb.append(string).append(NEXT_LINE);
        }
        body = sb.toString().getBytes(StandardCharsets.UTF_8);
        return body;
    }

    private String getHeader() {
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
