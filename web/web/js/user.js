
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
            repo.lastCommitMessage +"</td>" +
            "<td><form method='post' id='openRepoForm'><button type='submit' form='openRepoForm' value='Submit' onClick='openRepoPage(\"" + repo.name + "\")'>open Repo</button></td></tr>";
        $("#myRepos").append(markup);
}

function openRepoPage(repoName) {
    $.post("/repository", {repoName: repoName});
}

function addRepositoryToOthersTable(repo, username) {
    var markup = "<tr><td>"+repo.name+"</td><td>"+
        repo.activeBranch +"</td><td>" +
        repo.numOfBranches +"</td><td>" +
        repo.lastCommitTime +"</td><td>"+
        repo.lastCommitMessage +"</td>" +
        "<td><button onClick='fork(\"" + repo.name +  "\",\"" + username + "\")'>fork</button></td></tr>";
    $("#othersRepos").append(markup);
}

function addAllRepositoriesToOtherUserTable(username){
    var table = document.getElementById("othersRepos");
    clearTable("othersRepos");
    table.deleteCaption();
    table.createCaption().innerHTML = username + " repositories";
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
            addRepositoryToOthersTable(jsonRes[i], username);
        }
    });
}

function fork(repoName, fromUser) {
    $.post('/fork', {fromUser: fromUser, repoName: repoName});
    addAllRepositoriesToTable();
}

function addAllRepositoriesToTable(){
    var username =  getCurrUser();
    clearTable("myRepos");
    $.get("/usersRepos", {username: username}, function(response) {
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
            addRepositoryToUserTable(jsonRes[i]);
        }
    });
}

function clearTable(tableId){
    var table = document.getElementById(tableId);
    for(var i = table.rows.length - 1; i > 0; i--)
    {
        table.deleteRow(i);
    }
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

function loadXml(){
    $.ajax("/upload",
        {url: "/upload",
        type: "POST",
        cache: false,
        contentType: false,
        processData: false,
        data: new FormData($("#load-xml")[0]),
        success: function() { addAllRepositoriesToTable(); },
        error: function (data) { alert(data); }});
}



function init(){
    $("#load-xml").on('submit', function(e) {
        e.preventDefault();
        loadXml();
    });
}

window.onload = init;