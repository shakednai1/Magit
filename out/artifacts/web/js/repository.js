$(document).ready(function() {
    $.get('/repository').success(function (response) {
        updateRepoDetails(response);
        updateRepoBranches(response);
        updateHeadBranchCommits();
    });


});

function getCurrUser() {
    return document.cookie.split("user=")[1]
}

function updateRepoDetails(response) {
    var jsonRes = JSON.parse(response);
    document.getElementById("head").innerHTML = "head branch : " + jsonRes.head;
    document.getElementById("currRepo").innerHTML = jsonRes.repoName;

    if(jsonRes.remoteFrom !== null){
        document.getElementById("remoteFrom").innerHTML = "forked from : " + jsonRes.remoteFrom;
        document.getElementById("remoteRepoName").innerHTML = "remote repository name : " + jsonRes.remoteName;
    }
}

function updateRepoBranches(response) {
    var jsonRes = JSON.parse(response);

    var branchesToCheckout = {
        "local branches": [],
        "remote branches": []
    };

    // update local branch in branches list and checkout options
    var localBranches = jsonRes.localBranches;
    for (i in localBranches) {
        $("#branchesList").append('<li>' + localBranches[i] + '</li>');
        branchesToCheckout["local branches"].push({name: localBranches[i]});
    }

    if (jsonRes.remoteFrom !== null) {
        // update remote branches in branches list and checkout options
        var remoteBranches = jsonRes.remoteBranches;
        for (i in remoteBranches) {
            $("#remoteBranchesList").append('<li>' + remoteBranches[i] + '</li>');
            branchesToCheckout["remote branches"].push({name: remoteBranches[i]});
        }
    }
    else {
        document.getElementById("remoteBranchesTitle").style.display = "none";
    }

    var $select = $('#checkout');
    $.each(branchesToCheckout, function (key, value) {
        var group = $('<optgroup label="' + key + '" />');
        $.each(value, function () {
            $('<option />').html(this.name).appendTo(group);
        });
        group.appendTo($select);
    });
}

function checkoutBranch() {
    var e = document.getElementById("checkout");
    var branchName = e.options[e.selectedIndex].text;
    $.post('/branch/checkout', {branchName: branchName}).success(function () {
        document.getElementById("head").innerHTML = "head branch : " + branchName;
        updateHeadBranchCommits();
    });
}

function createNewBranch() {
    var branchName = document.getElementById("createNewBranch").elements.namedItem("branchName").value
    $.post('/branch', {branchName: branchName});
}

function updateHeadBranchCommits() {
    clearTable("commits");
    $.get('/branch').success(function (response) {
       var jsonRes = JSON.parse(response);
       for (i in jsonRes){
           var commit = jsonRes[i];
           var markup = "<tr><td>"+commit.sha1+"</td><td>"+
               commit.message +"</td><td>" +
               commit.commitTime +"</td><td>" +
               commit.committer +"</td><td>"+
               commit.pointingBranches +"</td><td>" +
               "<button onClick='showCommitFileSystem(\"" + commit.sha1 + "\")'>show file system</button></td></tr>";
           $("#commits").append(markup);
       }
    });
}

function showCommitFileSystem(commitSha1) {
    $.get('/commit', {commitSha1: commitSha1})
        .success(function (response) {
        var jsonRes =  JSON.parse(response);
        openFSWindow(commitSha1, jsonRes);
    });
}

function openFSWindow(commitSha1, jsonRes) {
    var url = "fileSystem.jsp";
    var width = 700;
    var height = 600;
    var left = parseInt((screen.availWidth/2) - (width/2));
    var top = parseInt((screen.availHeight/2) - (height/2));
    var windowFeatures = "width=" + width + ",height=" + height +
        ",status,resizable,left=" + left + ",top=" + top +
        "screenX=" + left + ",screenY=" + top + ",scrollbars=yes";
    FSwindows = window.open(url, "subWind", windowFeatures);
    FSwindows.window.onload = function (ev) {
        FSwindows.document.getElementById("FStitle").innerHTML = commitSha1 + " commit file system";
        for (i in jsonRes){
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

function clearTable(tableId){
    var table = document.getElementById(tableId);
    for(var i = table.rows.length - 1; i > 0; i--)
    {
        table.deleteRow(i);
    }
}

