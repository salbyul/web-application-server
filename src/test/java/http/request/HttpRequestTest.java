package http.request;

import http.HttpMethod;
import org.junit.Test;
import util.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HttpRequestTest {

    public static final String BASE_DIRECTORY = "./src/test/resources/";

    @Test
    public void parseHttpRequestGetMethod() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(BASE_DIRECTORY + "Http_GET.txt")));
            HttpRequest httpRequest = new HttpRequest(br);
            assertThat(httpRequest.getMethod(), is(HttpMethod.GET));
            assertThat(httpRequest.getUri(), is("/user/list.html"));
            assertThat(httpRequest.getHeader("Accept"), is("*/*"));
            assertThat(httpRequest.getHeader("Connection"), is("keep-alive"));
            assertThat(httpRequest.getParameter("userId"), is("test"));
            assertThat(httpRequest.getParameter("password"), is("12345"));
            assertThat(httpRequest.getVersion(), is("HTTP/1.1"));
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void parseHttpRequestPostMethod() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(BASE_DIRECTORY + "Http_POST.txt")));
            HttpRequest httpRequest = new HttpRequest(br);
            assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
            assertThat(httpRequest.getUri(), is("/user/create"));
            assertThat(httpRequest.getHeader("Accept"), is("*/*"));
            assertThat(httpRequest.getHeader("Connection"), is("keep-alive"));
            assertThat(httpRequest.getHeader("Content-Type"), is("application/x-www-form-urlencoded"));
            assertThat(httpRequest.getHeader("Content-Length"), is("46"));
            assertThat(httpRequest.getParameter("userId"), is("javajigi"));
            assertThat(httpRequest.getParameter("password"), is("password"));
            assertThat(httpRequest.getParameter("name"), is("JaeSung"));
            assertThat(httpRequest.getVersion(), is("HTTP/1.1"));
        } catch (IOException e) {
            fail();
        }
    }
}
