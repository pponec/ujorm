<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
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
<xsl:if test="name != ''
    and string-length(name) &lt;= 40
    and string-length(substring-after(description, ' is ')) &lt;= 256
    and string-length(substring-before(fulladdress, ' ,')) &lt;= 128
    and string-length(phone) &lt;= 20
    and number(stars) = stars
    and number(price) = price
    and url != ''
    and string-length(url) &lt;= 100
">

<xsl:value-of select="name"/>
<xsl:value-of select="$SEPARATOR"/>
<xsl:value-of select="substring-after(description, ' is ')" /> <!-- ONLY STREET -->
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
<xsl:text>&#xa;</xsl:text>
</xsl:if>

</xsl:for-each>

</xsl:template>
</xsl:stylesheet>
