
<link rel="stylesheet" type="text/css" href="commitsTable.css">
<script src='https://code.jquery.com/jquery-2.2.4.min.js'></script>
<script src="js/pullRequest.js"></script>

<h1>Pull Request <span id="prID"></span> </h1>
<h5> opened at <span id="prTime"></span> by user <span id="prUser"></span>
</h5>
<div id="prDetails">
    <p>Repository:&nbsp; <span id="repoName"> </span> </p>
    <strong id="branches"></strong>
    <p>Description</p>
    <textarea id="comment" readonly=true></textarea>
</div>
<div id="prActions" align="left">
    <button id="accept" type="submit"> Accept </button>
    <button id="decline" type="submit"> Decline </button>
</div>
<p id="prStatus"> </p>
<div id="changes">
    <h2> Pull Request Changes </h2>
    <table id="changedFiles" class="commitsTable">
        <thead>
        <tr>
            <td>File Path</td>
            <td>Change Type</td>
            <td>Action</td>
        </tr>
        </thead>
        <tbody> </tbody>
    </table>
</div>
