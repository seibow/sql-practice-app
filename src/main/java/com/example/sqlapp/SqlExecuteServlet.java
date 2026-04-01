package com.example.sqlapp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/execute")
public class SqlExecuteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	request.setCharacterEncoding("UTF-8");
    	
    	//セッション取得 or 作成
    	HttpSession session = request.getSession(true);
    	String schemaName = (String) session.getAttribute("schemaName");
    	
    	//初回アクセス時にsandbox作成
    	if (schemaName == null) {
    		schemaName = SandboxManager.createSchemaName(session.getId());
    		session.setAttribute("schemaName", schemaName);
    		try {
    			SandboxManager.createSandbox(schemaName);
    		} catch (SQLException e) {
    			request.setAttribute("error", "sandbox作成に失敗しました: " + e.getMessage());
    		}
    	}
    	
    	request.setAttribute("schemaName", schemaName);
    	transferFlashMessage(session, request);
    	request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	request.setCharacterEncoding("UTF-8");
    	
    	//セッションからスキーマ名を取得
    	HttpSession session = request.getSession(false);
    	String schemaName = (session != null) ? (String) session.getAttribute("schemaName") : null;
    	
    	String sql = request.getParameter("sql");
    	
    	request.setAttribute("schemaName", schemaName);
    	request.setAttribute("sql", sql);
    	
    	if (schemaName == null) {
    		request.setAttribute("error", "セッションが切れています");
    		request.getRequestDispatcher("/result.jsp").forward(request, response);
    		return;
    	}
    	
    	String userName = SandboxManager.createUserName(schemaName);
    	
    	try (Connection conn = DatabaseConnection.getSessionConnection(userName);
    		 Statement stmtPath = conn.createStatement()) {
    		stmtPath.execute("SET search_path TO " + schemaName);
    		String sqlUpper = sql.trim().toUpperCase();
    		
    		if (sqlUpper.startsWith("SELECT")  || sqlUpper.startsWith("SHOW")) {
    			executeQuery(conn, sql, request);
    		} else {
    			executeUpdate(conn, sql, request);
    		}
    	} catch (SQLException e) {
    		request.setAttribute("error", e.getMessage());
    	}
    	
    	transferFlashMessage(session, request);
    	request.getRequestDispatcher("/result.jsp").forward(request, response);
    }
    
    private void transferFlashMessage(HttpSession session, HttpServletRequest request) {
    	String flashMessage = (String) session.getAttribute("flashMessage");
    	String flashType = (String) session.getAttribute("flashType");
    	if (flashMessage != null) {
    		request.setAttribute("flashMessage", flashMessage);
    		request.setAttribute("flashType", flashType);
    		session.removeAttribute("flashMessage");
    		session.removeAttribute("flashType");
    	}
    }
     		    
    private void executeQuery(Connection conn, String sql, HttpServletRequest request)
    		throws SQLException {
    	try (Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql)) {
    			ResultSetMetaData meta = rs.getMetaData();
    			int columnCount = meta.getColumnCount();
    		
    			List<String> columns = new ArrayList<>();
   				for (int i = 1; i <= columnCount; i++) {
   					columns.add(meta.getColumnName(i));
   				}
  
   				List<List<String>> results = new ArrayList<>();
   				while (rs.next()) {
   					List<String> row = new ArrayList<>();
   					for (int i = 1; i <= columnCount; i++) {
   						row.add(rs.getString(i));        					
   					}
    				results.add(row);
    			}
    	     request.setAttribute("columns", columns);
    	     request.setAttribute("results", results);
         }
    }
    	
    private void executeUpdate(Connection conn, String sql, HttpServletRequest  request)
            throws SQLException {
    	try (Statement stmt = conn.createStatement()) {
			int affected = stmt.executeUpdate(sql);
			request.setAttribute("affected", affected);
		}
    }
}
