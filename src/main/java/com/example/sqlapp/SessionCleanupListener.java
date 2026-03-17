package com.example.sqlapp;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class SessionCleanupListener implements HttpSessionListener {

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		var session = event.getSession();
		String schemaName = (String) session.getAttribute("schemaName");
		
		if (schemaName != null) {
			try {
				SandboxManager.dropSandbox(schemaName);
				System.out.println("セッション終了によりsandbox削除: " + schemaName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
