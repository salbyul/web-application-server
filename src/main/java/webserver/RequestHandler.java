package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import cookie.Cookie;
import db.DataBase;
import exception.HttpRequestException;
import http.ContentType;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import static util.HttpStatusCode.*;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private HttpResponse response;
    private HttpRequest request;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            DataOutputStream dos = new DataOutputStream(out);
            init(dos, br);

            String nextPath = HttpRequestUtils.DEFAULT_URL;
            if (request.getMethod().isPost()) {
                if (request.getUri().equals("/user/create")) {
                    User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email"));
                    DataBase.addUser(user);
                    log.info("New User: {}", user);
                }
                if (request.getUri().equals("/user/login")) {
                    if (isValidUser(request.getParameter("userId"), request.getParameter("password"))) {
                        response.setCode(BAD_REQUEST);
                        response.addCookie(new Cookie("logined", "false"));
                        response.forward(HttpRequestUtils.LOGIN_FAILED_URL);
                        return;
                    }
                    response.addCookie(new Cookie("logined", "true"));
                }
                response.redirect(nextPath);
                return;
            }
            if (request.getUri().equals("/user/list.html")) {
                Cookie cookie = request.getCookie("logined");
                if (cookie == null || cookie.getValue().equals("false")) {
                    nextPath = "/user/login.html";
                    response.setCode(FORBIDDEN);
                    response.forward(nextPath);
                    return;
                }
                nextPath = "/user/list.html";
                response.forward(nextPath);
                return;
            }
            response.forward(request.getUri());
        } catch (IOException | HttpRequestException e) {
            log.error(e.getMessage());
        }
    }

    private void init(final OutputStream out, final BufferedReader br) {
        request = new HttpRequest(br);
        response = new HttpResponse(out);
        response.setContentType(extractContentType(request.getUri()));
    }

    private ContentType extractContentType(final String url) {
        if (url.length() >= 5 && url.contains(".css")) {
            return ContentType.CSS;
        } else if (url.length() >= 4 && url.contains(".js")) {
            return ContentType.JS;
        }
        return ContentType.HTML;
    }

    private boolean isValidUser(final String userId, final String password) throws IOException {
        Optional<User> optionalUser = Optional.ofNullable(DataBase.findUserById(userId));
        if (!optionalUser.isPresent()) {
            return true;
        }
        User user = optionalUser.get();
        return !user.getPassword().equals(password);
    }
}
