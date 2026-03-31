package com.example.sqlapp;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SandboxManager {
	
	private static final String[][] TABLE_SEQUENCES = {
			{"customers", "customer_id"},
			{"products", "product_id"},
			{"orders", "order_id"},
			{"order_items", "item_id"}
	};
	
	public static String createSchemaName(String sessionId) {
		return "sandbox_" + sessionId.substring(0, 8).toLowerCase();
	}
	
	public static String createUserName(String schemaName) {
		return "user_" + schemaName.substring(8);
	}
	
	//専用スキーマを作成してpublicからコピー
	public static void createSandbox(String schemaName) throws SQLException {
		String userName = createUserName(schemaName);
		
		try (Connection conn = DatabaseConnection.getAdminConnection();
			Statement stmt = conn.createStatement()) {
			
			createSchema(stmt, schemaName);
			copyTablesFromPublic(stmt, schemaName);
			createSandboxUser(stmt, userName);
			grantPermissions(stmt, schemaName, userName);
			setupSequences(stmt, schemaName, userName);
			
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


	private static void createSchema(Statement stmt, String schemaName) throws SQLException {
		stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
	}

	private static void copyTablesFromPublic(Statement stmt, String schemaName) throws SQLException {
		for (String[] entry : TABLE_SEQUENCES) {
			String table = entry[0];
			stmt.execute("CREATE TABLE " + schemaName + "." + table + " (LIKE public." + table + " INCLUDING ALL)");
			stmt.execute("INSERT INTO " + schemaName + "." + table + " SELECT * FROM public." + table);
		}
	}

	private static void createSandboxUser(Statement stmt, String userName) throws SQLException {
		String sessionPassword = DatabaseConnection.getSessionPassword();
		stmt.execute("CREATE USER " + userName + " WITH PASSWORD '" + sessionPassword + "'");
	}

	private static void grantPermissions(Statement stmt, String schemaName, String userName) throws SQLException {
		stmt.execute("GRANT USAGE ON SCHEMA " + schemaName + " TO " + userName);
		stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA " + schemaName + " TO " + userName);
		stmt.execute("GRANT USAGE ON ALL SEQUENCES IN SCHEMA " + schemaName + " TO " + userName);
		stmt.execute("GRANT CREATE ON SCHEMA " + schemaName + " TO " + userName);
	}

	private static void setupSequences(Statement stmt, String schemaName, String userName) throws SQLException {
		for (String[] entry : TABLE_SEQUENCES) {
			String table = entry[0];
			String column = entry[1];
			String seqName = table + "_" + column + "_seq";
			
			stmt.execute("CREATE SEQUENCE " + schemaName + "." + seqName);
			stmt.execute("SELECT setval('" + schemaName + "." + seqName + "', (SELECT MAX(" + column + ") FROM " + schemaName + "." + table + "))");
			stmt.execute("ALTER TABLE " + schemaName + "." + table + " ALTER COLUMN " + column + " SET DEFAULT nextval('" + schemaName + "." + seqName + "')");
			stmt.execute("GRANT USAGE ON SEQUENCE " + schemaName + "." + seqName + " TO " + userName);
		}
	}
}

