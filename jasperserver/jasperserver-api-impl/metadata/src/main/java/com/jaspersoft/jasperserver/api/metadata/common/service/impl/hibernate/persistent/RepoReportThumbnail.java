package com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent;

import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.sql.Blob;

/**
 * @author Grant Bacon <gbacon@jaspersoft.com>
 * @version
 */

public class RepoReportThumbnail implements Serializable {
    private static final Log log = LogFactory.getLog(RepoReportThumbnail.class);

    public long id;
    public RepoResource resource;
    public RepoUser user;
    public Blob thumbnail;

    public RepoReportThumbnail() {}

    public Blob getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Blob thumbnail) {
        this.thumbnail = thumbnail;
    }

    public RepoResource getResource() {
        return resource;
    }

    public void setResource(RepoResource resource) {
        this.resource = resource;
    }

    public RepoUser getUser() {
        return user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getResourceId() {
        return resource.getId();
    }

    public void setResourceId(long resourceId) {
        this.resource.setId(resourceId); }

    public long getUserId() {
        return user.getId();
    }

    public void setUserId(long userId) {
        this.user.setId(userId);
    }

    public void setUser(RepoUser user) {
        this.user = user;
    }

    public boolean equals(Object other) {
        if (!(other instanceof RepoReportThumbnail)) return false;
        final RepoReportThumbnail thumbnailOther = (RepoReportThumbnail) other;

        return (thumbnailOther.getResource().getId() == this.resource.getId()) && (thumbnailOther.getUser().getId() == this.user.getId());
    }

    public int hashCode() {
        return super.hashCode();
    }



}
