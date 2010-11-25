package it.geosolutions.geobatch.action.egeos.emsa.features;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.geometry.jts.LiteCoordinateSequence;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Parses an array of polygons into a {@link MultiPolygon}
 * 
 * @author Andrea Aime
 */
public class PolygonArrayMapping extends PolygonMapping {

    public PolygonArrayMapping(String xpath, String destName) {
        super(xpath, destName);
    }

    public PolygonArrayMapping(String name) {
        super(name);
    }

    @Override
    public Object getValue(XPath xpath, Node root) throws XPathExpressionException {
        GeometryFactory gf = new GeometryFactory();
        NodeList polygons = (NodeList) xpath.evaluate(this.xpath, root, XPathConstants.NODESET);
        if (polygons.getLength() == 0) {
            return null;
        }

        Polygon[] polarr = new Polygon[polygons.getLength()];
        for (int i = 0; i < polygons.getLength(); i++) {
            Node polygon = polygons.item(i);
            String ordinates = (String) xpath.evaluate("gml:exterior/gml:LinearRing/gml:posList", polygon,
                    XPathConstants.STRING);
            double[] doubles = parseRingOrdinates(ordinates);
            LinearRing shell = gf.createLinearRing(new LiteCoordinateSequence(doubles));

            NodeList interiorNodes = (NodeList) xpath.evaluate("gml:interior/gml:LinearRing/gml:posList",
                    polygon, XPathConstants.NODESET);
            LinearRing[] holes = new LinearRing[interiorNodes.getLength()];
            for (int j = 0; j < holes.length; j++) {
                doubles = parseRingOrdinates(interiorNodes.item(j).getTextContent());
                holes[i] = gf.createLinearRing(new LiteCoordinateSequence(doubles));
            }

            polarr[i] = new GeometryFactory().createPolygon(shell, holes);

        }

        return gf.createMultiPolygon(polarr);
    }
}
