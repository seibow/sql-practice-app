package com.example.sqlapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.ArrayList;

@WebServlet("/execute")
public class SqlExecuteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	request.setCharacterEncoding("UTF-8");
    	
    	//セッション取得 or 作成
    	var session = request.getSession(true);
    	String schemaName = (String) session.getAttribute("schemaName");
    	
    	//初回アクセス時にsandbox作成
    	if (schemaName == null) {
    		schemaName = SandboxManager.createSchemaName(session.getId());
    		session.setAttribute("schemaName", schemaName);
    		try {
    			SandboxManager.createSandbox(schemaName);
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	request.setAttribute("schemaName", schemaName);
    	String flashMessage = (String) session.getAttribute("flashMessage");
    	String flashType = (String) session.getAttribute("flashType");
    	if (flashMessage != null) {
    		request.setAttribute("flashMessage", flashMessage);
    		request.setAttribute("flashType", flashType);
    		session.removeAttribute("flashMessage");
    		session.removeAttribute("flashType");
    	}
    	request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	request.setCharacterEncoding("UTF-8");
    	
    	//セッションからスキーマ名を取得
    	var session = request.getSession(false);
    	String schemaName = (session != null) ? (String) session.getAttribute("schemaName") : null;
    	
    	String sql = request.getParameter("sql");
    	
    	request.setAttribute("schemaName", schemaName);
    	request.setAttribute("sql", sql);
    	
    	try {
    		Class.forName("org.postgresql.Driver");
    	} catch (ClassNotFoundException e) {
    		request.setAttribute("error", "ドライバ読み込み失敗: " + e.getMessage());
    		request.getRequestDispatcher("/result.jsp").forward(request, response);
    		return;
    	}
    	
    	if (schemaName == null) {
    		request.setAttribute("error", "セッションが切れています");
    		request.getRequestDispatcher("/result.jsp").forward(request, response);
    		return;
    	}
    	
    	String userName = SandboxManager.createUserName(schemaName);
    	try (Connection conn = DatabaseConnection.getSessionConnection(userName);
    		Statement stmtPath = conn.createStatement()) {
    		
    		//search_pathをセッションのスキーマに設定
    		stmtPath.execute("SET search_path TO " + schemaName);
    		
    		String sqlUpper = sql.trim().toUpperCase();
    		
    		if (sqlUpper.startsWith("SELECT") || sqlUpper.startsWith("SHOW")) {
    			try (Statement stmt = conn.createStatement();
    				ResultSet rs = stmt.executeQuery(sql)) {
    				ResultSetMetaData meta = rs.getMetaData();
    				int columnCount = meta.getColumnCount();
    		
    				List<String> columns = new java.util.ArrayList<>();
    				for (int i = 1; i <= columnCount; i++) {
    					columns.add(meta.getColumnName(i));
    				}
  
    				List<List<String>> results = new java.util.ArrayList<>();
    				while (rs.next()) {
    					List<String> row = new java.util.ArrayList<>();
    					for (int i = 1; i <= columnCount; i++) {
    						row.add(rs.getString(i));
    					}
    					results.add(row);
    				}
    		    request.setAttribute("columns", columns);
    		    request.setAttribute("results", results);
    		}
    	} else {
    		try (Statement stmt = conn.createStatement()) {
    			int affected = stmt.executeUpdate(sql);
    			request.setAttribute("affected", affected);
    		}
    	}
    } catch (SQLException e) {
    	request.setAttribute("error", e.getMessage());
    }
    
    String flashMessage = (String) session.getAttribute("flashMessage");
    if (flashMessage != null) {
    	request.setAttribute("flashMessage", flashMessage);
    	session.removeAttribute("flashMessage");
    }
    request.getRequestDispatcher("/result.jsp").forward(request, response);
    }
}
