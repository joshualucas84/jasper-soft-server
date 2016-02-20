package com.jaspersoft.jasperserver.api.metadata.common.service;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;

import java.io.OutputStream;

/**
 * @author Grant Bacon <gbacon@jaspersoft.com>
 * @version
 */
public interface ThumbnailGenerationService {

    public OutputStream createThumbnail(JasperPrint jasperPrint, JasperReportsContext jasperReportsContext);

}
