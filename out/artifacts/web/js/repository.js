var currBranchesNames = {local: [], remote: []};


$(document).ready(function () {
    $.get('/repository').success(function (response) {
        updateRepoDetails(response);
        updateRepoBranches(response);
        updateHeadBranchCommits();
    });

    $("#PullRequestForm").on('submit', function (e) {
        e.preventDefault();
        openPullRequest();
    });

    setInterval( function (){ addAllRepositoryPullRequests()}, 20000);
    setInterval( updateHeadBranchCommits, 5000)

});

function getCurrUser() {
    return document.cookie.split("user=")[1];
}

function push() {
    $.post('/collaboration', {action: "push"})
        .success(function () {
            $.get('/repository').success(function (response) {
                updateRepoBranches(response);
            });
        });
}

function pull() {
    $.post('/collaboration', {action: "pull"})
        .success(function () {
            $.get('/repository').success(function (response) {
                updateRepoBranches(response);
            });
        });


}

function updateRepoDetails(response) {
    var jsonRes = JSON.parse(response);
    document.getElementById("head").innerHTML = "head branch : " + jsonRes.head;
    document.getElementById("currRepo").innerHTML = jsonRes.repoName;

    if (jsonRes.remoteFrom !== null) {
        var remoteFrom = jsonRes.remoteFrom.split("ex3")[1];
        remoteFrom = remoteFrom.split(jsonRes.remoteName)[0];
        document.getElementById("remoteFrom").innerHTML = "forked from : " + remoteFrom.substring(1, remoteFrom.length -1);
        document.getElementById("remoteRepoName").innerHTML = "remote repository name : " + jsonRes.remoteName;
    } else {
        document.getElementById("EnablePullRequest").style.display = "none";
        document.getElementById("PullRequest").style.display = "none";
        // document.getElementById("PullRequestForm").style.display = "none";

    }
}

function updateRepoBranches(response) {
    var jsonRes = JSON.parse(response);

    currBranchesNames.local = jsonRes.localBranches;
    currBranchesNames.remote = jsonRes.remoteBranches;

    _updateRepoBranchesMain();
    _updateRepoBranchesCheckout();
    _updateRepoBranchesPullRequest();
    _updateCollaboration();
}

function _updateCollaboration() {
    if(currBranchesNames.remote != null){
        $("#collaboration").show();
    }
}

function _updateRepoBranchesMain() {

    function _updateBranchesInnerList(listElement, itemCls, listValue) {
        //listElement.remove(itemCls);
        listElement.empty();
        for (i in listValue) {
            listElement.append('<li class=${itemCls}>' + listValue[i] + '</li>');
        }
    }
    _updateBranchesInnerList($("#branchesList"), "branchNameItem", currBranchesNames.local);
    _updateBranchesInnerList($("#remoteBranchesList"), "remoteBranchNameItem", currBranchesNames.remote);

    if (currBranchesNames.remote == null)
        document.getElementById("remoteBranchesTitle").style.display = "none";

}

function _updateRepoBranchesCheckout() {
    var $select = $('#checkout');
    $select.empty();

    $.each(currBranchesNames, function (key, value) {
        var group = $('<optgroup label="' + key + '" />');
        $.each(value, function () {
            $('<option />').html(this).appendTo(group);
        });
        group.appendTo($select);
    });
}


function _updateRepoBranchesPullRequest() {
    $("#prFromBranch").empty();
    $("#prToBranch").empty();
    for (i in currBranchesNames.local)
        $("#prFromBranch").append('<option>' + currBranchesNames.local[i] + '</option>');

    for (i in currBranchesNames.remote)
        $("#prToBranch").append('<option>' + currBranchesNames.remote[i] + '</option>');

}

function checkoutBranch() {
    var e = document.getElementById("checkout");
    var branchName = e.options[e.selectedIndex].text;
    $.post('/branch/checkout', {branchName: branchName})
        .success(function () {
            document.getElementById("head").innerHTML = "head branch : " + branchName;
            updateHeadBranchCommits();
        });
}

function createNewBranch() {
    var branchName = document.getElementById("createNewBranch").elements.namedItem("branchName").value
    $.post('/branch', {branchName: branchName}).error(function (xhr, status, error) {
        alert(xhr.responseText);
    });
}

