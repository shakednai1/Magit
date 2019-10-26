<%--
  Created by IntelliJ IDEA.
  User: shaked
  Date: 24/10/2019
  Time: 20:28
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<link rel="stylesheet" type="text/css" href="commitsTable.css">
<script src='https://code.jquery.com/jquery-2.2.4.min.js'></script>
<script src="js/editWC.js"></script>
<body>
<h2 style="color: #2e6c80;">Current commit WC</h2>
<div id="commitDiv">
<form id="prepareCommitForm" align="right"><input type="submit" value="Commit"/></form>
</div>
<table id="files" class="commitsTable">
    <thead>
    <tr>
        <td>file path</td>
        <td>view</td>
        <td>edit</td>
        <td>delete</td>
    </tr>
    </thead>
</table>
<button id="createNewFile" onclick="createNewFile()">create new file</button>
<div id="actionsResults"></div>
</body>
</html>

<style>
    textarea{
        width: 500px;
        min-height: 50px;
        font-family: Arial, sans-serif;
        font-size: 13px;
        color: #444;
        padding: 5px;
    }
    .noscroll{
        overflow: hidden;
        resize: none;
    }
    .hiddendiv{
        display: none;
        white-space: pre-wrap;
        width: 500px;
        min-height: 50px;
        font-family: Arial, sans-serif;
        font-size: 13px;
        padding: 5px;
        word-wrap: break-word;
    }
    .lbr {
        line-height: 3px;
    }
</style>
