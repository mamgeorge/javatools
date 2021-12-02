<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <html>
            <head>
                <title>Books</title>
                <style>body { font-family: verdana; }</style>
            </head>
            <body>
                <center>
                    <h3>Book Collection</h3>
                    <table border="1">
                        <tr>
                            <th>Title</th>
                            <th>Published</th>
                            <th>Author</th>
                            <th>Price</th>
                        </tr>
                        <xsl:for-each select="catalog/book">
                            <tr>
                                <td>
                                    <xsl:value-of select="title"/>
                                </td>
                                <td>
                                    <xsl:value-of select="publish_date"/>
                                </td>
                                <td>
                                    <xsl:value-of select="author"/>
                                </td>
                                <td>
                                    <xsl:value-of select="price"/>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </center>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>