/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
public class FTPManagerController extends AbstractController {

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
	 * (javax.servlet .http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// FtpUserDAO ftpUserDAO = (FtpUserDAO) getApplicationContext().getBean(
		// "ftpUserDAO");

		ModelAndView mav = new ModelAndView("ftp");
		List<FtpUser> ftpUsers = ((GeoBatchUserManager) ((DefaultFtpServer) server
				.getFtpServer()).getUserManager()).getAllUsers();// ftpUserDAO.findAll();
		mav.addObject("ftpUsers", ftpUsers);

		request.getSession().setAttribute("ftpUsers", ftpUsers);
		return mav;
	}
}
