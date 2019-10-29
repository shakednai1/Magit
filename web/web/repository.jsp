
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<link rel="stylesheet" type="text/css" href="commitsTable.css">
<link rel="stylesheet" type="text/css" href="common.css">

<script src='https://code.jquery.com/jquery-2.2.4.min.js'></script>
<script src="js/repository.js"></script>
<body>
<p>&nbsp;</p>
<h1 id="currRepo" style="color: #5e9ca0;">&nbsp;</h1>
<form align="right" action="/index.html"><input type="submit" value="Logout" /></form>
<form align="right" action="user.jsp"><input type="submit" value="Back to all user's repositories" /></form>
<h2 id="remoteFrom" style="color: #5e9ca0;"></h2>
<h2 id="remoteRepoName" style="color: #5e9ca0;"></h2>

<hr>

<h2 style="color: #2e6c80;">Branches</h2>
<ul id="branchesList"></ul>
<h2 id="remoteBranchesTitle" style="color: #2e6c80;">Remote branches</h2>
<ul id="remoteBranchesList"></ul>
<p id="head" style="color: #2e6c80; font-size: 90%;">head branch:</p>
<p style="color: #2e6c80; font-size: 90%;">create new branch:</p>
<form id="createNewBranch" enctype="multipart/form-data"><input name="branchName" type="text" /> <input type="submit" value="create" onClick="createNewBranch()"/></form>
<p style="color: #2e6c80; font-size: 90%;">checkout branch:</p>
<select id="checkout"></select><button onClick="checkoutBranch()">checkout</button>
<br />
<div id="collaboration" style="display:none">
    <p>Collaboration actions:</p>
    <button onClick="push()">push</button>
    <button onClick="pull()">pull</button>
</div>

<hr>

<h2 style="color: #2e6c80;">Pull Requests</h2>
<button id="EnablePullRequest" onclick="togglePullRequest()" value="Open Pull Request">Open Pull Request</button>

<div id="PullRequest" >
    <form id="PullRequestForm"><br />
        From branch :<select  id="prFromBranch" required="true"> </select> &nbsp; &nbsp; &nbsp; &nbsp;
        To Branch:&nbsp;<select id="prToBranch" required="true"> </select><br />
        Description: <br /> <textarea id="prDescription" cols="60" required="true" rows="10"></textarea> <br />
        <input type="submit" value="Create">
    </form>
</div>
<br>
<div id="repoPullRequests">
    <table id="repoPullRequestsTable" class="commitsTable">
        <script>addAllRepositoryPullRequests()</script>
        <thead>
        <tr>
            <td>Requesting User</td>
            <td>From Branch</td>
            <td>To Branch</td>
            <td>Comment</td>
            <td>Time</td>
            <td>Action</td>
        </tr>
        </thead>
        <tbody></tbody>
    </table>
</div>

<hr>
<button onClick="openWCwindow()">edit current WC</button>
<br>

<h2 style="color: #2e6c80;">Head branch commits</h2>
<table id="commits" class="commitsTable">
    <thead>
    <tr>
        <td>sha1</td>
        <td>message</td>
        <td>time</td>
        <td>created by</td>
        <td>pointed branches</td>
        <td>action</td>
    </tr>
    </thead>
</table>
<h2 style="color: #2e6c80;">Notifications</h2>
<button id="refreshNotification" onclick="setNotifications()">refresh</button>
<div id="userNotification" style="height: 500px; width: 1200px; border: 1px solid #ccc; font: 16px/26px Georgia, Garamond, Serif; overflow: auto;">
    <ol id="userNotificationList"></ol>
</div>
</body>
</html>
