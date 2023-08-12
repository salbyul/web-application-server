package controller;

import db.DataBase;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCreateController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(UserCreateController.class);
    private static final UserCreateController controller = new UserCreateController();

    private UserCreateController() {}

    protected static UserCreateController getInstance() {
        return controller;
    }

    @Override
    public void doGet(final HttpRequest request, final HttpResponse response) {

    }

    @Override
    public void doPost(final HttpRequest request, final HttpResponse response) {
        User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email"));
        DataBase.addUser(user);
        log.info("New User: {}", user);
        response.redirect("/index.html");
    }
}
