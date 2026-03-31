package com.example.sqlapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private static final String URL = System.getenv("DB_URL");
	private static final String USER = System.getenv("DB_USER");
	private static final String PASSWORD = System.getenv("DB_PASSWORD");
	private static final String ADMIN_USER = System.getenv("DB_ADMIN_USER");
	private static final String ADMIN_PASSWORD = System.getenv("DB_ADMIN_PASSWORD");
	private static final String SESSION_PASSWORD = System.getenv("SESSION_PASSWORD") != null ? System.getenv("SESSION_PASSWORD") : "session_pass";
	
	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError("PostgreSQL ドライバ読み込み失敗: " + e.getMessage());
		}
	}
	
	private DatabaseConnection () {}
	
	private static Connection connect(String user, String password) throws SQLException {
		return DriverManager.getConnection(URL, user, password);
	}
	
	public static Connection getConnection() throws SQLException {
		return connect(USER, PASSWORD);
	}
	
	public static Connection getAdminConnection() throws SQLException {
		return connect(ADMIN_USER, ADMIN_PASSWORD);
	}
	
	public static Connection getSessionConnection(String userName) throws SQLException {
		return connect(userName, SESSION_PASSWORD); 
	}
	
	public static String getSessionPassword() {
		return SESSION_PASSWORD;
	}
	
	public static void main(String[] args) {
		try(Connection conn = getConnection()) {
			System.out.println("接続成功！");
			System.out.println("DB: " + conn.getMetaData().getDatabaseProductName());
			System.out.println("VERSION: " + conn.getMetaData().getDatabaseProductVersion());
		} catch (SQLException e) {
			System.out.println("接続失敗: " + e.getMessage());
		}
	}
	
	

}
