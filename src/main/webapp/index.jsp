<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>SQL練習アプリ</title>
</head>
<body>
    <% if (request.getAttribute("flashMessage") != null) { %>
      <% String type = (String)request.getAttribute("flashType"); %>
      <p style="color:<%= "success".equals(type) ? "green" : "red" %>">
        ${flashMessage}
      </p>
    <% } %>
      
	<h1>SQL練習アプリ</h1>
	<p>あなたの環境: ${schemaName}</p>
	
	<form method="post" action="/execute">
		<textarea name="sql" rows="5" cols="50"></textarea><br>
		<button type="submit">実行</button>
	</form>
	
	<form method="post" action="/reset">
		<button type="submit">データベースリセット</button>
	</form>
</body>
</html>
