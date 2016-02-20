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
package com.jaspersoft.jasperserver.jaxrs.connection;

import com.jaspersoft.jasperserver.jaxrs.common.JaxrsEntityParser;
import com.jaspersoft.jasperserver.remote.connection.ConnectionsManager;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.ClientTypeHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: ConnectionsJaxrsService.java 49286 2014-09-23 13:32:25Z ykovalchyk $
 */
@Service
@Path("/connections")
public class ConnectionsJaxrsService {
    private static final Pattern EXTRACT_CONNECTION_TYPE_PATTERN = Pattern.compile("application/([^\\.]+)\\.([^(\\.|\\+)]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CONNECTION_METADATA_PATTERN = Pattern.compile("application/([^\\.]+)(\\.[^.]+)?\\.metadata\\+.+",
            Pattern.CASE_INSENSITIVE);
    @Context
    private Providers providers;
    @Context
    private HttpHeaders httpHeaders;
    @Context
    private HttpServletRequest request;
    @Resource
    private ConnectionsManager connectionsManager;

    @POST
    public Response createConnection(InputStream stream, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType mediaType,
            @HeaderParam(HttpHeaders.ACCEPT) MediaType accept) throws URISyntaxException, IllegalParameterValueException, IOException {
        final Class<?> connectionClass = getConnectionClass(mediaType);
        final String acceptString = accept != null ? accept.toString() : "";
        final boolean isMetadataRequested = CONNECTION_METADATA_PATTERN.matcher(acceptString).matches();
        final Object connectionDescription = parseEntity(connectionClass, stream, mediaType);
        final UUID connectionId = connectionsManager.createConnection(connectionDescription);
        final StringBuffer locationBuffer = request.getRequestURL().append("/").append(connectionId.toString());
        if (isMetadataRequested) {
            final Object connectionMetadata = connectionsManager.getConnectionMetadata(connectionId);
            return Response.ok(connectionMetadata)
                    .location(new URI(locationBuffer.append("/metadata").toString()))
                    .header("Content-Type", "application/" + ClientTypeHelper.extractClientType(connectionMetadata.getClass())
                            + ".metadata" + acceptString.substring(acceptString.lastIndexOf("+"))).build();
        } else {
            return Response.created(new URI(locationBuffer.toString()))
                    .entity(connectionDescription).build();
        }
    }

    protected Object parseEntity(Class<?> connectionClass, InputStream stream, MediaType mediaType) throws IOException {
        return JaxrsEntityParser.newInstance(providers, httpHeaders).parseEntity(connectionClass, stream, mediaType);
    }

    @PUT
    @Path("/{uuid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response modifyConnection(@PathParam("uuid") UUID uuid, InputStream stream, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType mediaType) throws URISyntaxException, IllegalParameterValueException, ResourceNotFoundException, IOException {
        final Object connectionDescription = parseEntity(getConnectionClass(mediaType), stream, mediaType);
        final Object newConnection = connectionsManager.modifyConnection(uuid, connectionDescription);
        return Response.ok(newConnection).build();
    }

    @GET
    @Path("/{uuid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getConnectionDetails(@PathParam("uuid") UUID uuid) throws ResourceNotFoundException {
        final Object connection = connectionsManager.getConnection(uuid);
        return connection != null ? Response.ok(connection).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{uuid}")
    public Response removeConnectionDetails(@PathParam("uuid") UUID uuid) throws ResourceNotFoundException {
        connectionsManager.removeConnection(uuid);
        return Response.noContent().build();
    }

    @GET
    @Path("/{uuid}/metadata")
    public Response getConnectionMetadata(@PathParam("uuid") UUID uuid) {
        return Response.ok(connectionsManager.getConnectionMetadata(uuid)).build();
    }

    protected Class<?> getConnectionClass(MediaType mediaType) {
        if(mediaType ==  null){
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MandatoryParameterNotFoundException("Content-Type").getErrorDescriptor()).build());
        }
        Class<?> connectionClass = null;
        Matcher matcher = EXTRACT_CONNECTION_TYPE_PATTERN.matcher(mediaType.toString());
        if (matcher.find()) {
            String connectionType = matcher.group(2);
            connectionClass = connectionsManager.getConnectionDescriptionClass(connectionType);
        }
        if (connectionClass == null) {
            throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
        }
        return connectionClass;
    }
}
