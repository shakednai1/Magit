import user.User;
import user.UserManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "login", urlPatterns = "/login")
public class Login extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = UserManager.addUser(username, password);

        Cookie userCookie = new Cookie("user", username);
        response.addCookie(userCookie);

        response.sendRedirect("http://localhost:8080/user.jsp");
//        user.getEngine().changeActiveRepository("repo 1");
//        response.sendRedirect("http://localhost:8080/pullRequest.jsp?id=1");

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
