package nl.tue.bpmgame.bpmgamegui.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Remembers the username when the user does a failed log in. This way, the user does not have to enter the username again. 
 *
 */
public class UserNameCachingAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	public static final String LAST_USERNAME_KEY = "LAST_USERNAME";

	@SuppressWarnings("unused")
	@Autowired
	private UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

		super.onAuthenticationFailure(request, response, exception);

		String lastUserName = request.getParameter("email");

		HttpSession session = request.getSession(false);
		if (session != null || isAllowSessionCreation()) {
			request.getSession().setAttribute(LAST_USERNAME_KEY, lastUserName);
		}
	}
}