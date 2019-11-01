<!DOCTYPE html>
<html lang="en">

<script src='https://code.jquery.com/jquery-2.2.4.min.js'></script>
<script src="js/user.js"></script>

<body>
<h1 id="currUser" style="color: #5e9ca0;"></h1>

<form align="right" action="/Magit/index.html"><input type="submit" value="Logout"/></form>

<p style="color: #2e6c80; font-size: 90%;">Load new repository:</p>
<form id="load-xml" enctype="multipart/form-data">
    <input accept="text/xml" name="xmlFile" type="file"/>
    <input type="submit" value="Load"/>
</form>


<h2 style="color: #2e6c80;">My repositories</h2>
<link rel="stylesheet" type="text/css" href="reposTable.css">
<table id="myRepos" class="reposTable">
    <script>addAllRepositoriesToTable()</script>
    <thead>
    <tr>
        <td>repository name</td>
        <td>active branch</td>
        <td>num of branches</td>
        <td>last commit time</td>
        <td>last commit massage</td>
        <td>action</td>
    </tr>
    </thead>
</table>

<p><strong>&nbsp;</strong></p>
<h2 style="color: #2e6c80;">Magit users</h2>
<ul id="usersList"></ul>
<table id="othersRepos" class="reposTable">
    <caption>Others repositories</caption>
    <thead>
    <tr>
        <td>repository name</td>
        <td>active branch</td>
        <td>num of branches</td>
        <td>last commit time</td>
        <td>last commit massage</td>
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
