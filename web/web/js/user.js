// var getCurrUser = function(){$("#currUser").innerText(innerText"dadsg")}
//function() getCurrUser{$("h1").text('<%= Session["user"] %>')}

$(document).ready(function() {
    var currentUser = getCurrUser();
    updateUserNameInPageTitle(currentUser);
    addAllUsersToList();
});

function getCurrUser() {
    return document.cookie.split("user=")[1]
}

function updateUserNameInPageTitle(username){
    document.getElementById("currUser").innerHTML = username;
}

function addRepositoryToUserTable(repo){
        var markup = "<tr><td>"+repo.name+"</td><td>"+
            repo.activeBranch +"</td><td>" +
            repo.numOfBranches +"</td><td>" +
            repo.lastCommitTime +"</td><td>"+
            repo.lastCommitMessage +"</td></tr>";
        $("#myRepos").append(markup);
}

function addRepositoryToOthersTable(repo) {
    var markup = "<tr><td>"+repo.name+"</td><td>"+
        repo.activeBranch +"</td><td>" +
        repo.numOfBranches +"</td><td>" +
        repo.lastCommitTime +"</td><td>"+
        repo.lastCommitMessage +"</td>" +
        "<td><button onClick='fork(\"" + repo.name + "," + username + "," + getCurrUser() + "\")'>fork</button></td>" +
        "</tr>";
    $("#othersRepos").append(markup);
}

// TODO update to take from real data
function addAllRepositoriesToOtherUserTable(username){
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
            addRepositoryToOthersTable(repos[i]);
        }
    });
}



function fork(repoName, fromUser, toUser) {
 //TODO add servlet
}

// TODO update to take from real data
function addAllRepositoriesToTable(){
    var username =  getCurrUser();
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
            addRepositoryToUserTable(repos[i]);
        }
    });
}

function addAllUsersToList(){
    // TODO : change onlyCreated=true
    var currentUser = getCurrUser();
    $.get("/users?onlyCreated=false", function(response){
        var jsonRes = JSON.parse(response)["response"];
        for(i in jsonRes){
            var name = jsonRes[i]["username"];
            if(name !== currentUser){
                $("#usersList").append('<li><a href="#" onClick="addAllRepositoriesToOtherUserTable(\'' + name + '\')"</a>'+name+'</li>');
                }
            }
        }
    );
}

