package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import controller.Servlet;
import cookie.Cookie;
import exception.HttpRequestException;
import http.request.HttpRequest;
import http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.HttpSession;

import static util.HttpRequestUtils.*;

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
            log.debug("URI: {}", request.getUri());
            Servlet.handle(request, response);
        } catch (IOException | HttpRequestException e) {
            log.error(e.getMessage());
        }
    }

    private void init(final OutputStream out, final BufferedReader br) {
        request = new HttpRequest(br);
        response = new HttpResponse(out);
        response.addHeader(CONTENT_TYPE, extractContentType(request.getUri()));
        Cookie sessionCookie = request.getCookie(HttpSession.SESSION_COOKIE_KEY);
        if (sessionCookie == null) {
            String uuid = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(HttpSession.SESSION_COOKIE_KEY, uuid);
            response.addCookie(cookie);
        }
    }

    private String extractContentType(final String url) {
        String extension = url.substring(url.lastIndexOf('.') + 1);

        if (url.length() >= 5 && extension.equals(CONTENT_TYPE_CSS)) {
            return TEXT_CSS;
        } else if (url.length() >= 4 && extension.equals(CONTENT_TYPE_JS)) {
            return TEXT_JAVASCRIPT;
        }
        return TEXT_HTML;
    }
}
