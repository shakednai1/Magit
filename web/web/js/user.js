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
            repo.lastCommitMessage +"</td>" +
            "<td><button onClick='openRepoPage(\"" + repo.name +  "\")'>open repo</button></td></tr>";
        $("#myRepos").append(markup);
}

function openRepoPage(repoName) {
    //TODO servlet to get repo page jsp
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
    var itemData=new FormData($("#load-xml")[0]);

    $.ajax("/upload",
        {url: "/upload",
            type: "POST",
            cache: false,
            contentType: false,
            processData: false,
            data: itemData,
            dataType: "json",
            success:addAllRepositoriesToTable()});
}

function init(){
    document.getElementById('load-xml').onsubmit = loadXml;
}

window.onload = init;