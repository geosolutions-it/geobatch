<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tsk="http://www.geo-solutions.it/tsk">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:template match="s_srs">
        <xsl:value-of select="concat(' -s_srs ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="t_srs">
        <xsl:value-of select="concat(' -t_srs ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="to">
        <xsl:value-of select="concat(' -to ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="order">
        <xsl:value-of select="concat(' -order ', .)"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="tps">
        <xsl:value-of select="concat(' -tps')"/>
         <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="rpc">
        <xsl:value-of select="concat(' -rpc')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="geoloc">
        <xsl:value-of select="concat(' -geoloc')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="et">
        <xsl:value-of select="concat(' -et ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="te">
        <xsl:value-of select="concat(' -te ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="tr">
        <xsl:value-of select="concat(' -tr ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="ts">
        <xsl:value-of select="concat(' -ts ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="wo">
        <xsl:value-of select="concat(' -wo ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="ot">
        <xsl:value-of select="concat(' -ot ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="wt">
        <xsl:value-of select="concat(' -wt ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
	<xsl:template match="r">
        <xsl:value-of select="concat(' -r ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="srcnodata">
        <xsl:value-of select="concat(' -srcnodata ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="dstnodata">
        <xsl:value-of select="concat(' -dstnodata ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
	<xsl:template match="dstalpha">
        <xsl:value-of select="concat(' -dstalpha')"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="wm">
        <xsl:value-of select="concat(' -wm ', .)"/>
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
      
    <xsl:template match="wm">
        <xsl:value-of select="concat(' -wm ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="cutline">
        <xsl:value-of select="concat(' -cutline ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    
    <xsl:template match="cl">
        <xsl:value-of select="concat(' -cl ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
          
    <xsl:template match="cwhere">
        <xsl:value-of select="concat(' -cwhere ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
 
    <xsl:template match="csql">
        <xsl:value-of select="concat(' -csql ', .)"/>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
          
    <xsl:template match="cblend">
        <xsl:value-of select="concat(' -cblend ', .)"/>
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