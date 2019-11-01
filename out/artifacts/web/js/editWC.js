$(document).ready(function() {
    updateFilesTable();
    $("#prepareCommitForm").on('submit', function(e) {
        e.preventDefault();
        prepareCommit();
    });


});

function getCurrUser() {
    return document.cookie.split("user=")[1];
}


function updateFilesTable() {
    clearTable("files");
    $.get('/Magit/currentWC').success(function (response) {
        var jsonRes = JSON.parse(response);
        for (i in jsonRes) {
            var fileName = jsonRes[i];
            var markup = "<tr><td>" + fileName + "</td><td>" +
                "<button onClick='viewFileContent(this)'>show content</button></td><td>" +
                "<button onClick='editFile(this)'>edit</button></td><td>" +
                "<button onClick='deleteFile(this)'>delete</button></td></tr>";
            $("#files").append(markup);
        }
    });
}

function viewFileContent(btn) {
    var filePath = $(btn).closest('tr').find('td:first').text();
    $.get('/Magit/currentWC', {fileName : filePath}).success(function (response) {
        var actionsResults =  $("#actionsResults");
        actionsResults.empty();
        var title = $("<p id='filePath' style='color: #2e6c80; font-size: 90%;'>"+ filePath + "</p>");
        var textArea = $("<textarea id='fileContent' readonly='readonly' style='height: 250px; width: 250px; border: 1px solid #ccc; font: 16px/26px Georgia, Garamond, Serif; overflow:auto; '></textarea>");
        response = JSON.parse(response);
        var content = response.replace("\\", "\\\\");
        textArea.text(content);
        actionsResults.append(title);
        actionsResults.append(textArea);

    });
}



function editFile(btn) {
    var filePath = $(btn).closest('tr').find('td:first').text();
    $.get('/Magit/currentWC', {fileName : filePath}).success(function (response) {
        var actionsResults =  $("#actionsResults");
        actionsResults.empty();
        var title = $("<p id='filePath' style='color: #2e6c80; font-size: 90%;'>"+ filePath + "</p>");
        var textArea = $("<textarea id='fileContent' style='height: 250px; width: 250px; border: 1px solid #ccc; font: 16px/26px Georgia, Garamond, Serif; overflow:auto;'></textarea>");
        response = JSON.parse(response);
        var content = response.replace("\\", "\\\\");
        textArea.text(content);
        filePath = filePath.replace("\\", "\\\\");
        var applyChanges = $("<button onclick='updateFile()'> save changes </button>");
        actionsResults.append(title);
        actionsResults.append(textArea);
        actionsResults.append(applyChanges);
    });
}

function updateFile() {
    var newContent = $("#fileContent").val();
    var filePath = $("#filePath").text();
    $.post('/Magit/currentWC' , {content : newContent, fileName: filePath}).success(function (response) {
    })
}

function deleteFile(btn) {
    var filePath = $(btn).closest('tr').find('td:first').text();
    filePath = filePath;
    $.ajax({
        url: '/Magit/currentWC/' + filePath,
        type: 'DELETE',
        success: function() {
            updateFilesTable();
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

function createNewFile() {
    var actionsResults =  $("#actionsResults");
    actionsResults.empty();
    $("#createNewFile").hide();
    var textArea = $("<textarea form='newFileForm' id='fileContent' style='height: 250px; width: 250px; border: 1px solid #ccc; font: 16px/26px Georgia, Garamond, Serif; overflow:auto;'></textarea>");
    var form = $("<form id='newFileForm'>" +
        "please provide file path (include file name): <input id='filePath' type='text' name='filePath' >" +
        "<input type='submit' onclick='saveNewFile()' value='create new file'>" +
        "</form>");
    actionsResults.append(form);
    actionsResults.append(textArea);
}

function saveNewFile() {
    var newContent = $("#fileContent").val();
    var filePath = $("#filePath").val();
    $.post('/Magit/currentWC' , {content : newContent, fileName: filePath}).success(function () {
        updateFilesTable();
    });
    $("#createNewFile").show();

}

function prepareCommit() {
    $("#prepareCommitForm").hide();
    var form = $("<form id='commitMsgForm'>" +
        "please provide commit message: <input id='commitMsg' type='text' name='commitMsg' >" +
        "<input type='submit' value='commit'>" +
        "</form>");
    $("#commitDiv").append(form);
    $("#commitMsgForm").on('submit', function(e) {
        e.preventDefault();
        commit();
    });

}

function commit() {
    var commitMsg = $("#commitMsg").val();
    $.post('/Magit/commit', {commitMsg : commitMsg}).error(function (xhRequest, errorText, thrownError) {
        alert("There are no changes to commit");
    })
    $("#prepareCommitForm").show();
    $("#commitMsgForm").remove();
}