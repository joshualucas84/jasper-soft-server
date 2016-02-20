package com.jaspersoft.jasperserver.export;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Zakhar.Tomchenco
 * Date: 7/25/12
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectPermissionServiceMock implements ObjectPermissionService {
    public ObjectPermission newObjectPermission(ExecutionContext context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ObjectPermission getObjectPermission(ExecutionContext context, ObjectPermission objPerm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getObjectPermissionsForRecipient(ExecutionContext context, Object recipient) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getObjectPermissionsForObject(ExecutionContext context, Object targetObject) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ObjectPermission> getEffectivePermissionsForObject(ExecutionContext context, Object targetObject) {
        return null; // TODO: implement.
    }

    public List getObjectPermissionsForObjectAndRecipient(ExecutionContext context, Object targetObject, Object recipient) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isObjectAdministrable(ExecutionContext context, Object targetObject) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void putObjectPermission(ExecutionContext context, ObjectPermission objPerm) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deleteObjectPermission(ExecutionContext context, ObjectPermission objPerm) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deleteObjectPermissionForObject(ExecutionContext context, Object targetObject) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deleteObjectPermissionsForRecipient(ExecutionContext context, Object recipient) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getInheritedObjectPermissionMask(ExecutionContext context, Object targetObject, Object recipient) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public List<ObjectPermission> getObjectPermissionsForSubtree(String uri) {
		return null;
	}

}
