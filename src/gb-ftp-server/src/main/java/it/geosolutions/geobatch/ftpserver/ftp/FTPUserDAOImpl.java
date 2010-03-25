/*
 */

package it.geosolutions.geobatch.ftpserver.ftp;

import it.geosolutions.geobatch.ftpserver.dao.FtpPropsDAO;
import it.geosolutions.geobatch.ftpserver.dao.hibernate.HibFtpPropsDAO;
import it.geosolutions.geobatch.ftpserver.model.FtpProps;
import it.geosolutions.geobatch.users.dao.DAOException;
import it.geosolutions.geobatch.users.dao.GBUserDAO;
import it.geosolutions.geobatch.users.model.GBUser;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Merge the results from the GBUsersDAO and HibFtpPropsDAO
 *
 * <P>TODO: todo: make sure commits and rollbacks are really transactionally distributed
 *
 * @author etj
 */
@Transactional(propagation = Propagation.REQUIRED)
public class FTPUserDAOImpl implements FtpUserDAO {

	private GBUserDAO userDAO;
	private FtpPropsDAO propsDAO;


	public boolean existsUser(Long id) throws DAOException {
		return userDAO.findById(id, false) != null;
	}

	public boolean existsUser(String name) throws DAOException {
		return userDAO.findByUserName(name) != null;
	}

	public FtpUser findByUserName(String userName) throws DAOException {
		GBUser user = userDAO.findByUserName(userName);
		if(user != null) {
			FtpProps props = propsDAO.findById(user.getId(), false);
			if(props == null)
				props = new FtpProps(user.getId()); // take default values
			return new FtpUser(user, props);
		}
		else
			return null;
	}

	public List<FtpUser> findAll() throws DAOException {
		List<GBUser> gbUsers = userDAO.findAll();
		List<FtpUser> ftpUsers = new ArrayList<FtpUser>();
		for(GBUser gbUser : gbUsers) {
			FtpProps props = propsDAO.findById(gbUser.getId(), false);
			if(props == null)
				props = new FtpProps(gbUser.getId()); // take default values
			ftpUsers.add(new FtpUser(gbUser, props));
		}
		return ftpUsers;
	}

	public FtpUser save(FtpUser user) throws DAOException {
		user.setSourceUser(userDAO.save(user.getSourceUser()));
		user.getSourceFtpProps().setId(user.getId());
		user.setSourceFtpProps(propsDAO.save(user.getSourceFtpProps()));
		return user;
	}

	public void delete(Long userId) throws DAOException {
		propsDAO.delete(userId);
		userDAO.delete(userId);
	}



	
	public void setFtpPropsDAO(FtpPropsDAO ftpPropsDAO) {
		this.propsDAO = ftpPropsDAO;
	}

	public void setUserDAO(GBUserDAO userDAO) {
		this.userDAO = userDAO;
	}

}
