package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;


public class ImageMosaicPropertiesTest {

	@Test
	public void getPropertiesTest() throws UnsatisfiedLinkError, FileNotFoundException{
		try{
			ImageMosaicProperties.getProperty(new File("datastore.properties"));
		}catch (FileNotFoundException e){
			Assert.assertTrue(Boolean.TRUE);
		} catch (NullPointerException e) {
			Assert.assertTrue(Boolean.FALSE);
		} catch (IOException e) {
			Assert.assertTrue(Boolean.FALSE);
		}
		
		final Properties props;
		try {
			props = ImageMosaicProperties.getProperty(new File("src/main/resources/data/imagemosaic_work/config/datastore.properties"));
			final String schema=props.getProperty("schema");
			Assert.assertNotNull(schema);
		} catch (NullPointerException e) {
			Assert.assertTrue(Boolean.FALSE);
		} catch (IOException e) {
			Assert.assertTrue(Boolean.FALSE);
		}
	}
}
