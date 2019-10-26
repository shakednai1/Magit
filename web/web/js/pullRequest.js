$(document).ready(function () {
    var url = new URL(document.location.href);
    $.get('/pull_request', {id: url.searchParams.get("id")})
        .success(function (response) {
            var pr = JSON.parse(response);

            updatePRDetails(pr);
            updateChangedFiles(pr);
        });

});

function updatePRDetails(pr) {
    $("#prID").append(pr.sha1.sha1);
    $("#prTime").append(pr.creationTime);
    $("#prUser").append(pr.requestingUser);
    $("#branches").append(pr.fromBranch + " > " + pr.toBranch);
    $("#comment").append(pr.comment);
    $("#repoName").append(pr.remoteRepoName);

    if(pr.status !== "NEW"){
        $("#prActions").style.display = "none";
        $("#prStatus").style.display = "block";
        $("#prStatus").append("Pull request " + pr.state);
    }
    else{
        $("#prActions").style.display = "block";
    }

}

function updateChangedFiles(pr) {

    for (var i=0; i < pr.newFiles.length; i++){
        addFileChangeToTable(pr.newFiles[i], "NEW");
    }

    for (var i=0; i < pr.updateFiles.length; i++){
        addFileChangeToTable(pr.updateFiles[i], "UPDATE");
    }

    for (var i=0; i < pr.deleteFiles.length; i++){
        addFileChangeToTable(pr.deleteFiles[i], "DELETE");
    }
}


function addFileChangeToTable(path, state) {

    var markup = "<tr><td>" + path + "</td><td>" + state + "</td>";

    if(state === "NEW" || state === "UPDATE")
        var extra = "<td><button onClick='getContent(\"" + path + "\")'>Show Content</button></td></tr>";
    else
        var extra = "<td></td></tr>" ;

    markup += extra;

    $("#changedFiles   > tbody:last-child").append(markup);
}

function getContent(path){

}