package http.response;

import cookie.Cookie;
import db.DataBase;
import http.Charset;
import http.ContentType;
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

public class HttpResponse {

    private static final Map<HttpStatusCode, String> firstLineHttpProtocolMap = new HashMap<>();

    private HttpStatusCode code;
    private ContentType contentType;
    private Charset charset;
    private int contentLength;
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
        this.code = code;
        this.cookies = new HashMap<>();
        contentLength = 0;
        this.out = out;
    }

    public void setCode(final HttpStatusCode code) {
        this.code = code;
    }

    public void setContentType(final ContentType type, final Charset charset) {
        this.contentType = type;
        this.charset = charset;
    }

    public void setContentType(final ContentType type) {
        setContentType(type, Charset.UTF8);
    }

    public void addCookie(final Cookie cookie) {
        this.cookies.put(cookie.getKey(), cookie);
    }

    public void forward(final String path) throws IOException {
        BufferedReader br;
        byte[] body;
        String nextPath = path.equals("/") ? "./webapp/index.html" : path;

        if (!nextPath.startsWith("./webapp")) {
            nextPath = "./webapp" + path;
        }
        if (nextPath.equals("./webapp/user/list.html")) {
            body = getBody(nextPath);
        } else {
            body = Files.readAllBytes(new File(nextPath).toPath());
        }
        br = new BufferedReader(new InputStreamReader(new FileInputStream(nextPath)));
        this.contentLength = body.length;
        this.body = IOUtils.readData(br, contentLength);
        out.write(getHeader().getBytes(StandardCharsets.UTF_8));
        out.write(body);
    }

    public void redirect(final String path) throws IOException{
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 302 See Other\n")
                .append("Location: ")
                .append(path).append("\n");
        if (cookies.size() > 0) {
            sb.append("Set-Cookie: ");
            for (String key : cookies.keySet()) {
                sb.append(cookies.get(key)).append("\n");
            }
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private byte[] getBody(final String url) throws IOException {
        byte[] body;
        List<String> strings = Files.readAllLines(new File(url).toPath());
        boolean isDynamicSection = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            if (isDynamicSection) {
                List<User> allUser = new ArrayList<>(DataBase.findAll());
                for (int i = 1; i <= allUser.size(); i++) {
                    User user = allUser.get(i - 1);
                    stringBuilder.append("<tr>\n<th scope=\"row\">")
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
            stringBuilder.append(string).append("\n");
        }
        body = stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
        return body;
    }

    private String getHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append(firstLineHttpProtocolMap.get(this.code)).append("\n")
                .append("Content-Type: ").append(this.contentType).append(";").append("charset=").append(this.charset).append("\n")
                .append("Content-Length: ").append(this.contentLength).append("\n");
        if (cookies.size() > 0) {
            sb.append("Set-Cookie: ");
            for (String key : cookies.keySet()) {
                sb.append(cookies.get(key)).append("\n");
            }
        }
        return sb.append("\n").toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(firstLineHttpProtocolMap.get(this.code)).append("\n")
                .append("Content-Type: ").append(this.contentType).append(";").append("charset=").append(this.charset).append("\n")
                .append("Content-Length: ").append(this.contentLength).append("\n");
        if (cookies.size() > 0) {
            sb.append("Set-Cookie: ");
            for (String key : cookies.keySet()) {
                sb.append(cookies.get(key)).append("\n");
            }
        }
        if (body != null) {
            sb.append("\n")
                    .append(this.body);
        }
        return sb.toString();
    }
}
