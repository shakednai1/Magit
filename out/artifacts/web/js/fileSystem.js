$(document).ready(function() {
    alert("blabla");
    document.getElementById("FStitle").innerHTML = getCommitSha1();


});


function getCommitSha1(){
    var commit = window.location.split("?")[1];
    alert(commit);
    alert(commit.split("=")[1]);

    return commit.split("=")[1];
}

