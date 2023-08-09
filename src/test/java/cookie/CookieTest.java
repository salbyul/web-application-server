package cookie;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CookieTest {

    Cookie cookie;

    @Before
    public void setUp() throws Exception {
        cookie = new Cookie("logined", "false");
    }

    @Test
    public void getKeyValue() {
        assertThat(cookie.getKey(), is("logined"));
        assertThat(cookie.getValue(), is("false"));
    }

    @Test
    public void setMaxAge() {
        cookie.setMaxAge(1800);
        assertThat(cookie.getMaxAge(), is(1800));
    }

    @Test
    public void setDomain() {
        cookie.setDomain("localhost:8080");
        assertThat(cookie.getDomain(), is("localhost:8080"));
    }

    @Test
    public void httpOnly() {
        cookie.setHttpOnly(true);
        assertThat(cookie.getHttpOnly(), is(true));
    }

    @Test
    public void secure() {
        cookie.setSecure(true);
        assertThat(cookie.getSecure(), is(true));
    }

    @Test
    public void setExpires() {
        cookie.setExpires(LocalDateTime.of(1996, 12, 8, 0, 0));
        assertThat(cookie.getExpires(), is(LocalDateTime.of(1996, 12, 8, 0, 0)));
    }

    @Test
    public void cookieToString() {
        cookie.setMaxAge(1800);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setExpires(LocalDateTime.of(1996, 12, 8, 0, 0));
        cookie.setDomain("localhost:8080");
        assertThat(cookie.toString(), is("logined=false; Max-Age=1800; Domain=localhost:8080; HttpOnly; Secure; Expires=Sun, 08 Dec 1996 00:00:00 GMT"));
    }
}
