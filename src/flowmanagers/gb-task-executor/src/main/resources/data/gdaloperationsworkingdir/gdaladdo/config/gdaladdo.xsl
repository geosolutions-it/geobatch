<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tsk="http://www.geo-solutions.it/tsk">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:template match="r">
        <xsl:value-of select="concat(' -r ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="ro">
        <xsl:value-of select="concat(' -ro')"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="clean">
        <xsl:value-of select="concat(' -clean ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="filename">
        <xsl:value-of select="concat(' ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="levels">
        <xsl:value-of select="concat(' ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
</xsl:stylesheet>