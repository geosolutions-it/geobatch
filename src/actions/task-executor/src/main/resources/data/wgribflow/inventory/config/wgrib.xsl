<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tsk="http://www.geo-solutions.it/tsk">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:template match="s">
        <xsl:value-of select="concat(' -s','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="v">
        <xsl:value-of select="concat(' -v','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="V">
        <xsl:value-of select="concat(' -V','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="PDS">
        <xsl:value-of select="concat(' -PDS','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="PDS10">
        <xsl:value-of select="concat(' -PDS10','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="GDS">
        <xsl:value-of select="concat(' -GDS','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="GDS10">
        <xsl:value-of select="concat(' -GDS10','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="verf">
        <xsl:value-of select="concat(' -verf','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="ncep_opn">
        <xsl:value-of select="concat(' -ncep_opn','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="ncep_rean">
        <xsl:value-of select="concat(' -ncep_rean','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="ncep_ens">
        <xsl:value-of select="concat(' -ncep_ens','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="4yr">
        <xsl:value-of select="concat(' -4yr','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="min">
        <xsl:value-of select="concat(' -min','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="d">
        <xsl:value-of select="concat(' -d ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
	<xsl:template match="p">
        <xsl:value-of select="concat(' -p ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="i">
        <xsl:value-of select="concat(' -i','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="text">
        <xsl:value-of select="concat(' -text','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
	<xsl:template match="ieee">
        <xsl:value-of select="concat(' -ieee','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="grib">
        <xsl:value-of select="concat(' -grib','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="bin">
        <xsl:value-of select="concat(' -bin','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="dwdgrib">
        <xsl:value-of select="concat(' -dwdgrib','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
      
    <xsl:template match="H">
        <xsl:value-of select="concat(' -H','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="append">
        <xsl:value-of select="concat(' -append','')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="o">
        <xsl:value-of select="concat(' -o ', .)"/>
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