package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cookie.Cookie;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpMethodUtils;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            DataOutputStream dos = new DataOutputStream(out);
            String line = br.readLine();
            if (line == null) {
                return;
            }
            String url = HttpRequestUtils.getUrl(line);
            HttpMethodUtils method = HttpMethodUtils.getMethod(line.substring(0, line.indexOf(" ")));
            log.debug("URL: [{}]", url);
            Map<String, String> header = generateHeader(br);
            Map<String, Cookie> cookies = HttpRequestUtils.parseCookies(header.get("Cookie"));
            if (url.equals("/user/create")) {
                url = HttpRequestUtils.DEFAULT_URL;
                String contentLength = header.get("Content-Length");
                String requestBody = IOUtils.readData(br, Integer.parseInt(contentLength));
                Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);
                log.info("New User: {}", user);
            }
            if (url.equals("/user/login")) {
                url = HttpRequestUtils.DEFAULT_URL;
                String contentLength = header.get("Content-Length");
                String requestBody = IOUtils.readData(br, Integer.parseInt(contentLength));
                Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
                if (isValidUser(dos, method, cookies, params)) return;
                cookies.put("logined", new Cookie("logined", "true"));
                writeResponse(dos, url, method, cookies);
                return;
            }
            writeResponse(dos, url, method, cookies);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loginFailedResponse(final DataOutputStream dos, final HttpMethodUtils method, final Map<String, Cookie> cookies) throws IOException {
        String url;
        url = HttpRequestUtils.LOGIN_FAILED_URL;
        cookies.put("logined", new Cookie("logined", "false"));
        writeResponse(dos, url, method, cookies);
    }

    private Map<String, String> generateHeader(final BufferedReader br) throws IOException {
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

    private void writeResponse(final DataOutputStream dos, final String url, final HttpMethodUtils method, final Map<String, Cookie> cookies) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        if (method.isPost()) {
            if (url.equals(HttpRequestUtils.LOGIN_FAILED_URL)) {
                response400Header(dos, body.length, cookies);
            }
            if (url.equals(HttpRequestUtils.DEFAULT_URL)) {
                response303Header(dos, body.length, cookies);
            }
        }
        if (method.isGet()) {
            response200Header(dos, body.length, cookies);
        }
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, final Map<String, Cookie> cookies) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            setCookies(cookies, dos);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response400Header(DataOutputStream dos, int lengthOfBodyContent, final Map<String, Cookie> cookies) {
        try {
            dos.writeBytes("HTTP/1.1 400 Bad Request \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            setCookies(cookies, dos);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response303Header(DataOutputStream dos, int lengthOfBodyContent, final Map<String, Cookie> cookies) {
        try {
            dos.writeBytes("HTTP/1.1 303 See Other \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            setCookies(cookies, dos);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void setCookies(final Map<String, Cookie> cookies, final DataOutputStream dos) throws IOException {
        if (cookies != null) {
            for (String key : cookies.keySet()) {
                Cookie cookie = cookies.get(key);
                dos.writeBytes("Set-Cookie: " + cookie.toString() + "\r\n");
            }
        }
    }

    private boolean isValidUser(final DataOutputStream dos, final HttpMethodUtils method, final Map<String, Cookie> cookies, final Map<String, String> params) throws IOException {
        Optional<String> optionalUserId = Optional.ofNullable(params.get("userId"));
        Optional<String> optionalPassword = Optional.ofNullable(params.get("password"));
        if (!optionalUserId.isPresent() || !optionalPassword.isPresent()) {
            loginFailedResponse(dos, method, cookies);
            return true;
        }
        Optional<User> optionalUser = Optional.ofNullable(DataBase.findUserById(params.get("userId")));
        if (!optionalUser.isPresent()) {
            loginFailedResponse(dos, method, cookies);
            return true;
        }
        User user = optionalUser.get();
        if (!user.getPassword().equals(optionalPassword.get())) {
            loginFailedResponse(dos, method, cookies);
            return true;
        }
        return false;
    }
}
