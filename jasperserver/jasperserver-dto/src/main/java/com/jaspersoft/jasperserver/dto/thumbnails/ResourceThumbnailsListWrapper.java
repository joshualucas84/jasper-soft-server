package com.jaspersoft.jasperserver.dto.thumbnails;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 */
public class ResourceThumbnailsListWrapper {
    private List<ResourceThumbnail> thumbnails;


    public ResourceThumbnailsListWrapper() {}

    public ResourceThumbnailsListWrapper(ResourceThumbnailsListWrapper other) {
        this.thumbnails = other.getThumbnails();
    }

    public ResourceThumbnailsListWrapper(List<ResourceThumbnail> thumbnails) {
        this.thumbnails = thumbnails;
    }

    @XmlElement(name = "thumbnail")
    public List<ResourceThumbnail> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<ResourceThumbnail> thumbnails) {
        this.thumbnails = thumbnails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceThumbnailsListWrapper)) return false;

        ResourceThumbnailsListWrapper that = (ResourceThumbnailsListWrapper) o;

        if (thumbnails != null ? !thumbnails.equals(that.thumbnails) : that.thumbnails != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return thumbnails != null ? thumbnails.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ResourceThumbnailsListWrapper{" +
                "thumbnails=" + thumbnails +
                '}';
    }
}
