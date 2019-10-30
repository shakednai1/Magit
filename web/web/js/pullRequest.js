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

    setStatus(pr.status, pr.reason);
}


function setStatus(status, reason) {
    if (status !== "NEW") {
        document.getElementById("prActions").style.display = "none";
        document.getElementById("prStatus").style.display = "block";
        $("#prStatus").append("Pull request " + status);
        $("#prStatus").append("<br>Reason: " + reason);
    } else {
        document.getElementById("prActions").style.display = "block";
    }
}

function updateChangedFiles(pr) {

    for (var i = 0; i < pr.newFiles.length; i++) {
        addFileChangeToTable(pr.newFiles[i], "NEW");
    }

    for (var i = 0; i < pr.updateFiles.length; i++) {
        addFileChangeToTable(pr.updateFiles[i], "UPDATE");
    }

    for (var i = 0; i < pr.deleteFiles.length; i++) {
        addFileChangeToTable(pr.deleteFiles[i], "DELETE");
    }
}


function addFileChangeToTable(changedFile, state) {

    var path = changedFile.path;
    var sha1 = changedFile.sha1;
    var markup = "<tr><td>" + path + "</td><td>" + state + "</td>";

    if (state === "NEW" || state === "UPDATE")
        var extra = "<td><button onClick='getContent(\"" + sha1 + "\",\"" + path + "\" )'>Show Content</button></td></tr>";
    else
        var extra = "<td></td></tr>";

    markup = markup + extra;

    $("#changedFiles   > tbody:last-child").append(markup);
}

function getContent(sha1, path) {
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
    $("#fileContentPath").html(res.path);
    $("#fileContentText").val(res.content);
}

function updateStatus(status) {
    var reason = $("#message").val();
    $.ajax("/pull_request",
        {
            url: "/pull_request",
            type: "PUT",
            contentType: "application/json",
            processData: false,
            cache: false,
            data: JSON.stringify({'status': status, 'prID': prUID, 'reason': reason})
        })
        .success(function () {
            var reason = $("#message").val();
            setStatus(status, reason);
            $("#prActions").find('form').remove();
    });
}

function prepareFinalStatus(status) {
    $("#acceptBtn").hide();
    $("#declineBtn").hide();
    var statusChangeTextForm = $("<form id='statusChangeTextForm'><textarea name='message' id='message' rows='5' cols='20'>" +
        "</textarea><input type='submit'></form>");
    $("#prActions").append("you chose to " + status + " please add the reason for your choice: ");
    $("#prActions").append(statusChangeTextForm);
    $("#statusChangeTextForm").on('submit',
        function (e) {
            e.preventDefault();
            updateStatus(status);
        });
}
