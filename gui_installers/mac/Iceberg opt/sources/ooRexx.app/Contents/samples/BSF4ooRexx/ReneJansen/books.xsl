<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- <xsl:output method="text"/> -->
<xsl:output method="html"/>
<xsl:strip-space elements="*"/>

<xsl:template match="books">
  <html>
    <head>
  	<title>sequentially numbered booklist</title>
    </head>
    <body>
      <h1>A list of books</h1>
      <table width="640" border="1">
      <xsl:apply-templates/>
      </table>
    </body>
  </html>
</xsl:template>

<xsl:template match="book">
  <tr>
    <td><xsl:number/></td>
    <xsl:apply-templates/>
  </tr>
</xsl:template>

<xsl:template match="title | author | price">
  <td><xsl:value-of select="."/></td>
<xsl:text>&#xa;</xsl:text>
</xsl:template>

</xsl:stylesheet>
