package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import cookie.Cookie;
import db.DataBase;
import http.request.HttpRequest;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.HttpStatusCode;

import static util.HttpStatusCode.*;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            DataOutputStream dos = new DataOutputStream(out);
            HttpRequest httpRequest = HttpRequestUtils.generateHttpRequest(br);
            String nextPath = HttpRequestUtils.DEFAULT_URL;
            Map<String, Cookie> cookies = new HashMap<>();

            if (httpRequest.getMethod().isPost()) {
                if (httpRequest.getUri().equals("/user/create")) {
                    Map<String, String> body = httpRequest.getBody();
                    User user = new User(body.get("userId"), body.get("password"), body.get("name"), body.get("email"));
                    DataBase.addUser(user);
                    log.info("New User: {}", user);
                }
                if (httpRequest.getUri().equals("/user/login")) {
                    Map<String, String> body = httpRequest.getBody();
                    if (isValidUser(dos, cookies, body)) return;
                    cookies.put("logined", new Cookie("logined", "true"));
                }
                redirect(dos, cookies, nextPath);
                return;
            }
            if (httpRequest.getUri().equals("/user/list.html")) {
                Cookie cookie = httpRequest.getCookie("logined");
                if (cookie == null || cookie.getValue().equals("false")) {
                    nextPath = "/user/login.html";
                    writeResponse(dos, nextPath, cookies, FORBIDDEN);
                    return;
                }
                nextPath = "/user/list.html";
                writeResponse(dos, nextPath, cookies);
                return;
            }
            writeResponse(dos, httpRequest.getUri(), cookies);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void writeResponse(final DataOutputStream dos, final String url, final Map<String, Cookie> cookies, final HttpStatusCode code) throws IOException {
        String nextPath = url;
        if (url.equals("/")) {
            nextPath = "/index.html";
        }
        byte[] body;
        String contentType = extractContentType(url);
        if (url.equals("/user/list.html")) {
            body = getBody(url);
        } else {
            body = Files.readAllBytes(new File("./webapp" + nextPath).toPath());
        }
        dos.writeBytes(HttpRequestUtils.getFirstLineHttpProtocol(code) + " \r\n");
        dos.writeBytes("Content-Type: " + contentType + "\r\n");
        dos.writeBytes("Content-Length: " + body.length + "\r\n");
        setCookies(cookies, dos);
        dos.writeBytes("\r\n");

        responseBody(dos, body);
    }

    private String extractContentType(final String url) {
        if (url.length() >= 5 && url.contains(".css")) {
            return "text/css;charset=utf-8";
        } else if (url.length() >= 4 && url.contains(".js")) {
            return "text/javascript;charset=utf-8";
        }
        return "text/html;charset=utf-8";
    }

    private byte[] getBody(final String url) throws IOException {
        byte[] body;
        List<String> strings = Files.readAllLines(new File("./webapp" + url).toPath());
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

    private void responseBody(final DataOutputStream dos, final byte[] body) throws IOException {
        dos.write(body, 0, body.length);
        dos.flush();
    }

    private void writeResponse(final DataOutputStream dos, final String url, final Map<String, Cookie> cookies) throws IOException {
        writeResponse(dos, url, cookies, OK);
    }

    private void redirect(final DataOutputStream dos, final Map<String, Cookie> cookies, final String path) throws IOException {
        dos.writeBytes(HttpRequestUtils.getFirstLineHttpProtocol(SEE_OTHER) + " \r\n");
        dos.writeBytes("Location: " + path + "\r\n");
        dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
        dos.writeBytes("Content-Length: 0 \r\n");
        setCookies(cookies, dos);
        dos.writeBytes("\r\n");
    }

    private void setCookies(final Map<String, Cookie> cookies, final DataOutputStream dos) throws IOException {
        if (cookies == null) {
            return;
        }
        for (String key : cookies.keySet()) {
            Cookie cookie = cookies.get(key);
            dos.writeBytes("Set-Cookie: " + cookie.toString() + "\r\n");
        }
    }

    private boolean isValidUser(final DataOutputStream dos, final Map<String, Cookie> cookies, final Map<String, String> params) throws IOException {
        Optional<String> optionalUserId = Optional.ofNullable(params.get("userId"));
        Optional<String> optionalPassword = Optional.ofNullable(params.get("password"));
        if (!optionalUserId.isPresent() || !optionalPassword.isPresent()) {
            loginFailedResponse(dos, cookies);
            return true;
        }
        Optional<User> optionalUser = Optional.ofNullable(DataBase.findUserById(params.get("userId")));
        if (!optionalUser.isPresent()) {
            loginFailedResponse(dos, cookies);
            return true;
        }
        User user = optionalUser.get();
        if (!user.getPassword().equals(optionalPassword.get())) {
            loginFailedResponse(dos, cookies);
            return true;
        }
        return false;
    }

    private void loginFailedResponse(final DataOutputStream dos, final Map<String, Cookie> cookies) throws IOException {
        cookies.put("logined", new Cookie("logined", "false"));
        writeResponse(dos, HttpRequestUtils.LOGIN_FAILED_URL, cookies, BAD_REQUEST);
    }
}
