// var getCurrUser = function(){$("#currUser").innerText(innerText"dadsg")}
//function() getCurrUser{$("h1").text('<%= Session["user"] %>')}

$(document).ready(function() {
    addAllUsersToList();
});


function addRepositoryToTable(repo){
        var markup = "<tr><td>"+repo.name+"</td><td>"+
            repo.activeBranch +"</td><td>" +
            repo.numOfBranches +"</td><td>" +
            repo.lastCommitTime +"</td><td>"+
            repo.lastCommitMessage +"</td></tr>";
        $("#myRepos").append(markup);
}

function addAllRepositoriesToOtherUserTable(username){
    // TODO servlet to get all user's repo
    $.get("/usersRepos?username=" + username, function(response) {
        $("#othersRepos").append('<caption>'+ username + ' repositories' + '</caption>');
        var jsonRes = JSON.parse(response)["response"];
        var repos = [{
            "name": "repo1",
            "activeBranch": "branch1",
            "numOfBranches": 4,
            "lastCommitTime": "12.10.2019 34:56:34",
            "lastCommitMessage": "this is the last"
        },
            {
                "name": "repo2",
                "activeBranch": "branch2",
                "numOfBranches": 7,
                "lastCommitTime": "13.14.2019 35:23:33",
                "lastCommitMessage": "this is the last second repo"
            }];
        for (i in repos) {
            var repo = repos[i];
            var markup = "<tr><td>"+repo.name+"</td><td>"+
                repo.activeBranch +"</td><td>" +
                repo.numOfBranches +"</td><td>" +
                repo.lastCommitTime +"</td><td>"+
                repo.lastCommitMessage +"</td></tr>";
            $("#othersRepos").append(markup);
        }
    });
}

function addAllRepositoriesToTable(username){
    // TODO servlet to get all user's repo
    $.get("/usersRepos?username=" + username, function(response) {
        var jsonRes = JSON.parse(response)["response"];
        var repos = [{
            "name": "repo1",
            "activeBranch": "branch1",
            "numOfBranches": 4,
            "lastCommitTime": "12.10.2019 34:56:34",
            "lastCommitMessage": "this is the last"
        },
            {
                "name": "repo2",
                "activeBranch": "branch2",
                "numOfBranches": 7,
                "lastCommitTime": "13.14.2019 35:23:33",
                "lastCommitMessage": "this is the last second repo"
            }];
        for (i in jsonRes) {
            addRepositoryToTable(repos[i]);
        }
    });
}

function addAllUsersToList(){
    // TODO : change onlyCreated=true
    // TODO: fix href
    $.get("/users?onlyCreated=false", function(response){
        var jsonRes = JSON.parse(response)["response"];
        for(i in jsonRes){
            var name = jsonRes[i]["username"];
            $("#usersList").append('<li><a href="#" onClick="addAllRepositoriesToOtherUserTable(\'' + name + '\')"</a>'+name+'</li>');
            }
        }
    );
}

