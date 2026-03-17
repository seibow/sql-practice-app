package com.example.sqlapp;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/reset")
public class ResetServlet extends HttpServlet {
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		var session = request.getSession(false);
		
		if (session != null) {
			String schemaName = (String) session.getAttribute("schemaName");
			if (schemaName != null) {
				try {
					SandboxManager.resetSandbox(schemaName);
					session.setAttribute("flashMessage", "データベースをリセットしました");
					session.setAttribute("flashType", "success");
				} catch (SQLException e) {
					session.setAttribute("flashMessage", "リセットに失敗しました" + e.getMessage());
					session.setAttribute("flashType", "error");
					e.printStackTrace();
				}
			}
		}
		
		response.sendRedirect("/");
	}

}
