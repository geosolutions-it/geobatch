package it.geosolutions.geobatch.action.egeos.emsa.features;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProParser {

    /**
     * 
     * Parse an xml:
     * 
     * <?xml version="1.0" encoding="UTF-8"?> <sat:image
     * xmlns:sat="http://cweb.ksat.no/cweb/schema/satellite" xmlns="http://www.w3.org/1999/xlink"
     * xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:schemaLocation=
     * "http://cweb.ksat.no/cweb/schema/satellite http://cweb.ksat.no/cweb/schema/satellite/img.xsd"
     * > 
     * <sat:imageFileName>RS1_20100707050441.046.SCN8.NEAR.1.00000_geo8.tif</sat:imageFileName>
     * <sat:pixelSpacingX>200</sat:pixelSpacingX> <sat:pixelSpacingY>200</sat:pixelSpacingY>
     * <sat:samplesPerPixel>1</sat:samplesPerPixel> <sat:bitsPerSample>8</sat:bitsPerSample>
     * <sat:sampleType>Unsigned Integer</sat:sampleType>
     * <sat:resamplingMethod>CUBIC</sat:resamplingMethod> <sat:modelTiepoint>
     * <sat:I>0</sat:I><sat:J>0</sat:J><sat:K>0</sat:K>
     * <sat:X>17.67849810</sat:X><sat:Y>63.34124212</sat:Y><sat:Z>0</sat:Z> </sat:modelTiepoint>
     * <sat:modelPixelScale> <sat:ScaleX>0.0037778637</sat:ScaleX>
     * <sat:ScaleY>-0.0017906864</sat:ScaleY> <sat:ScaleZ>0</sat:ScaleZ> </sat:modelPixelScale>
     * <sat:GTModelTypeGeoKey>ModelTypeGeographic</sat:GTModelTypeGeoKey>
     * <sat:GTRasterTypeGeoKey>RasterPixelIsArea</sat:GTRasterTypeGeoKey>
     * <sat:GeographicTypeGeoKey>GCS_WGS_84</sat:GeographicTypeGeoKey>
     * <sat:GeogCitationGeoKey>"WGS 84"</sat:GeogCitationGeoKey>
     * <sat:GeogAngularUnitsGeoKey>Angular_Degree</sat:GeogAngularUnitsGeoKey> <sat:GCS>4326/WGS
     * 84</sat:GCS> <sat:Datum>6326/World Geodetic System 1984</sat:Datum> <sat:Ellipsoid>7030/WGS
     * 84 (6378137.00,6356752.31)</sat:Ellipsoid> <sat:PrimeMeridian>8901/Greenwich (0.000000/ 0d 0'
     * 0.00"E)</sat:PrimeMeridian> <sat:LowerLeft srsName="EPSG:4326"> <gml:pos>59.8960
     * 17.6785</gml:pos> </sat:LowerLeft> <sat:LowerRight srsName="EPSG:4326"> <gml:pos>59.8960
     * 25.4647</gml:pos> </sat:LowerRight> <sat:UpperLeft srsName="EPSG:4326"> <gml:pos>63.3412
     * 17.6785</gml:pos> </sat:UpperLeft> <sat:UpperRight srsName="EPSG:4326"> <gml:pos>63.3412
     * 25.4647</gml:pos> </sat:UpperRight> <sat:Center srsName="EPSG:4326"> <gml:pos>61.6186
     * 21.5716</gml:pos> </sat:Center> <sat:requestID>N/A</sat:requestID>
     *  <sat:source>
     * <sat:productID>RS1_20100707050441.046.SCN8.NEAR.1.00000</sat:productID>
     * <sat:satellite>RADARSAT1</sat:satellite> <sat:sensor>SAR</sat:sensor>
     * <sat:orbit>76584</sat:orbit> <sat:beamMode>SCN-HH</sat:beamMode>
     * <sat:direction>DESCENDING</sat:direction> <sat:resolution>50.0</sat:resolution>
     * <sat:station>ITMA</sat:station> <sat:startTime>2010-07-07T05:04:41.854Z</sat:startTime>
     * <sat:stopTime>2010-07-07T05:05:28.590Z</sat:stopTime> <sat:cornerPoint srsName="EPSG:4326">
     * <gml:pos>62.5987 25.4259</gml:pos> </sat:cornerPoint> <sat:cornerPoint srsName="EPSG:4326">
     * <gml:pos>63.3228 18.8011</gml:pos> </sat:cornerPoint> <sat:cornerPoint srsName="EPSG:4326">
     * <gml:pos>59.9137 23.8023</gml:pos> </sat:cornerPoint> <sat:cornerPoint srsName="EPSG:4326">
     * <gml:pos>60.6023 17.7146</gml:pos> </sat:cornerPoint> <sat:centerPoint srsName="EPSG:4326">
     * <gml:pos>62.9996033 22.1541824</gml:pos> </sat:centerPoint>
     * <sat:acrossTrackIncidenceAngle>0.0</sat:acrossTrackIncidenceAngle>
     * <sat:alongTrackIncidenceAngle>N/A</sat:alongTrackIncidenceAngle>
     * <sat:illuminationAzimuthAngle>N/A</sat:illuminationAzimuthAngle> 
     *  </sat:source> 
     * </sat:image>
     * 
     * returning a file which is the 'sat:imageFileName' file renamed in a filename_date.tiff which
     * will be passed to the image
     * 
     * @param xmlFile
     * @return
     * @throws Exception
     */
    public File parse(File xmlFile) throws Exception {
        File ret = null;
        // parse the document into a dom
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        boolean found = false;
        String fileName = null;
        String time = null;
        NodeList list = doc.getDocumentElement().getChildNodes();
        int size = list.getLength();
        for (int i = 0; i < size; i++) {
            Node proNode = list.item(i);
            if (proNode.getNodeName() == "sat:imageFileName") {
                fileName = proNode.getTextContent();
                System.out.println("FILE:" + proNode.getTextContent());
            } else if (proNode.getNodeName() == "sat:source") {
                NodeList sourceList = proNode.getChildNodes();
                int sourceSize = sourceList.getLength();
                for (int j = 0; j < sourceSize; j++) {
                    Node sourceNode = sourceList.item(j);
                    if (sourceNode.getNodeName() == "sat:startTime") {
                        time = sourceNode.getTextContent();
                        found = true;
                        System.out.println("TIME:" + sourceNode.getTextContent());
                        break;
                    }
                }
                break;
            }
            if (found) {
                StringBuilder file = new StringBuilder();
                ret = new File(xmlFile.getParent() + fileName);
                file.append(xmlFile.getParent()).append(FilenameUtils.getBaseName(fileName))
                        .append("_").append(time).append(".tiff");
                // rename the file
                try {
                    if (!ret.renameTo(new File(file.toString())))
                        return null; // if filed
                } catch (Exception e) {
System.out.println("Exception: " + e.getLocalizedMessage());
                    ret = null;
                }
            }
        }
        return ret;
    }
    
// TODO JUnit tests
    public static void main(String[] args0) throws Exception {
        new ProParser()
                .parse(new File(
                "/home/carlo/work/data/EMSAWorkingDir/out/20110118T084207016UTC/"+
                "569_RS1_20100707050441.046.SCN8.NEAR.1.00000_PRO/RS1_20100707050441.046.SCN8.NEAR.1.00000_geo8.xml"));
    }
}
