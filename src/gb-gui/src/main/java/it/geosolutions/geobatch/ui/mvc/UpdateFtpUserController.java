/**
 * 
 */
package it.geosolutions.geobatch.ui.mvc;

import it.geosolutions.geobatch.ftpserver.ftp.FtpUser;
import it.geosolutions.geobatch.ftpserver.server.GeoBatchServer;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author giuseppe
 * 
 */
public class UpdateFtpUserController extends AbstractController {

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
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal
     * (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String userId = request.getParameter("userId");

        server.getUserManager().delete(userId);

        List<FtpUser> ftpUsers = server.getUserManager().getAllUsers();

        ModelAndView mav = new ModelAndView("ftpUsers");
        mav.addObject("ftpUsers", ftpUsers);

        return mav;
    }
}
