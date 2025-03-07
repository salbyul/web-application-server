package controller;

import cookie.Cookie;
import db.DataBase;
import http.request.HttpRequest;
import http.response.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static util.HttpStatusCode.FORBIDDEN;

public class UserListController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(UserListController.class);
    private static final UserListController controller = new UserListController();

    private UserListController() {}

    protected static UserListController getInstance() {
        return controller;
    }

    @Override
    public void doGet(final HttpRequest request, final HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        Object user = session.getAttribute("user");
        if (user == null) {
            response.setCode(FORBIDDEN);
            response.forward("/user/login.html");
            return;
        }
        response.setBody(getBody());
        response.forward("/user/list.html");
    }

    @Override
    public void doPost(final HttpRequest request, final HttpResponse response) {

    }

    private String getBody() throws IOException {
        List<String> strings = Files.readAllLines(new File("./webapp/user/list.html").toPath());
        boolean isDynamicSection = false;
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (isDynamicSection) {
                List<User> allUser = new ArrayList<>(DataBase.findAll());
                for (int i = 1; i <= allUser.size(); i++) {
                    User user = allUser.get(i - 1);
                    sb.append("<tr>\n<th scope=\"row\">")
                            .append(i)
                            .append("</tr> <td>")
                            .append(user.getUserId())
                            .append("</td> <td>")
                            .append(user.getName())
                            .append("</td> <td>")
                            .append(user.getEmail())
                            .append("</td> <td> <a href=\"#\" class=\"btn btn-success\" roll=\"button\">수정</a></td>\n</tr>\n");
                }
                isDynamicSection = false;
            }
            if (string.contains("<tbody>")) {
                isDynamicSection = true;
                continue;
            }
            sb.append(string).append("\n");
        }
        return sb.toString();
    }
}
