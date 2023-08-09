package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

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
            log.debug("method: {}", method);
            Map<String, String> header = generateHeader(br);
            if (url.startsWith("/user/create")) {
                String contentLength = header.get("Content-Length");
                String requestBody = IOUtils.readData(br, Integer.parseInt(contentLength));
                Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                log.info("New User: {}", user);
                url = HttpRequestUtils.DEFAULT_URL;
            }
            writeResponse(dos, url, method);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private Map<String, String> generateHeader(final BufferedReader br) throws IOException {
        Map<String, String> header = new HashMap<>();
        String line;
        while (!"".equals(line = br.readLine())) {
            log.debug("line: {}", line);
            String[] split = line.split(": ");
            if (split.length == 2) {
                header.put(split[0], split[1]);
            }
        }
        return header;
    }

    private void writeResponse(final DataOutputStream dos, final String url, final HttpMethodUtils method) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        if (method.isPost()) {
            response303Header(dos, body.length);
        }
        if (method.isGet()) {
            response200Header(dos, body.length);
        }
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response303Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 303 See Other \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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
}
