<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>SQL練習アプリ</title>
</head>
<body>
	<h1>SQL練習アプリ</h1>
	<p>あなたの環境: ${schemaName}</p>
	<h2>実行したSQL</h2>
	<pre>${sql}</pre>
	
	<!-- SELECT結果の表示 -->
	<% if (request.getAttribute("results") != null) { %>
		<table border="1">
			<tr>
				<% for (String col : (List<String>) request.getAttribute("columns")) { %>
					<th><%= col %></th>
				<% } %>
			</tr>
			<% for (List<String> row : (List<List<String>>) request.getAttribute("results")) { %>
				<tr>
					<% for (String cell : row) { %>
						<td><%= cell %></td>
					<% } %>
				</tr>
			<% } %>
		</table>
	<% } %>
	
	<!-- 更新件数の表示 -->
	<% if (request.getAttribute("affected") != null) { %>
		<p style="color:green">
			${affected}件のデータが更新されました
		</p>
	<% } %>
	
	<!-- エラーの表示 -->
	<% if (request.getAttribute("error") != null) { %>
		<p style="color:red">エラー: ${error}</p>
	<% } %>
	
	<a href="/">戻る</a>
</body>
</html>