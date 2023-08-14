package controller;

import cookie.Cookie;
import db.DataBase;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.HttpSession;
import util.HttpRequestUtils;

import java.io.IOException;

import static util.HttpStatusCode.BAD_REQUEST;

public class UserLoginController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(UserLoginController.class);
    private static final UserLoginController controller = new UserLoginController();

    private UserLoginController() {
    }

    protected static UserLoginController getInstance() {
        return controller;
    }

    @Override
    public void doGet(final HttpRequest request, final HttpResponse response) throws IOException {

    }

    @Override
    public void doPost(final HttpRequest request, final HttpResponse response) throws IOException {
        if (isValidUser(request.getParameter("userId"), request.getParameter("password"))) {
            HttpSession session = request.getSession();
            User user = DataBase.findUserById(request.getParameter("userId"));
            session.setAttribute("user", user);
            log.debug("{} is login!", request.getParameter("userId"));
            response.redirect("/index.html");
            return;
        }
        response.setCode(BAD_REQUEST);
        response.forward(HttpRequestUtils.LOGIN_FAILED_URL);
        response.redirect("/user/login_failed.html");
    }

    private boolean isValidUser(final String userId, final String password) {
        if (userId == null || password == null) {
            return false;
        }
        User user = DataBase.findUserById(userId);
        return user != null && user.getPassword().equals(password);
    }
}
