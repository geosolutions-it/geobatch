/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.actions.ds2ds.geoserver;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.ds2ds.DsBaseAction;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.datastore.GSOracleNGDatastoreEncoder;
import it.geosolutions.geoserver.rest.encoder.datastore.GSPostGISDatastoreEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.type.GeometryDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;


/**
 * A GB action to publish DB layers in GeoServer, taking as event input the xml
 * output of the Ds2dsAction. The action also allows to create GeoServer
 * workspace and configure datastore if they do not exist yet.
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 */
public class DSGeoServerAction extends DsBaseAction {

	protected final static Logger LOGGER = LoggerFactory
			.getLogger(DSGeoServerAction.class);
	
	private static final String acceptedFileType = "xml";
	
	final DSGeoServerConfiguration conf;
	
	/**
	 * Constructs the DSGeoServerAction
	 * 
	 * @param actionConfiguration
	 */
	public DSGeoServerAction(DSGeoServerConfiguration actionConfiguration) {
		super(actionConfiguration);
		this.conf = actionConfiguration;
	}

	@Override
	public Queue<EventObject> execute(Queue<EventObject> events)
			throws ActionException {

		listenerForwarder.started();
		
		//check global configurations
		//Geoserver config
		//----------------
		updateTask("Check GeoServer configuration");

		final String url = conf.getGeoserverURL();
		final String user = conf.getGeoserverUID();
		final String password = conf.getGeoserverPWD();		
		GeoServerRESTManager gsMan = null;
		try {
			gsMan = new GeoServerRESTManager(new URL(url), user, password);				
		} catch (MalformedURLException e) {
			failAction("Wrong GeoServer URL");
						
		} catch(IllegalArgumentException e){
			failAction("Unable to create the GeoServer Manager using a null argument");

		}
		//TODO how to check if GS user/password are correct?
		listenerForwarder.progressing(5,"GeoServer configuration checked");
		
		//Check operation
		//---------------
		updateTask("Check operation");
		String op = conf.getOperation();
		if(op == null || !(op.equalsIgnoreCase("PUBLISH") || op.equalsIgnoreCase("REMOVE"))){
			failAction("Bad operation: " + op + " in configuration");
		}
		listenerForwarder.progressing(10,"Operation checked");
		
		//Check WorkSpace
		//---------------
		updateTask("Check workspace configuration");
		String ws = conf.getDefaultNamespace();
		String wsUri = conf.getDefaultNamespaceUri();
		
		Boolean existWS = false;
		synchronized(existWS){
			existWS = gsMan.getReader().getWorkspaceNames().contains(ws);
			
			if(!existWS){
				
				boolean createWS = conf.getCreateNameSpace();
				if(createWS){
					//try to create the workspace
					updateTask("Create workspace "+ws+" in GeoServer");
					boolean created = false;
					if(wsUri == null){
						created = gsMan.getPublisher().createWorkspace(ws);
					}else{
						try {
							created = gsMan.getPublisher().createWorkspace(ws, new URI(wsUri));
						} catch (URISyntaxException e) {
							failAction("Invalid NameSpace URI "+wsUri+" in configuration");
						}
					}	 
					if(!created){
						failAction("FATAL: unable to create workspace "+ws+" in GeoServer");
					}
				}else{
					failAction("Bad workspace (namespace): "+ ws +" in configuration");
				}
			}
		}
		
		listenerForwarder.progressing(25,"GeoServer workspace checked");
		
		//event-based business logic
		while (events.size() > 0) {
			final EventObject ev;
			try {
				if ((ev = events.remove()) != null) {

					updateTask("Working on incoming event: " + ev.getSource());
					
					updateTask("Check acceptable file");
					FileSystemEvent fileEvent = (FileSystemEvent) ev;

					//set FeatureConfiguration
					updateTask("Set Feature Configuration");
					this.createFeatureConfiguration(fileEvent);
					FeatureConfiguration featureConfig = conf.getFeatureConfiguration();
						
					//TODO check FeatureConfiguration
					updateTask("Check Feature Configuration");
					if(featureConfig.getTypeName() == null){
						failAction("feature typeName cannot be null");
					}
							
					//TODO check if the typeName already exists for the target workspace?
						
					//datastore check (and eventually creation)
					updateTask("Check datastore configuration");
					String ds = conf.getStoreName();
							
					Boolean existDS = false;
					synchronized(existDS){
								
						existDS = gsMan.getReader().getDatastores(ws).getNames().contains(ds);
						if(!existDS){	
							boolean createDS = conf.getCreateDataStore();
							if(createDS){
										
								//create datastore
								updateTask("Create datastore in GeoServer");
								Map<String,Object> datastore = this.deserialize(featureConfig.getDataStore());							

								   String dbType = (String) datastore.get("dbtype");
								        
								boolean created = false;
								if(dbType.equalsIgnoreCase("postgis")){
									GSPostGISDatastoreEncoder encoder = new GSPostGISDatastoreEncoder(ds);
									encoder.setName(ds);
									encoder.setEnabled(true);
									encoder.setHost((String) datastore.get("host"));
									encoder.setPort(Integer.parseInt((String) datastore.get("port")));
									encoder.setDatabase((String) datastore.get("database"));
									encoder.setSchema((String) datastore.get("schema"));
									encoder.setUser((String) datastore.get("user"));
									encoder.setPassword((String) datastore.get("passwd"));

									created = gsMan.getStoreManager().create(ws, encoder);
									if(!created){
										failAction("FATAL: unable to create PostGIS datastore "+ds+" in GeoServer");
									}
											
								}else if(dbType.equalsIgnoreCase("oracle")){
									String dbname = (String) datastore.get("database");
									GSOracleNGDatastoreEncoder encoder = new GSOracleNGDatastoreEncoder(ds, dbname);
									encoder.setName(ds);
									encoder.setEnabled(true);
									encoder.setHost((String) datastore.get("host"));
									encoder.setPort(Integer.parseInt((String) datastore.get("port")));
									encoder.setDatabase(dbname);
									encoder.setSchema((String) datastore.get("schema"));
									encoder.setUser((String) datastore.get("user"));
									encoder.setPassword((String) datastore.get("passwd"));

									created = gsMan.getStoreManager().create(ws, encoder);
									if(!created){
										failAction("FATAL: unable to create Oracle NG datastore "+ds+" in GeoServer");
									}
								}else{
									failAction("The datastore type "+dbType+" is not supported");
								}

							}else{
								failAction("Bad datastore:"+ ds +" in configuration. Datastore "+ds+" doesn't exist in workspace (namespace) "+ws);
							}
						}
					}
					listenerForwarder.progressing(50,"Check GeoServer datastore");
							
							
					//feature type publication/removal
					boolean done = false;
					if(op.equalsIgnoreCase("PUBLISH")){
						if(!gsMan.getReader().getLayers().getNames().contains(featureConfig.getTypeName())){
									
							updateTask("Publish DBLayer "+featureConfig.getTypeName()+" in GeoServer");
									
							//featuretype
							final GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
							fte.setName(featureConfig.getTypeName());
							fte.setTitle(featureConfig.getTypeName());
							String crs = featureConfig.getCrs();
							if(crs != null){
								fte.setSRS(featureConfig.getCrs());
							}else{
								fte.setSRS("EPSG:4326");
							}
							fte.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
							        
							//layer & default style
							final GSLayerEncoder layerEncoder = new GSLayerEncoder();
							layerEncoder.setDefaultStyle(this.defineLayerStyle(featureConfig, gsMan));
									
							//publish
							done = gsMan.getPublisher().publishDBLayer(ws, ds, fte, layerEncoder);
							if(!done){
								failAction("Impossible to publish DBLayer "+featureConfig.getTypeName()+" in GeoServer");
							}
						}
										
					}else if(op.equalsIgnoreCase("REMOVE")){
						if(gsMan.getReader().getLayers().getNames().contains(featureConfig.getTypeName())){
									
							//remove
							updateTask("Remove DBLayer "+featureConfig.getTypeName()+" from GeoServer");
									
							done = gsMan.getPublisher().unpublishFeatureType(ws, ds, featureConfig.getTypeName());
							if(!done){
								failAction("Impossible to remove DBLayer "+featureConfig.getTypeName()+" in GeoServer");
							}
						}
					}
						
					listenerForwarder.progressing(100F, "Successful Geoserver "+op+" operation");
					listenerForwarder.completed();

				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Encountered a NULL event: SKIPPING...");
					}
					continue;
				}
			} catch (Exception ioe) {
				failAction("Unable to produce the output: "
						+ ioe.getLocalizedMessage());
			}
		}
		return events;

	}
	
	/**
	 * Creates the source DataStore from the given input file event.
	 * 
	 * @param fileEvent
	 * @return
	 * @throws IOException
	 * @throws ActionException 
	 */
	private void createFeatureConfiguration(FileSystemEvent fileEvent) throws IOException, ActionException {
			
		String fileType =  FilenameUtils.getExtension(fileEvent.getSource().getName()).toLowerCase();		
		FeatureConfiguration featureConfig = conf.getFeatureConfiguration();
		if(fileType.equals(acceptedFileType)){
			InputStream inputXML = null;
			try {
				inputXML = new FileInputStream(fileEvent.getSource());				
				featureConfig  = FeatureConfiguration.fromXML(inputXML);							
			} catch (Exception e) {
	            throw new IOException("Unable to load input XML", e);
	        } finally {
	            IOUtils.closeQuietly(inputXML);
	        }
		}else{
			failAction("Bad input file extension: "+fileEvent.getSource().getName()+". Input must be an XML file");
	
		}		
		conf.setFeatureConfiguration(featureConfig);
	}

	/**
	 * Get the geometry type binding of the given layer
	 * 
	 * @param featureConfig
	 * @throws ActionException
	 */
	private Class<?> getGeometryTypeBinding(FeatureConfiguration featureConfig)
			throws ActionException {
		DataStore datastore = null;
		Class<?> binding = null;

		try {
			datastore = DataStoreFinder.getDataStore(featureConfig
					.getDataStore());
			SimpleFeatureSource sfs = datastore.getFeatureSource(featureConfig
					.getTypeName());
			GeometryDescriptor geomDescriptor = sfs.getSchema().getGeometryDescriptor();
			if(geomDescriptor != null){
				binding = geomDescriptor.getType().getBinding();
			}
		} catch (IOException ioe) {
			failAction(ioe.getMessage());

		} finally {
			datastore.dispose();
		}

		return binding;
	}
	
	/**
	 * Defines the style to set for the GeoServer layer. The method:
	 * - first check for style availability in the configuration
	 * - else it tries to assign a default style by checking the geometry type binding from the feature
	 * configuration, checking the availability of Geoserver default styles, and eventually publish them
	 * 
	 * NOTE: this method was put in place, because in geoserver-manager, while no style assignment
	 * makes GeoServer choosing the appropriate default style (polygon, style or point), it can result
	 * in an GeoServer error when trying to access the Layer Publication panel.
	 * 
	 * 
	 * @param featureConfig
	 * @param gsMan (instance of GeoserverRESTManager)
	 * @return
	 * @throws ActionException 
	 * @throws IOException 
	 */
	private String defineLayerStyle(FeatureConfiguration featureConfig, GeoServerRESTManager gsMan) throws ActionException, IOException{
		updateTask("Define the layer default style");
		
		String defaultStyle = conf.getDefaultStyle();
		List<String> styles = gsMan.getReader().getStyles().getNames();
		if (defaultStyle != null) {
			updateTask("Use default style from configuration");
			if (!styles.contains(defaultStyle)) {
				failAction("Style " + defaultStyle + " doesn't exist in GeoServer");
			}
		} else {
			Class<?> geomBinding = this.getGeometryTypeBinding(featureConfig);
			if(geomBinding != null){
				if(geomBinding.equals(Polygon.class) || geomBinding.equals(MultiPolygon.class)){
					defaultStyle = "polygon";
				}else if(geomBinding.equals(LineString.class) || geomBinding.equals(MultiLineString.class)){
				defaultStyle = "line";
				}else{
					defaultStyle = "point";
				}
			}else{
				defaultStyle = "point";
			}
		}
		
		return defaultStyle;
	}
	
	/**
	 * Deserializes datastore params. This method is used prior to the datastore
	 * configuration using the geoserver-manager library
	 * 
	 * @param datastore
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Map<String,Object> deserialize(Map<String,Serializable> datastore) throws IOException, ClassNotFoundException{
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        objOut.writeObject(datastore);
        objOut.close();
        ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        Map<String,Object> connect = (Map<String, Object>) objIn.readObject();
        return connect;
	}


}
