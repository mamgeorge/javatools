<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fn="fn"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		version="2.0" exclude-result-prefixes="xs fn">

	<xsl:output method="html"
	            doctype-system="http://www.w3.org/TR/html4/strict.dtd"
	            doctype-public="-//W3C//DTD HTML 4.01//EN"
	            indent="yes"/>

	<xsl:param name="csv" select="'battles.csv'"/>
	<xsl:param name="lineEnding" select="'&#xD;&#xA;'"/>

	<xsl:template match="/">

		<html>
			<head>
				<title>battles</title>
			</head>
			<body>
				<h3>battles</h3>

				<table border="1">

					<xsl:for-each select="row">
						<tr>
							<xsl:for-each select="column">
								<td>
									<xsl:value-of select="."/>
								</td>
							</xsl:for-each>
						</tr>
					</xsl:for-each>
				</table>
			</body>
		</html>

	</xsl:template>
</xsl:stylesheet>
