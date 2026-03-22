package com.example.sqlapp;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SandboxManager {
	public static String createSchmaName(String sessionId) {
		return "sandbox_" + sessionId.substring(0, 8).toLowerCase();
	}
	
	public static String createUserName(String schemaName) {
		return "user_" + schemaName.substring(8);
	}
	
	//専用スキーマを作成してpublicからコピー
	public static void createSandbox(String schemaName) throws SQLException {
		String userName = createUserName(schemaName);
		
		try {
    		Class.forName("org.postgresql.Driver");
    	} catch (ClassNotFoundException e) {
    		throw new SQLException("ドライバ読み込み失敗: ", e);
    	}
		
		try (Connection conn = DatabaseConnection.getAdminConnection();
			Statement stmt = conn.createStatement()) {
			
			//スキーマ作成
			stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
			
			//テーブル構造とデータをコピー
			String[] tables = {"customers", "products", "orders", "order_items"};
			for (String table : tables) {
				stmt.execute("CREATE TABLE " + schemaName + "." + table + " (LIKE public." + table + " INCLUDING ALL)");
				stmt.execute("INSERT INTO " + schemaName + "." + table + " SELECT * FROM public." + table);
			}
			
			// セッション専用ユーザー作成
			String sessionPassword = System.getenv("SESSION_PASSWORD") != null ? System.getenv("SESSION_PASSWORD") : "session_pass";
			stmt.execute("CREATE USER " + userName + "WITH PASSWORD '" + sessionPassword + "'");
			
			//作成したスキーマにsandbox_userの権限付与
			stmt.execute("GRANT USAGE ON SCHEMA " + schemaName + " TO " + userName);
			stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA " + schemaName + " TO " + userName);
			stmt.execute("GRANT USAGE ON ALL SEQUENCES IN SCHEMA " + schemaName + " TO " + userName);
			stmt.execute("GRANT CREATE ON SCHEMA " + schemaName + " TO " + userName);
			
			
			System.out.println("sandbox作成完了: " + schemaName + " / " + userName);
		}
	}
	
	//削除
	public static void dropSandbox(String schemaName) throws SQLException {
		String userName = createUserName(schemaName);
		try (Connection conn = DatabaseConnection.getAdminConnection();
			Statement stmt = conn.createStatement()) {
			stmt.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
			stmt.execute("DROP USER IF EXISTS " + userName);
			System.out.println("sandbox削除完了: " + schemaName + " / " + userName);
		}
	}
	
	//リセット
	public static void resetSandbox(String schemaName) throws SQLException {
		dropSandbox(schemaName);
		createSandbox(schemaName);
		System.out.println("sandboxリセット完了: " + schemaName);
	}
}
