package session;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {

    public static final String SESSION_COOKIE_KEY = "JSESSIONID";
    private final String id;
    private final Map<String, Object> body;

    private HttpSession(final String id) {
        this.id = id;
        body = new HashMap<>();
        SessionManager.addSession(this);
    }

    public static HttpSession getSession(final String sessionId) {
        HttpSession session = SessionManager.getSession(sessionId);
        if (session == null) {
            return new HttpSession(sessionId);
        }
        return session;
    }

    public String getId(){
        return this.id;
    }

    public void setAttribute(final String name, final Object value) {
        this.body.put(name, value);
    }

    public Object getAttribute(final String name) {
        return this.body.get(name);
    }

    public void removeAttribute(final String name) {
        this.body.remove(name);
    }

    public void invalidate() {
        this.body.clear();
    }
}
