<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tsk="http://www.geo-solutions.it/tsk">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    
    <xsl:template match="scriptLocation">
        <xsl:value-of select="concat(' ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="of">
        <xsl:value-of select="concat(' -of ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="co">
        <xsl:value-of select="concat(' -co ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="ot">
        <xsl:value-of select="concat(' -ot ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="ps">
        <xsl:value-of select="concat(' -ps ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="levels">
        <xsl:value-of select="concat(' -levels ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="v">
        <xsl:value-of select="concat(' -v')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="pyramidOnly">
        <xsl:value-of select="concat(' -pyramidOnly')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="r">
        <xsl:value-of select="concat(' -r ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="s_srs">
        <xsl:value-of select="concat(' -s_srs ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="tileIndex">
        <xsl:value-of select="concat(' -tileIndex ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="tileIndexField">
        <xsl:value-of select="concat(' -tileIndexField ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="csv">
        <xsl:value-of select="concat(' -csv ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="csvDelim">
        <xsl:value-of select="concat(' -csvDelim ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="targetDir">
        <xsl:value-of select="concat(' -targetDir ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="inputFiles">
        <xsl:value-of select="concat(' ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
	<xsl:template match="optfile">
        <xsl:value-of select="concat(' --optfile ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>