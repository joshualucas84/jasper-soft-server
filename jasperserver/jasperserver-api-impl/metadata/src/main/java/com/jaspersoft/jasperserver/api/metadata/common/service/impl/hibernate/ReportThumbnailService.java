package com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

/**
 * This service stores and retrieves thumbnail records from the repository using Hibernate
 *
 * @author Grant Bacon <gbacon@jaspersoft.com>
 * @version
 */
public interface ReportThumbnailService {

    /**
     * Save report thumbnail in database relating it to the user and report it belongs to
     *
     * @param thumbnailStream
     * @param user
     * @param resource
     */
    public void saveReportThumbnail(ByteArrayOutputStream thumbnailStream, User user, Resource resource);

    /**
     * Obtain a persisted thumbnail
     *
     * @param user User who requested thumbnail
     * @param resource Client resource object of which to obtain a thumbnail
     * @return image data
     * @throws JSException
     */
    public ByteArrayInputStream getReportThumbnail(User user, Resource resource);


    /**
     * Obtain a persisted thumbnail
     *
     * @param user User who requested thumbnail
     * @param reportUri Location of the resource within the repository
     * @return image data
     * @throws JSException
     */
    public ByteArrayInputStream getReportThumbnail(User user, String reportUri);


    /**
     * Obtain the default thumbnail
     *
     * @return default thumbnail image data
     */
    public ByteArrayInputStream getDefaultThumbnail();

}
