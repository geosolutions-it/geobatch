/**
 * 
 */
package it.geosolutions.geobatch.users;



import java.io.File;

import junit.framework.Assert;

import org.geotools.TestData;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class CustomPlaceholderTest extends Assert{
	
	private ClassPathXmlApplicationContext context;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("GEOBATCH_CONFIG_DIR", TestData.file(CustomPlaceholderTest.class,"config").getAbsolutePath());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.context = new ClassPathXmlApplicationContext("applicationContext-test.xml");
	}

	/**
	 * Checks that the db has been placed at the right place.
	 * 
	 * @throws Exception
	 */
	@Test
	public void basicTest() throws Exception{
		
		Object bean = this.context.getBean("dataDirHandler");
		assertNotNull(bean);			
		Object bean1 = this.context.getBean("dataSource-gb-users");
		assertNotNull(bean1);
		bean = this.context.getBean("placeholderProperties-database");
		assertNotNull(bean);
		
		CustomPropertyOverride cpo= (CustomPropertyOverride) bean;
		assertNotNull(cpo);

		com.mchange.v2.c3p0.ComboPooledDataSource ds= (ComboPooledDataSource) bean1;
		ds.getConnection().close();
		assertEquals("jdbc:h2:"+TestData.file(this,"config").getAbsolutePath()+CustomPropertyOverride.SETTINGS_GBUSERS,ds.getJdbcUrl());
		assertTrue(new File(ds.getJdbcUrl().substring("jdbc:h2:".length())+".h2.db").exists());
	}

}
