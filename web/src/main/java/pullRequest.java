import com.google.gson.Gson;
import core.RemoteRepository;
import core.Repository;
import exceptions.NoActiveRepositoryError;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import user.User;
import pullRequest.PullRequest;
import user.notification.EditPRNotification;
import user.notification.NewPRNotification;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@WebServlet("/pull_request")
public class pullRequest extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
             IOException {
        String queryString = request.getQueryString();
        String prID = queryString.split(Pattern.quote("="))[1];

        User user = WebUtils.getSessionUser(request);

        try{
            PullRequest pr = new PullRequest(user.getName(), user.getEngine().getCurrentRepoName(), prID);

            pr.setFileChanges(user.getEngine().getActiveRepo());

            Gson gson = new Gson();
            response.getWriter().println(gson.toJson(pr));
        }
        catch (NoActiveRepositoryError e){
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> params = request.getParameterMap();

        User user = WebUtils.getSessionUser(request);
        Repository activeRepository = user.getEngine().getActiveRepository();
        RemoteRepository remoteRepository = activeRepository.getRemoteRepository();

        remoteRepository.loadRemoteBranches();
        if(remoteRepository.getBranchByName(params.get("fromBranch")[0]) == null){
            response.setStatus(400);
            response.setContentType("text/plain");
            response.getWriter().println("Please push branch "+params.get("fromBranch")[0] );
            return;
        }


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

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String data = request.getReader().lines().collect(Collectors.toList()).get(0);

        JSONObject json;
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        PullRequest.PRStatus status = PullRequest.PRStatus.valueOf(json.get("status").toString());
        String prID = json.get("prID").toString();

        User user = WebUtils.getSessionUser(request);

        try{
            PullRequest pr = new PullRequest(user.getName(),
                    user.getEngine().getCurrentRepoName(),
                    prID);

            if(status.name().equals("ACCEPTED"))
                pr.accept(user.getEngine().getActiveRepo());

            pr.setStatus(status);

            EditPRNotification prNoti = new EditPRNotification(pr);
            prNoti.save();
        }
        catch (NoActiveRepositoryError e){
            System.out.println(request.getRequestURI() +":"+ e.getMessage());
        }



    }


    }