function updateHeadBranchCommits() {
    clearTable("commits");
    $.get('/branch').success(function (response) {
        var jsonRes = JSON.parse(response);
        for (i in jsonRes) {
            var commit = jsonRes[i];
            var markup = "<tr><td>" + commit.sha1 + "</td><td>" +
                commit.message + "</td><td>" +
                commit.commitTime + "</td><td>" +
                commit.committer + "</td><td>" +
                commit.pointingBranches + "</td><td>" +
                "<button onClick='showCommitFileSystem(\"" + commit.sha1 + "\")'>show file system</button></td></tr>";
            $("#commits").append(markup);
        }
    });
}

function showCommitFileSystem(commitSha1) {
    $.get('/commit', {commitSha1: commitSha1})
        .success(function (response) {
            var jsonRes = JSON.parse(response);
            openFSWindow(commitSha1, jsonRes);
        });
}

function openFSWindow(commitSha1, jsonRes) {
    var url = "fileSystem.jsp";
    var width = 700;
    var height = 600;
    var left = parseInt((screen.availWidth / 2) - (width / 2));
    var top = parseInt((screen.availHeight / 2) - (height / 2));
    var windowFeatures = "width=" + width + ",height=" + height +
        ",status,resizable,left=" + left + ",top=" + top +
        "screenX=" + left + ",screenY=" + top + ",scrollbars=yes";
    FSwindows = window.open(url, "subWind", windowFeatures);
    FSwindows.window.onload = function (ev) {
        FSwindows.document.getElementById("FStitle").innerHTML = commitSha1 + " commit file system";
        for (i in jsonRes) {
            var ul = FSwindows.document.getElementById("files");
            var li = FSwindows.document.createElement("li");
            li.appendChild(document.createTextNode(jsonRes[i]));
            ul.appendChild(li);
        }
    }
    //FSwindows.document.body.innerHTML = "<h2> commit "+ commitSha1 + " file system <\h2><ul><li>first</li></ul>";
}

function openWCwindow() {
    var url = "editWC.jsp";
    var width = 700;
    var height = 600;
    var left = parseInt((screen.availWidth/2) - (width/2));
    var top = parseInt((screen.availHeight/2) - (height/2));
    var windowFeatures = "width=" + width + ",height=" + height +
        ",status,resizable,left=" + left + ",top=" + top +
        "screenX=" + left + ",screenY=" + top + ",scrollbars=yes";
    window.open(url, "subWind", windowFeatures);
}

function clearTable(tableId) {
    var table = document.getElementById(tableId);
    for (var i = table.rows.length - 1; i > 0; i--) {
        table.deleteRow(i);
    }
}


function togglePullRequest() {
    var x = document.getElementById("PullRequest");
    if (x.style.display === "none") {
        x.style.display = "block";
        document.getElementById("EnablePullRequest").innerHTML = "Cancel Pull Request";
    } else {
        x.style.display = "none";
        document.getElementById("EnablePullRequest").innerHTML = "Open Pull Request";

    }
}

function openPullRequest() {
    var x = $("#PullRequestForm");
    var fromBranch = x.find("#prFromBranch").val();
    var toBranch = x.find("#prToBranch").val();
    var prDescription = x.find("#prDescription").val();

    $.post("/pull_request",
        {fromBranch: fromBranch, toBranch: toBranch, comment: prDescription})
        .success(alert("PR published"));
}

function addAllRepositoryPullRequests() {
    $.get("/pull_requests")
        .success(function (response) {
            clearTable("repoPullRequestsTable");

            var jsonRes = JSON.parse(response)["response"];
            for(var i = 0; i < jsonRes.length ; i++)
            {
                addPullRequestToTable(JSON.parse(jsonRes[i]));
            }
        });
}


function addPullRequestToTable(pr) {

    var markup = "<tr><td>" + pr.requestingUser + "</td><td>" +
        pr.fromBranch + "</td><td>" +
        pr.toBranch + "</td><td>" +
        pr.comment + "</td><td>" +
        pr.creationTime + "</td>" +
        "<td><button onClick='openPullRequestWindow(\"" + pr.sha1.sha1 + "\")'>Open</button></td></tr>";
    $("#repoPullRequestsTable   > tbody:last-child").append(markup);
}

function openPullRequestWindow(prSha1) {

}


