/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
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
package com.jaspersoft.jasperserver.jaxrs.authority;

import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.dto.authority.ClientUser;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.remote.common.UserSearchCriteria;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.RemoteException;
import com.jaspersoft.jasperserver.remote.exception.ResourceAlreadyExistsException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.converters.UserConverter;
import com.jaspersoft.jasperserver.remote.services.UserAndRoleService;
import com.jaspersoft.jasperserver.remote.services.impl.UserAndRoleServiceImpl;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * @author: Zakhar.Tomchenco
 */

public class UsersJaxrsService {
    @Resource
    private UserConverter userConverter;
    private UserAndRoleService service;

    public Response getUsers(int startIndex,
                             int maxRecords,
                             String tenantId,
                             Boolean includeSubOrgs,
                             Boolean hasAllRequiredRoles,
                             String search,
                             List<String> requredRoleNames) throws RemoteException {

        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setIncludeSubOrgs(includeSubOrgs);
        criteria.setName(search == null ? "" : search);
        criteria.setTenantId("".equals(tenantId) ? null : tenantId);
        criteria.setHasAllRequiredRoles(hasAllRequiredRoles == null ? true : hasAllRequiredRoles);

        if (requredRoleNames != null && requredRoleNames.size() > 0) {
            List<Role> roles = new LinkedList<Role>();
            Role found;
            for (String name : requredRoleNames) {
                found = findRole(name);
                if (found == null) {
                    throw new ResourceNotFoundException(name);
                }
                roles.add(found);
            }
            criteria.setRequiredRoles(roles);
        }

        List<User> users = service.findUsers(criteria);
        int totalCount = users.size();

        if (totalCount < startIndex) {
            users.clear();
        } else {
            if (maxRecords != 0) {
                if (startIndex + maxRecords > totalCount) {
                    users = users.subList(startIndex, totalCount);
                } else {
                    users = users.subList(startIndex, startIndex + maxRecords);
                }
            } else {
                if (startIndex > 0){
                    users = users.subList(startIndex, totalCount);
                }
            }
        }
        Response response;
        if (users.size() == 0) {
            response = Response.status(Response.Status.NO_CONTENT)
                    .header(RestConstants.HEADER_START_INDEX, startIndex)
                    .header(RestConstants.HEADER_RESULT_COUNT, users.size())
                    .header(RestConstants.HEADER_TOTAL_COUNT, totalCount)
                    .build();
        } else {
            response = Response.ok()
                    .entity(new UsersListWrapper(users))
                    .header(RestConstants.HEADER_START_INDEX, startIndex)
                    .header(RestConstants.HEADER_RESULT_COUNT, users.size())
                    .header(RestConstants.HEADER_TOTAL_COUNT, totalCount)
                    .build();
        }
        return response;
    }

    public Response putUsers(){
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    public Response createUser(ClientUser clientUser) throws RemoteException {
        if (findUser(clientUser.getUsername(), clientUser.getTenantId()) != null){
            throw new ResourceAlreadyExistsException(clientUser.getUsername());
        }

        User created = service.putUser(userConverter.toServer(clientUser, null));
        return Response.status(Response.Status.CREATED).entity(userConverter.toClient(created, null)).build();

    }

    public Response deleteUsers() {
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    public Response getPropertiesOfUser(String name,
                                        String tenantId) throws RemoteException {

        User user = findUser(name, tenantId);

        if (user != null){
            return Response.ok(userConverter.toClient(user, null)).build();
        }

        throw new ResourceNotFoundException(name);
    }

    public Response putUser(ClientUser clientUser, String name, String tenantId) throws RemoteException {
        User user = findUser(name, tenantId);
        Response.Status status = Response.Status.OK;

        if (user == null){
            status = Response.Status.CREATED;
            user = new UserImpl();
        }

        user = userConverter.toServer(clientUser, user, null);
        user.setUsername(name);
        user.setTenantId(tenantId);

        return Response.status(status).entity(userConverter.toClient(service.putUser(user), null)).build();
    }

    public Response postToUser(String name) {
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    public Response deleteUser(String name,
                               String tenantId) throws RemoteException {

        User user = findUser(name, tenantId);

        if (user == null){
            throw new ResourceNotFoundException(name);
        }

        service.deleteUser(user);

        return Response.noContent().build();
    }

    private User findUser(String name, String tenantId) throws RemoteException {
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setName(name);
        criteria.setTenantId("".equals(tenantId) ? null : tenantId);

        try{
            List<User> found = service.findUsers(criteria);

            for (User user : found) {
                if (requestedUser(user, name, tenantId)) {
                    return user;
                }
            }

        }  catch (IllegalStateException ise){
            throw new IllegalParameterValueException("username", "null");
        }

        return null;
    }

    private boolean requestedUser(User user, String name, String tenantId) {
        return (user.getUsername().equals(name)) &&
                ((user.getTenantId() == null && tenantId == null) ||
                (user.getTenantId() != null && user.getTenantId().equals(tenantId)));
    }

    private Role findRole(String name) throws RemoteException{
       return ((UserAndRoleServiceImpl)service).getUserAuthorityService().getRole(null,name);
    }

    public UserAndRoleService getService() {
        return service;
    }

    public void setService(UserAndRoleService service) {
        this.service = service;
    }

}
