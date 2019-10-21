$(document).ready(function() {
    var currentUser = getCurrUser();
    $.get('/repository').success(function (response) {
        updateRepoNameInPageTitle(response);
    });
});

function updateRepoNameInPageTitle(response){
    var d =4;
    document.getElementById("currRepo").innerHTML = response;
}

