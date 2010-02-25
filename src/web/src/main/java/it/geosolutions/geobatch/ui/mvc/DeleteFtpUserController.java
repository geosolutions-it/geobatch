/**
 * 
 */
package it.geosolutions.geobatch.ui.mvc;

import it.geosolutions.geobatch.ftp.server.GeoBatchServer;
import it.geosolutions.geobatch.ftp.server.GeoBatchUserManager;
import it.geosolutions.geobatch.ftp.server.model.FtpUser;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ftpserver.impl.DefaultFtpServer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author giuseppe
 * 
 */
public class DeleteFtpUserController extends AbstractController {

	private GeoBatchServer server;

	/**
	 * @param server
	 *            the server to set
	 */
	public void setServer(GeoBatchServer server) {
		this.server = server;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal
	 * (javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String userId = request.getParameter("userId");

		((GeoBatchUserManager) ((DefaultFtpServer) server.getFtpServer())
				.getUserManager()).delete(userId);

		List<FtpUser> ftpUsers = ((GeoBatchUserManager) ((DefaultFtpServer) server
				.getFtpServer()).getUserManager()).getAllUsers();

		request.getSession().setAttribute("ftpUsers", ftpUsers);

		ModelAndView mav = new ModelAndView("ftp");
		mav.addObject("ftpUsers", ftpUsers);

		return mav;
	}
}
