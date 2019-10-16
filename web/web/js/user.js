// var getCurrUser = function(){$("#currUser").innerText(innerText"dadsg")}
//function() getCurrUser{$("h1").text('<%= Session["user"] %>')}

$(document).ready(function() {
    addAllRepositoriesToTable();
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

function addAllRepositoriesToTable() {
    //ajax get all user repos
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
        addRepositoryToTable(repos[i]);
    }
}

function addAllUsersToList(){
    // TODO : change onlyCreated=true
    // TODO: fix href
    $.get("/users?onlyCreated=false", function(response){
        var jsonRes = JSON.parse(response)["response"];
        for(i in jsonRes){
                $("#usersList").append('<li><a href="/stam">'+ jsonRes[i]["username"] +'</a></li>');
            }
        }
    );
}

