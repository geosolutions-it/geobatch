/**
 * 
 */
package it.geosolutions.geobatch.ui.security;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/**
 * @author Administrator
 * 
 */
public class CurrentUser {
	public Object getUser() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public String getUserName() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public GrantedAuthority[] getGrantedAuthorities() {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	}
}
