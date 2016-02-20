package com.jaspersoft.jasperserver.api.metadata.security;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.JasperServerSidRetrievalStrategyImpl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Volodya Sabadosh
 * @version $Id: $
 */
public class ContextSensitiveAttributeArgumentVoter extends AttributeArgumentVoter {
    public static final String DEFAULT_PERMS = "default";

    @Resource
    private String roleAdministrator;

    private Map<String, List<Permission>> requiredPermissionsMap;

    @Override
    protected List<Sid> generateSids(Authentication authentication, Object object) {
        List<Sid> sids = super.generateSids(authentication, object);

        if (isPrivilegedForExecuteOnly(object)) {
            Role role = new RoleImpl();
            role.setRoleName(roleAdministrator);

            if (sidStrategy instanceof JasperServerSidRetrievalStrategyImpl) {
                sids.add(((JasperServerSidRetrievalStrategyImpl) sidStrategy).getSid(role));
            }
        }

        return sids;
    }

    @Override
    protected List<Permission> generateRequiredPermissions(Object secureObject) {
        if (isPrivilegedForExecuteOnly(secureObject)) {
            return requiredPermissionsMap.get(ExecutionContextImpl.EXECUTE_OVERRIDE);
        } else {
            return requiredPermissionsMap.get(DEFAULT_PERMS);
        }
    }

    public void setRequiredPermissionsMap(Map<String, List<Permission>> requiredPermissionsMap) {
        this.requiredPermissionsMap = requiredPermissionsMap;
    }

}
