package com.jaspersoft.jasperserver.api.metadata.common.service.impl;


import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;

/**
 * Create thumbnails asynchronously
 *
 * @author Grant Bacon (gbacon@jaspersoft.com)
 */
public interface AsyncThumbnailCreator {

    /**
     * Generate a thumbnail, and persist it.
     *
     * @param jpAccessor
     * @param reportContext
     * @param user
     */
    public void createAndStoreThumbnail(JasperPrintAccessor jpAccessor, JasperReportsContext reportContext, User user, Resource resource);

}
