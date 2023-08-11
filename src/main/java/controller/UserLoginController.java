package controller;

import cookie.Cookie;
import db.DataBase;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import static util.HttpStatusCode.BAD_REQUEST;

public class UserLoginController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(UserLoginController.class);
    private static final UserLoginController controller = new UserLoginController();

    private UserLoginController() {}

    protected static UserLoginController getInstance() {
        return controller;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpResponse httpResponse) {

        if (isValidUser(httpRequest.getParameter("userId"), httpRequest.getParameter("password"))) {
            httpResponse.addCookie(new Cookie("logined", "true"));
            log.debug("{} is login!", httpRequest.getParameter("userId"));
            httpResponse.redirect("/index.html");
            return;
        }
        httpResponse.setCode(BAD_REQUEST);
        httpResponse.addCookie(new Cookie("logined", "false"));
        httpResponse.forward(HttpRequestUtils.LOGIN_FAILED_URL);
        httpResponse.redirect("/user/login_failed.html");
    }

    private boolean isValidUser(final String userId, final String password) {
        if (userId == null || password == null) {
            return false;
        }
        User user = DataBase.findUserById(userId);
        return user != null && user.getPassword().equals(password);
    }
}
