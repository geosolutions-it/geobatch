/**
 * 
 */
package it.geosolutions.geobatch.ui.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    public Collection<GrantedAuthority> getGrantedAuthorities() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    }
}
