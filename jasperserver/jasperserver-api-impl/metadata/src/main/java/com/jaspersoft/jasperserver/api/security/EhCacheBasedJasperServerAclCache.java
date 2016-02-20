/*
 * Copyright (C) 2005 - 2011 Jaspersoft Corporation. All rights reserved.
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
package com.jaspersoft.jasperserver.api.security;

import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.security.JasperServerAclImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.util.FieldUtils;
import org.springframework.util.Assert;

/**
 * @author Oleg.Gavavka
 */

public class EhCacheBasedJasperServerAclCache implements NonMutableAclCache {
    private final Ehcache cache;
    private PermissionGrantingStrategy permissionGrantingStrategy;

    public EhCacheBasedJasperServerAclCache(Ehcache cache, PermissionGrantingStrategy permissionGrantingStrategy) {
        Assert.notNull(cache, "Cache required");
        Assert.notNull(permissionGrantingStrategy, "PermissionGrantingStrategy required");
        this.cache = cache;
        this.permissionGrantingStrategy = permissionGrantingStrategy;

    }

    @Override
    public void evictFromCache(ObjectIdentity objectIdentity) {
        Assert.notNull(objectIdentity, "ObjectIdentity required");
        cache.remove(objectIdentity.getIdentifier().toString());

    }

    @Override
    public Acl getFromCache(ObjectIdentity objectIdentity) {
        Assert.notNull(objectIdentity, "ObjectIdentity required");

        Element element = null;

        try {
            element = cache.get(objectIdentity.getIdentifier().toString());
        } catch (CacheException ignored) {}

        if (element == null) {
            return null;
        }



        return initializeTransientFields((Acl)element.getValue());
    }

    @Override
    public void putInCache(Acl acl) {
        Assert.notNull(acl, "Acl required");
        Assert.notNull(acl.getObjectIdentity(), "ObjectIdentity required");

        if ((acl.getParentAcl() != null) && (acl.getParentAcl() instanceof Acl)) {
            putInCache(acl.getParentAcl());
        }
        cache.put(new Element(acl.getObjectIdentity().getIdentifier().toString(), acl));


    }

    private Acl initializeTransientFields(Acl value) {
        if (value instanceof JasperServerAclImpl) {
            FieldUtils.setProtectedFieldValue("permissionGrantingStrategy", value, this.permissionGrantingStrategy);
            if (value.isEntriesInheriting()) {
                //seeking parent in cache
                Acl parentAcl = getFromCache(((JasperServerAclImpl) value).getParentOid());
                // parent is missing in cache - invalidate this Acl
                if (parentAcl==null) {
                    evictFromCache(value.getObjectIdentity());
                    return null;
                }
                FieldUtils.setProtectedFieldValue("parentAcl", value, parentAcl);

            }
        }

        if (value.getParentAcl() != null) {
            initializeTransientFields(value.getParentAcl());
        }
        return value;
    }

    @Override
    public void clearCache() {
        cache.removeAll();

    }
}
