package com.example.sqlapp;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import jakarta.servlet.http.HttpSession;

@WebListener
public class SessionCleanupListener implements HttpSessionListener {

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		String schemaName = (String) session.getAttribute("schemaName");
		
		if (schemaName == null) {
			return;
		}
		
		try {
			SandboxManager.dropSandbox(schemaName);
			System.out.println("セッション終了によりsandbox削除: " + schemaName);
		} catch (Exception e) {
			System.err.println("sandbox削除失敗: " + schemaName + " / " + e.getMessage());
		}
	}
}
