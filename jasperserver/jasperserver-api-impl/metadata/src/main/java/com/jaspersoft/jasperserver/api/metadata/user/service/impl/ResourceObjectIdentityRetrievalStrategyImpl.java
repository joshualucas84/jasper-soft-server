package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;

/**
 * @author Oleg.Gavavka
 */

public class ResourceObjectIdentityRetrievalStrategyImpl implements ObjectIdentityRetrievalStrategy {

    @Override
    public ObjectIdentity getObjectIdentity(Object domainObject) {
        if (domainObject instanceof InternalURIDefinition) {
            return (InternalURIDefinition) domainObject;
        }

        // Creating new InternalURI for object because we have many implementations of InternalURI which are not consistent
        if (domainObject instanceof InternalURI) {
            PermissionUriProtocol protocol = PermissionUriProtocol.fromString(((InternalURI) domainObject).getProtocol());
            return new InternalURIDefinition(((InternalURI) domainObject).getPath(), protocol);
        }
        if (domainObject instanceof String) {
            return new InternalURIDefinition((String)domainObject);
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
