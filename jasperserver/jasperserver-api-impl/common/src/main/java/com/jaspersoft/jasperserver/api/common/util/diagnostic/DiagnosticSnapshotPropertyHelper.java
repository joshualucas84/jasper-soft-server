/**
 * @author fpang
 * @since Oct 10, 2014
 * @version $Id: DiagnosticSnapshotPropertyHelper.java 52124 2014-12-25 10:08:39Z ytymoshenko $
 *
 */

package com.jaspersoft.jasperserver.api.common.util.diagnostic;

/*
*
*  JasperReports - Free Java Reporting Library.
*  Copyright (C) 2001 - 2009 Jaspersoft Corporation. All rights reserved.
*  http://www.jaspersoft.com.
*
*  Unless you have purchased a commercial license agreement from Jaspersoft,
*  the following license terms apply:
*
*  This program is part of JasperReports.
*
*  JasperReports is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  JasperReports is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
* /
*/

import java.util.Map;
import java.util.Properties;

/**
 * @author fpang
 *
 */
public class DiagnosticSnapshotPropertyHelper
{
	public final static String ATTRIBUTE_IS_DIAG_SNAPSHOT = "isDiagSnapshot";

	public static boolean isDiagSnapshotSet(Map<String, Object> map)
	{
		String val = (String)map.get(ATTRIBUTE_IS_DIAG_SNAPSHOT);
		return Boolean.valueOf(val);
	}
	
	public static boolean isDiagSnapshotSet(Properties props)
	{
		String val = props.getProperty(ATTRIBUTE_IS_DIAG_SNAPSHOT);
		return Boolean.valueOf(val);
	}
}
