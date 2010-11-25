package it.geosolutions.geobatch.action.egeos.emsa.features;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Parses a gml:DirectPosition2D into a Point
 * 
 * @author Andrea Aime
 */
public class DirectPositionMapping extends FeatureMapping {

    public DirectPositionMapping(String xpath, String destName) {
        super(xpath, destName);
    }

    public DirectPositionMapping(String name) {
        super(name);
    }

    @Override
    public Object getValue(XPath xpath, Node root) throws XPathExpressionException {
        String value = (String) super.getValue(xpath, root);
        if (value == null || "".equals(value.trim())) {
            return null;
        }

        String[] xy = value.split("\\s+");
        return new GeometryFactory().createPoint(new Coordinate(Double.parseDouble(xy[0]), Double
                .parseDouble(xy[1])));
    }
}
