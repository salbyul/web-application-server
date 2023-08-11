package controller;

import db.DataBase;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCreateController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(UserCreateController.class);
    private static final UserCreateController controller = new UserCreateController();

    private UserCreateController() {}

    protected static UserCreateController getInstance() {
        return controller;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpResponse httpResponse) {
        User user = new User(httpRequest.getParameter("userId"), httpRequest.getParameter("password"), httpRequest.getParameter("name"), httpRequest.getParameter("email"));
        DataBase.addUser(user);
        log.info("New User: {}", user);
        httpResponse.redirect("/index.html");
    }
}
