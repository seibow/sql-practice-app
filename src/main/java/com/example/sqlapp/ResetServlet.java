package com.example.sqlapp;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/reset")
public class ResetServlet extends HttpServlet {
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		
		if (session == null) {
			HttpSession newSession = request.getSession(true);
			newSession.setAttribute("flashMessage", "セッションの有効期限が切れていたため、新しい環境を作成しました");
			newSession.setAttribute("flashType", "success");
			response.sendRedirect("/");
			return;
		}
		
		String schemaName = (String) session.getAttribute("schemaName");
		
		try {
			SandboxManager.resetSandbox(schemaName);
			session.setAttribute("flashMessage", "データベースをリセットしました");
			session.setAttribute("flashType", "success");
		} catch (SQLException e) {
			session.setAttribute("flashMessage", "リセットに失敗しました" + e.getMessage());
			session.setAttribute("flashType", "error");
		}
		
		response.sendRedirect("/");
	}

}
