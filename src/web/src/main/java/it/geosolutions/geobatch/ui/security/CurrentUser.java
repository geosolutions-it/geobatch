/**
 * 
 */
package it.geosolutions.geobatch.ui.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * @author Administrator
 * 
 */
public class CurrentUser {
	public User getUser() {
		return (User) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}
}
