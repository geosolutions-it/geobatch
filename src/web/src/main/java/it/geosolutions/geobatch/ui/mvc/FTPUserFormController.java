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
import it.geosolutions.geobatch.ui.mvc.data.FtpUserDataBean;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ftpserver.impl.DefaultFtpServer;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * @author giuseppe
 * 
 */
public class FTPUserFormController extends SimpleFormController {

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
	 * org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject
	 * (javax.servlet .http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		FtpUserDataBean backingObject = new FtpUserDataBean();
		// this.setValidator(new FtpUserFormValidator(getApplicationContext()));
		logger.info("Returning backing object");
		return backingObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(java
	 * .lang.Object, org.springframework.validation.BindException)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		FtpUserDataBean givenData = (FtpUserDataBean) command;

//		GeoBatchServer server = (GeoBatchServer) getApplicationContext()
//				.getBean("geoBatchServer");

		logger.info(givenData.toString());

		FtpUser user = new FtpUser();
		user.setUserId(givenData.getUserId());
		user.setUserPassword(givenData.getPassword());
		user.setWritePermission(givenData.getWritePermission());
		if (!givenData.getUploadRate().equals(""))
			user.setUploadRate(Integer.parseInt(givenData.getUploadRate()));
		if (!givenData.getDownloadRate().equals(""))
			user.setDownloadRate(Integer.parseInt(givenData.getDownloadRate()));

		((GeoBatchUserManager) ((DefaultFtpServer) server.getFtpServer())
				.getUserManager()).save(user);

		List<FtpUser> ftpUsers = (List<FtpUser>) request.getSession().getAttribute("ftpUsers");
		ftpUsers.add(user);
		request.getSession().setAttribute("ftpUsers", ftpUsers);
		logger.info("Form data successfully submitted");
		return new ModelAndView(getSuccessView());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.BaseCommandController#onBindAndValidate
	 * (javax.servlet.http.HttpServletRequest, java.lang.Object,
	 * org.springframework.validation.BindException)
	 */
	@Override
	protected void onBindAndValidate(HttpServletRequest request,
			Object command, BindException errors) throws Exception {
//		GeoBatchServer server = (GeoBatchServer) getApplicationContext()
//				.getBean("geoBatchServer");

		boolean present = false;
		FtpUserDataBean givenData = (FtpUserDataBean) command;
		if (givenData == null) {
			errors.reject("error.nullpointer", "Null data received");
		} else {
			/* VALIDATE ALL FIELDS */
			if ((givenData.getUserId() == null)
					|| (givenData.getUserId().trim().length() <= 0)) {
				errors.rejectValue("userId", "error.code",
						"Ftp User Id is mandatory.");
			} else {
				if (((GeoBatchUserManager) ((DefaultFtpServer) server
						.getFtpServer()).getUserManager()).checkUser(givenData
						.getUserId())) {
					present = true;
					errors.rejectValue("userId", "error.code", "Ftp User "
							+ givenData.getUserId() + " has already entered.");
				}
			}

			if (!present) {
				if ((givenData.getPassword() == null)
						|| (givenData.getPassword().trim().length() <= 0)) {
					errors.rejectValue("password", "error.code",
							"Ftp User Password is mandatory.");
				}

				if ((givenData.getRepeatPassword() == null)
						|| (givenData.getRepeatPassword().trim().length() <= 0)) {
					errors.rejectValue("repeatPassword", "error.code",
							"Ftp User Repeat Password is mandatory.");
				}

				if ((!givenData.getPassword().equals(""))
						&& (!givenData.getRepeatPassword().equals(""))) {
					if (!givenData.getPassword().equals(
							givenData.getRepeatPassword())) {
						errors.rejectValue("password", "error.code",
								"The password must be the same.");
					}

				}

				if (!givenData.getDownloadRate().equals("")) {
					try {
						int downloadRate = Integer.parseInt(givenData
								.getDownloadRate());
						if (downloadRate < 0) {
							errors
									.rejectValue("downloadRate", "error.code",
											"Ftp User Download Rate must be greater than 0.");
						}
					} catch (NumberFormatException e) {
						errors.rejectValue("downloadRate", "error.code",
								"Ftp User Download Rate must be an integer.");
					}
				}

				if (!givenData.getUploadRate().equals("")) {
					try {
						int uploadRate = Integer.parseInt(givenData
								.getUploadRate());
						if (uploadRate < 0) {
							errors
									.rejectValue("uploadRate", "error.code",
											"Ftp User Upload Rate must be greater than 0.");
						}
					} catch (NumberFormatException e) {
						errors.rejectValue("uploadRate", "error.code",
								"Ftp User Upload Rate must be an integer.");
					}
				}
			}
		}
	}
}
