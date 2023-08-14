package session;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static final Map<String, HttpSession> sessions = new HashMap<>();

    static void addSession(final HttpSession session) {
        sessions.put(session.getId(), session);
    }

    static HttpSession getSession(final String sessionId) {
        return sessions.get(sessionId);
    }
}
