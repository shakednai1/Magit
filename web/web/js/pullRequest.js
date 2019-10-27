var prUID = "";

$(document).ready(function () {
    var url = new URL(document.location.href);
    $.get('/pull_request', {id: url.searchParams.get("id")})
        .success(function (response) {
            var pr = JSON.parse(response);

            updatePRDetails(pr);
            updateChangedFiles(pr);

        });

    document.getElementById("fileContent").style.display = "none";
});

function updatePRDetails(pr) {
    prUID = pr.sha1.sha1;

    $("#prID").append(pr.sha1.sha1);
    $("#prTime").append(pr.creationTime);
    $("#prUser").append(pr.requestingUser);
    $("#branches").append(pr.fromBranch + " > " + pr.toBranch);
    $("#comment").append(pr.comment);
    $("#repoName").append(pr.remoteRepoName);


    setStatus(pr.status);
}


function setStatus(status){
    if(status !== "NEW"){
        document.getElementById("prActions").style.display = "none";
        document.getElementById("prStatus").style.display = "block";
        $("#prStatus").append("Pull request " + status);
    }
    else{
        document.getElementById("prActions").style.display = "block";
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


function addFileChangeToTable(changedFile, state) {

    var path  = changedFile.path;
    var sha1  = changedFile.sha1;
    var markup = "<tr><td>" + path + "</td><td>" + state + "</td>";

    if(state === "NEW" || state === "UPDATE")
        var extra = "<td><button onClick='getContent(\"" + sha1 + "\",\"" + path +"\" )'>Show Content</button></td></tr>";
    else
        var extra = "<td></td></tr>" ;

    markup = markup + extra;

    $("#changedFiles   > tbody:last-child").append(markup);
}

function getContent(sha1, path){
    $.get("/file", {sha1: sha1, path: path})
        .success(
            function (response) {
                document.getElementById("fileContent").style.display = "block";
                updateFileContent(response);
            }
        );
}

function updateFileContent(response) {
    var res = JSON.parse(response);
    document.getElementById("fileContentPath").append(res.path);
    document.getElementById("fileContentText").append(res.content);
}

function updateStatus(status){
    $.ajax("/pull_request",
    {url: "/pull_request",
        type: "PUT",
        contentType: "application/json",
        processData: false,
        data: JSON.stringify({'status': status, 'prID':prUID})
    }).success( function(){
        setStatus(status);
    });
}