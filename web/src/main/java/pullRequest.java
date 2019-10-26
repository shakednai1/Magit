import core.RemoteRepository;
import core.Repository;
import models.BranchData;
import user.User;
import pullRequest.PullRequest;
import user.notification.NewPRNotification;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/pull_request")
public class pullRequest extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> params = request.getParameterMap();

        User user = WebUtils.getSessionUser(request);
        Repository activeRepository = user.getEngine().getActiveRepository();
        RemoteRepository remoteRepository = activeRepository.getRemoteRepository();

        // TODO validate that we can make pull request -  cannot if branch RTB or something?
        PullRequest pr = new PullRequest(activeRepository.getName(),
                remoteRepository.getName(),
                user.getName(),
                remoteRepository.getOwnerUser(),
                params.get("comment")[0],
                activeRepository.getBranchByName(params.get("fromBranch")[0]),
                remoteRepository.getBranchByName(params.get("toBranch")[0])
        );

        pr.create();
        new NewPRNotification(pr).save();
    }

}
