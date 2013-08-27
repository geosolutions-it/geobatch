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

package it.geosolutions.geobatch.ui.mvc.data;

/**
 * @author Alessio Fabiani
 *
 */
public class FlowManagerDataBean
{
    private String descriptorId;

    private String id;

    private String name;

    private String inputDir;

    private String outputDir;

    /**
     * @return the descriptorId
     */
    public String getDescriptorId()
    {
        return descriptorId;
    }

    /**
     * @param descriptorId
     *            the descriptorId to set
     */
    public void setDescriptorId(String descriptorId)
    {
        this.descriptorId = descriptorId;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the inputDir
     */
    public String getInputDir()
    {
        return inputDir;
    }

    /**
     * @param inputDir
     *            the inputDir to set
     */
    public void setInputDir(String inputDir)
    {
        this.inputDir = inputDir;
    }

    /**
     * @return the workingDirectory
     */
    public String getOutputDir()
    {
        return outputDir;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }
}
