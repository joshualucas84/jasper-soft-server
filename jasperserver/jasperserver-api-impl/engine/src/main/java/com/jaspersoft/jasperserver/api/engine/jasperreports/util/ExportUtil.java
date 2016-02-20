/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License  as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero  General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.AbstractHtmlExporter;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRExporterContext;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.AbstractXlsExporterConfiguration;
import net.sf.jasperreports.export.AbstractXlsReportConfiguration;
import net.sf.jasperreports.export.HtmlExporterConfiguration;
import net.sf.jasperreports.export.HtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.XlsExporterConfiguration;
import net.sf.jasperreports.export.XlsReportConfiguration;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: JRHtmlExportUtils.java 19932 2010-12-11 15:24:29Z tmatyashovsky $
 */
public class ExportUtil 
{
	public static final String HTTP_SERVLET_REQUEST = "HttpServletRequest";
	
	private final JasperReportsContext jasperReportsContext;
	private final String htmlType; 
	private final String xlsType; 
	
	private ExportUtil(JasperReportsContext jasperReportsContext)
	{
		if (jasperReportsContext == null)
		{
			jasperReportsContext = DefaultJasperReportsContext.getInstance();
		}
		this.jasperReportsContext = jasperReportsContext;
		JRPropertiesUtil propUtil = JRPropertiesUtil.getInstance(jasperReportsContext);
		htmlType = propUtil.getProperty("com.jaspersoft.jasperreports.export.html.type");
		xlsType = propUtil.getProperty("com.jaspersoft.jasperreports.export.xls.type");
	}

	public static ExportUtil getInstance(JasperReportsContext jasperReportsContext)
	{
		return new ExportUtil(jasperReportsContext);
	}

	public AbstractHtmlExporter<HtmlReportConfiguration, HtmlExporterConfiguration> createHtmlExporter()
	{
		if ("xhtml".equalsIgnoreCase(htmlType))
		{
			@SuppressWarnings("deprecation")
			net.sf.jasperreports.engine.export.JRXhtmlExporter depExp = new net.sf.jasperreports.engine.export.JRXhtmlExporter(jasperReportsContext);
			return depExp;
		}
		return new HtmlExporter(jasperReportsContext);
	}

	public JRXlsAbstractExporter<? extends XlsReportConfiguration, ? extends XlsExporterConfiguration, ? extends JRExporterContext> createXlsExporter()
	{
		if ("jxl".equalsIgnoreCase(xlsType))
		{
			@SuppressWarnings("deprecation")
			net.sf.jasperreports.engine.export.JExcelApiExporter depExp = new net.sf.jasperreports.engine.export.JExcelApiExporter(jasperReportsContext);
			return depExp;
		}
		return new JRXlsExporter(jasperReportsContext);
	}

	public AbstractXlsReportConfiguration createXlsReportConfiguration()
	{
		if ("jxl".equalsIgnoreCase(xlsType))
		{
			@SuppressWarnings("deprecation")
			net.sf.jasperreports.export.SimpleJxlReportConfiguration depConfig = new net.sf.jasperreports.export.SimpleJxlReportConfiguration();
			return depConfig;
		}
		return new SimpleXlsReportConfiguration();
	}

	public AbstractXlsExporterConfiguration createXlsExporterConfiguration()
	{
		if ("jxl".equalsIgnoreCase(xlsType))
		{
			@SuppressWarnings("deprecation")
			net.sf.jasperreports.export.SimpleJxlExporterConfiguration depConfig = new net.sf.jasperreports.export.SimpleJxlExporterConfiguration();
			return depConfig;
		}
		return new SimpleXlsExporterConfiguration();
	}

	@SuppressWarnings("deprecation")
	public void setConfiguration(
		JRXlsAbstractExporter<? extends XlsReportConfiguration, ? extends XlsExporterConfiguration, ? extends JRExporterContext> exporter,
		XlsReportConfiguration xlsReportConfig
		)
	{
		net.sf.jasperreports.engine.export.JExcelApiExporter depExp = 
			exporter instanceof net.sf.jasperreports.engine.export.JExcelApiExporter 
			? (net.sf.jasperreports.engine.export.JExcelApiExporter)exporter
			: null;
		if (depExp != null)
		{
			depExp.setConfiguration((net.sf.jasperreports.export.JxlReportConfiguration)xlsReportConfig);
		}
		else
		{
			((JRXlsExporter)exporter).setConfiguration(xlsReportConfig);
		}	
	}

	@SuppressWarnings("deprecation")
	public void setConfiguration(
		JRXlsAbstractExporter<? extends XlsReportConfiguration, ? extends XlsExporterConfiguration, ? extends JRExporterContext> exporter,
		XlsExporterConfiguration xlsExporterConfig
		)
	{
		net.sf.jasperreports.engine.export.JExcelApiExporter depExp = 
			exporter instanceof net.sf.jasperreports.engine.export.JExcelApiExporter 
			? (net.sf.jasperreports.engine.export.JExcelApiExporter)exporter
			: null;
		if (depExp != null)
		{
			depExp.setConfiguration((net.sf.jasperreports.export.JxlExporterConfiguration)xlsExporterConfig);
		}
		else
		{
			((JRXlsExporter)exporter).setConfiguration(xlsExporterConfig);
		}	
	}

}
