
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<script src='https://code.jquery.com/jquery-2.2.4.min.js'></script>
<script src="js/repository.js"></script>
<body>
<p>&nbsp;</p>
<h1 id="currRepo" style="color: #5e9ca0;">&nbsp;</h1>
<form action="/index.html"><input type="submit" value="Logout" /></form>
<h2 id="forkedFrom" style="color: #5e9ca0;">&nbsp;</h2>
<h2 style="color: #2e6c80;">Branches</h2>
<ul id="branchesList"></ul>
<p style="color: #2e6c80; font-size: 90%;">head branch:</p>
<p style="color: #2e6c80; font-size: 90%;">create new branch:</p>
<form id="createNewBranch" enctype="multipart/form-data"><input name="branchName" type="text" /> <input type="submit" value="create" /></form>
<p><strong>&nbsp;</strong></p>

<h2 style="color: #2e6c80;">Head branch commits</h2>
<table id="commits" class="reposTable">
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
</body>
</html>
