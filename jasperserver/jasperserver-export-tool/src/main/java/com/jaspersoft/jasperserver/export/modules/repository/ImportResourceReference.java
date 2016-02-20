package com.jaspersoft.jasperserver.export.modules.repository;

import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;

/**
 * Created by stas on 7/23/14.
 */
public class ImportResourceReference extends ResourceReference {
    public ImportResourceReference(Resource localResource) {
        super(localResource);
    }

    public ImportResourceReference(String referenceURI) {
        super(referenceURI);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImportResourceReference that = (ImportResourceReference) o;

        if (getTargetURI() != null ? !getTargetURI().equals(that.getTargetURI()) : that.getTargetURI() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getTargetURI() != null ? getTargetURI().hashCode() : 0;
    }
}
