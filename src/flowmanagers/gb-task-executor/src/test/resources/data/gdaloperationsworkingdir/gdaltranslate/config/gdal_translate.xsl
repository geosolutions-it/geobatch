<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tsk="http://www.geo-solutions.it/tsk">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:template match="ot">
        <xsl:value-of select="concat(' -ot ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="strict">
        <xsl:value-of select="concat(' -strict')"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="of">
        <xsl:value-of select="concat(' -of ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="outsize">
        <xsl:value-of select="concat(' -outsize ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="expand">
        <xsl:value-of select="concat(' -expand ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="scale">
        <xsl:value-of select="concat(' -scale ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="srcwin">
        <xsl:value-of select="concat(' -srcwin ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="prjwin">
        <xsl:value-of select="concat(' -prjwin ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="a_srs">
        <xsl:value-of select="concat(' -a_srs ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="a_url">
        <xsl:value-of select="concat(' -a_url ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="a_nodata">
        <xsl:value-of select="concat(' -a_nodata ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="gcp">
        <xsl:value-of select="concat(' -gcp ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="b">
        <xsl:value-of select="concat(' -b ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="sds">
        <xsl:value-of select="concat(' -sds ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="co">
        <xsl:value-of select="concat(' -co ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="source">
        <xsl:value-of select="concat(' ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="destination">
        <xsl:value-of select="concat(' ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
</xsl:stylesheet>