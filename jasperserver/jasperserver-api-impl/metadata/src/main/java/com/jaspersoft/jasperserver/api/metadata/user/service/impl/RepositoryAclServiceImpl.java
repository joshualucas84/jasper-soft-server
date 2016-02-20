package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.common.util.ImportRunMonitor;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.common.domain.PermissionUriProtocol;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.security.JasperServerAclImpl;
import com.jaspersoft.jasperserver.api.metadata.security.JasperServerPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ObjectPermissionImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg.Gavavka
 */
public class RepositoryAclServiceImpl implements AclService {
    protected final Log logger = LogFactory.getLog(getClass());
    protected static final Log log = LogFactory.getLog(RepositoryAclServiceImpl.class);

    public static final String RECIPIENT_USED_FOR_INHERITANCE_MARKER = "___INHERITANCE_MARKER_ONLY___";

    protected static final String RESOURCE_URI_PREFIX = Resource.URI_PROTOCOL + ":";
    protected static final int RESOURCE_URI_PREFIX_LENGTH = RESOURCE_URI_PREFIX.length();

    static String RECIPIENT_FOR_CACHE_EMPTY = "RESERVED_RECIPIENT_NOBODY";

    private ObjectPermissionService permissionService;
    private JasperServerSidRetrievalStrategyImpl sidRetrievalStrategy;
    private AclService aclLookupStrategy;

    @Override
    public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
        return null;  
    }

    @Override
    public Acl readAclById(ObjectIdentity object) throws NotFoundException {
        String identifier = object.getIdentifier().toString();
        InternalURI uri = object instanceof InternalURI ? (InternalURI) object : new InternalURIDefinition(identifier);
        // cut "repo:" or "attr:" from internalURI, because getObjectPermissionsForObject always adds "repo:" to uri
        for (PermissionUriProtocol protocol : PermissionUriProtocol.values()) {
            if (uri.getPath().startsWith(protocol.getProtocolPrefix())) {
                uri = new InternalURIDefinition(PermissionUriProtocol.removePrefix(uri.getPath()),
                        PermissionUriProtocol.getProtocol(uri.getPath()));
            }
        }

        log.debug("trying to find ACL by Object for: "+ identifier);
        //temporary ACL to create ACE
        Acl result = new JasperServerAclImpl(uri,null);
        List<Object> uniqueList = new ArrayList<Object>();
        List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();
        // Special case for Import
        if (isImportRunning()) {
            Sid sid = sidRetrievalStrategy.getSid(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            aces.add(new AccessControlEntryImpl(null,result,sid,JasperServerPermission.ADMINISTRATION,true,false,false));
            return new JasperServerAclImpl(uri,aces);
        }

        if (uri!=null) {
            List objectPermisions = permissionService.getObjectPermissionsForObject(null,uri);
            log.debug("get objectPermissions, count: "+objectPermisions.size()+" for URI "+uri.getURI());

            for(Object op: objectPermisions) {
                if (op instanceof ObjectPermission) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("ObjectPermission: "+op.toString());
                    }
                    JasperServerPermission permission = new JasperServerPermission(((ObjectPermission) op).getPermissionMask());
                    Object recipient = ((ObjectPermission) op).getPermissionRecipient();
                    log.debug("processing permission:"+permission.toString()+" for "+recipient.toString());
                    Sid sid = sidRetrievalStrategy.getSid(recipient);
                    AccessControlEntry accessControlEntry = new AccessControlEntryImpl(null,result,sid ,permission,true,false,false);
                    aces.add(accessControlEntry);
                }
            }
        }

        String parentPath = uri.getParentPath();
        Acl parentAcl=null;
        if (parentPath!=null) {
            log.debug("Found parent, trying to get permissions for him");
            InternalURI parentUri = new InternalURIDefinition(parentPath,
                    PermissionUriProtocol.fromString(uri.getProtocol()));
            parentAcl= getAclLookupStrategy().readAclById(parentUri);
        } else {
            log.debug("No parent found for URI "+uri.getURI());
        }
        result = new JasperServerAclImpl(uri,aces,parentAcl);
        log.debug("returning ACL: "+result.toString());

        return result;  
    }

    @Override
    public Acl readAclById(ObjectIdentity object, List<Sid> incomeSids) throws NotFoundException {
        List<Sid> sids = incomeSids != null ? incomeSids : new ArrayList<Sid>();
        List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();
        log.debug("trying to find ACL by Object and List<Sid>, Object:"+ object.getIdentifier().toString() );
        log.debug("Sid list:"+ sids.toString());
        Acl result = getAclLookupStrategy().readAclById(object);
        //Filtering result to have only requested Sids
/*
        for(AccessControlEntry ace: result.getEntries()) {
            if (sids.contains(ace.getSid())) {
                aces.add(ace);
            }
        }
        // If requested SID is not in our result - generate empty one
        for (Sid sid : sids) {
            Boolean foundSid = false;
            for (AccessControlEntry ace : result.getEntries()) {
                foundSid = ace.getSid().equals(sid);
                if (foundSid) {
                    break;
                }
            }
            if (!foundSid) {
                aces.add(new AccessControlEntryImpl(null, result, sid, JasperServerPermission.NOTHING, true, false, false));
            }
        }
*/
        //if (result.isSidLoaded(incomeSids))

        //return  new JasperServerAclImpl(result.getObjectIdentity(),aces,result.getParentAcl());
        return result;
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects) throws NotFoundException {
        Map<ObjectIdentity, Acl> result = new HashMap<ObjectIdentity, Acl>();
        for(ObjectIdentity oid: objects) {
            result.put(oid,getAclLookupStrategy().readAclById(oid));
        }

        return result;
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids) throws NotFoundException {
        Map<ObjectIdentity, Acl> result = new HashMap<ObjectIdentity, Acl>();
        for(ObjectIdentity oid: objects) {
            result.put(oid,getAclLookupStrategy().readAclById(oid,sids));
        }

        return result;
    }

    public void setPermissionService(ObjectPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setSidRetrievalStrategy(JasperServerSidRetrievalStrategyImpl sidRetrievalStrategy) {
        this.sidRetrievalStrategy = sidRetrievalStrategy;
    }

    public void setAclLookupStrategy(AclService aclLookupStrategy) {
        this.aclLookupStrategy = aclLookupStrategy;
    }

    public AclService getAclLookupStrategy() {
        // if we don`t have AclLookupStrategy use this bean
        return aclLookupStrategy!=null ? aclLookupStrategy: this;
    }
    private Boolean isImportRunning() {
        if (ImportRunMonitor.isImportRun()){
            //Add special permission for current user
            //to allow updating special resources like themes etc
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            for (GrantedAuthority authority : authentication.getAuthorities()){
                if (authority.getAuthority().equals(ObjectPermissionService.PRIVILEGED_OPERATION)){
                    return true;
                }
            }
        }
        return false;
    }
}
