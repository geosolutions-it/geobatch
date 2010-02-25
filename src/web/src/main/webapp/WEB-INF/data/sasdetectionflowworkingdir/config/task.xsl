<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tsk="http://www.geo-solutions.it/tsk">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:param name="shapeGeneratorScript"/>
    <xsl:param name="inputFile"/>
    <xsl:param name="outputDir"/>
    <xsl:param name="crsDefinitionsDir"/>

    <xsl:template match="shapeGeneratorScript">
        <xsl:value-of select="concat('', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="inputDir">
        <xsl:value-of select="concat(' -i ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="outputDir">
        <xsl:value-of select="concat(' -o ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="crsDefinitionsDir">
        <xsl:value-of select="concat(' --crsdir ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="loggingDir">
        <xsl:value-of select="concat(' --logdir ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
</xsl:stylesheet>