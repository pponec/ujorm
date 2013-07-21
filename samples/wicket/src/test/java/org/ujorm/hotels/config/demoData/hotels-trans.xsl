<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
<!ENTITY cr "&#xa;">
]>
<!-- Demo-hotels HTML stylesheet
<copyright>PPone(c)2013</copyright>
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method='text'/>

<!-- Parameters: -->
<xsl:param name="CITY_ID" select="'100'"/>
<xsl:param name="SEPARATOR" select="';'"/>
<xsl:param name="TRUE" select="'true'"/>
<xsl:template match="/">

<!-- CSV: -->
<xsl:for-each select="HotelList/Hotel">
<xsl:if test="stars != ''">
<xsl:if test="url != ''">
<xsl:if test="price != ''">

<xsl:value-of select="name"/>
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="substring-before(description, ' is ')" /> <!-- ONLY STREET -->
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="$CITY_ID" />
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="substring-before(fulladdress, ' ,')" /> <!-- ONLY STREET -->
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="phone"/>
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="stars"/>
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="url"/>
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="price"/>
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="$TRUE"/>
<xsl:text>&cr;</xsl:text>
</xsl:if>
</xsl:if>
</xsl:if>
</xsl:for-each>


</xsl:template>
</xsl:stylesheet>
