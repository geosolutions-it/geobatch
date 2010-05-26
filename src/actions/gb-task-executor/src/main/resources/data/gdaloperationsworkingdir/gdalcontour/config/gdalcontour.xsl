<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tsk="http://www.geo-solutions.it/tsk">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:template match="b">
        <xsl:value-of select="concat(' -b ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="a">
        <xsl:value-of select="concat(' -a ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="3d">
        <xsl:value-of select="concat(' -3d')"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="inodata">
        <xsl:value-of select="concat(' -inodata')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="snodata">
        <xsl:value-of select="concat(' -snodata ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="f">
        <xsl:value-of select="concat(' -f ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="i">
        <xsl:value-of select="concat(' -i ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="off">
        <xsl:value-of select="concat(' -off ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="fl">
        <xsl:value-of select="concat(' -fl ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="nln">
        <xsl:value-of select="concat(' -nln ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="srcfile">
        <xsl:value-of select="concat(' ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="dstfile">
        <xsl:value-of select="concat(' ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
</xsl:stylesheet>