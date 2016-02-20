package com.jaspersoft.jasperserver.jaxrs.resources;


import com.jaspersoft.jasperserver.api.metadata.common.domain.*;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.dto.resources.*;
import com.jaspersoft.jasperserver.dto.resources.hypermedia.*;
import com.jaspersoft.jasperserver.jaxrs.common.RestConstants;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.RequestInfoProvider;
import com.jaspersoft.jasperserver.jaxrs.wadl.PrivateApi;
import com.jaspersoft.jasperserver.remote.exception.RemoteException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConverter;
import com.jaspersoft.jasperserver.remote.services.BatchRepositoryService;
import com.jaspersoft.jasperserver.remote.services.SingleRepositoryService;
import com.jaspersoft.jasperserver.search.mode.AccessType;
import com.jaspersoft.jasperserver.search.service.RepositorySearchResult;
import com.jaspersoft.jasperserver.war.common.ConfigurationBean;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@PrivateApi
@Service
@Path("/api/resources")
@Transactional(rollbackFor = Exception.class)
public class HypermediaRepositoryJaxrsService {

    @Resource
    protected ResourceConverterProvider resourceConverterProvider;
    @Resource
    protected SingleRepositoryService singleRepositoryService;

    @Resource
    protected BatchRepositoryService batchRepositoryService;

    @Resource
    protected RequestInfoProvider requestInfoProvider;

    @Resource
    private Map<String, String> contentTypeMapping;

    @Resource
    private ConfigurationBean configurationBean;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, ResourceMediaType.FOLDER_XML, ResourceMediaType.FOLDER_JSON})
    public Response getResources(
            @QueryParam(RestConstants.QUERY_PARAM_SEARCH_QUERY) String q,
            @QueryParam("folderUri") String folderUri,
            @QueryParam("type") List<String> type,
            @QueryParam("excludeFolder") List<String> excludeFolders,
            @QueryParam("containerType") List<String> containerType,
            @QueryParam("accessType") String accessTypeString,
            @QueryParam(RestConstants.QUERY_PARAM_OFFSET)@DefaultValue("0") Integer start,
            @QueryParam(RestConstants.QUERY_PARAM_LIMIT)@DefaultValue("100") Integer limit,
            @QueryParam("recursive") @DefaultValue("true") Boolean recursive,
            @QueryParam("showHiddenItems") @DefaultValue("false") Boolean showHiddenItems,
            @QueryParam("forceTotalCount") @DefaultValue("false") Boolean forceTotalCount,
            @QueryParam(RestConstants.QUERY_PARAM_SORT_BY) String sortBy,
            @QueryParam(RestConstants.QUERY_PARAM_EXPANDED) Boolean expanded,
            @QueryParam("forceFullPage") @DefaultValue("false") Boolean forceFullPage,
            @HeaderParam(HttpHeaders.ACCEPT) String accept) throws RemoteException {

        Response res;
        if (ResourceMediaType.FOLDER_JSON.equals(accept) || ResourceMediaType.FOLDER_XML.equals(accept)) {
            res = getResourceDetails("", accept, containerType, expanded);
        } else {

            final String organizationFolderRegex = "^(?:" + configurationBean.getOrganizationsFolderUri() + "/([^/]+))*";
            final List<String> excludeFoldersWithPublic = new ArrayList<String>(excludeFolders);
            
            AccessType accessType = AccessType.ALL;
            if (accessTypeString != null && !"".equals(accessTypeString)) {
                if (AccessType.MODIFIED.name().equalsIgnoreCase(accessTypeString)) {
                    accessType = AccessType.MODIFIED;
                }
                if (AccessType.VIEWED.name().equalsIgnoreCase(accessTypeString)) {
                    accessType = AccessType.VIEWED;
                }
            }

            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            RepositorySearchResult<ClientResourceLookup> searchResult =
                    batchRepositoryService.getResources(q, folderUri, ListUtils.union(type, containerType), null, excludeFolders,
                            start, limit,
                            recursive, showHiddenItems,
                            sortBy, accessType, user,
                            forceFullPage);

            List<HypermediaResourceLookup> hypermediaLookups = new ArrayList<HypermediaResourceLookup>(searchResult.getItems().size());
            excludeFoldersWithPublic.add(configurationBean.getPublicFolderUri());
            for (ClientResourceLookup resourceLookup : searchResult.getItems()) {

                HypermediaResourceLookup hypermediaResourceLookup = new HypermediaResourceLookup(resourceLookup);

                if (containerType.contains(resourceLookup.getResourceType())) {
                	
                	boolean excludeWithPublic = false;

                    if (resourceLookup.getUri().matches(organizationFolderRegex)) {
                    	excludeWithPublic = true;
                    }

                    int resourcesCount = batchRepositoryService.getResourcesCount(
                    		q, resourceLookup.getUri(), 
                    		type,
                            excludeWithPublic? excludeFoldersWithPublic:excludeFolders, 
                            true,
                            showHiddenItems, 
                            accessType, 
                            null
                    );

                    if (resourcesCount > 0) {
                        //add links
                        HypermediaResourceLinks links = new HypermediaResourceLinks();
                        links.setSelf(requestInfoProvider.getBaseUrl() + "rest_v2/hypermedia_ext/resources" + hypermediaResourceLookup.getUri());


                        links.setContent(buildResourcesSearchLink(q, hypermediaResourceLookup.getUri(), type,
                                containerType, accessTypeString, start, limit, recursive, showHiddenItems,
                                forceTotalCount, sortBy, expanded, forceFullPage, accept));

                        hypermediaResourceLookup.setLinks(links);
                    } else {
                        //add stubs for links
                        HypermediaResourceLinks links = new HypermediaResourceLinks();
                        links.setSelf(requestInfoProvider.getBaseUrl() + "rest_v2/hypermedia_ext/resources" + hypermediaResourceLookup.getUri());
                        links.setContent("");

                        hypermediaResourceLookup.setLinks(links);
                    }
                }
                hypermediaLookups.add(hypermediaResourceLookup);
            }

            HypermediaResourceListWrapper result = new HypermediaResourceListWrapper(hypermediaLookups);
            HypermediaResourceLookupLinks lookupLinks = new HypermediaResourceLookupLinks();

            lookupLinks.setSelf(buildResourcesSearchLink(q, folderUri, type,
                    containerType, accessTypeString, start, limit, recursive, showHiddenItems,
                    forceTotalCount, sortBy, expanded, forceFullPage, accept));

            lookupLinks.setNext(buildResourcesSearchLink(q, folderUri, type,
                    containerType, accessTypeString, start + 100, limit, recursive, showHiddenItems,
                    forceTotalCount, sortBy, expanded, forceFullPage, accept));

            if (start - 100 >= 0) {
                lookupLinks.setPrev(buildResourcesSearchLink(q, folderUri, type,
                        containerType, accessTypeString, start - 100, limit, recursive, showHiddenItems,
                        forceTotalCount, sortBy, expanded, forceFullPage, accept));
            } else {
                lookupLinks.setPrev("");
            }

            result.setLinks(lookupLinks);

            final int iStart = searchResult.getClientOffset();
            final int iLimit = searchResult.getClientLimit();
            Response.ResponseBuilder response = new ResponseBuilderImpl();

            int realResultSize = searchResult.size();
            boolean isForceTotalCount = (forceTotalCount != null && forceTotalCount);

            if (realResultSize != 0) {
                response.status(Response.Status.OK).entity(result);

                response.header(RestConstants.HEADER_START_INDEX, iStart)
                        .header(RestConstants.HEADER_RESULT_COUNT, realResultSize);

                int totalCount = realResultSize;
                if (isForceTotalCount || iStart == 0 || forceFullPage) {
                    totalCount = forceFullPage
                            ? (((realResultSize < iLimit) && !isForceTotalCount) ? realResultSize : searchResult.getTotalCount())
                            : batchRepositoryService.getResourcesCount(q, folderUri, type, excludeFolders, recursive, showHiddenItems, accessType, user);
                }

                if (iStart == 0 || isForceTotalCount || iLimit == 0 || forceFullPage) {
                    response.header(RestConstants.HEADER_TOTAL_COUNT, totalCount);
                }

                if (forceFullPage && (searchResult.getNextOffset() < searchResult.getTotalCount())) {
                    response.header(RestConstants.HEADER_NEXT_OFFSET, searchResult.getNextOffset());
                }
            } else {
                response.status(Response.Status.NO_CONTENT);
            }

            res = response.build();
        }
        return res;

    }

    @GET
    @Path("/{uri: .+}")
    public Response getResourceDetails(@PathParam(ResourceDetailsJaxrsService.PATH_PARAM_URI) String uri,
                                       @HeaderParam(HttpHeaders.ACCEPT) String accept,
                                       @QueryParam("containerType") List<String> containerType,
                                       @QueryParam(RestConstants.QUERY_PARAM_EXPANDED) Boolean _expanded) throws RemoteException {

        boolean expanded = _expanded != null ? _expanded : false;
        uri = Folder.SEPARATOR + uri.replaceAll("/$", "");

        com.jaspersoft.jasperserver.api.metadata.common.domain.Resource resource = singleRepositoryService.getResource(uri);
        if (resource == null) {
            throw new ResourceNotFoundException(uri);
        }
        Response response;

        if ((resource instanceof FileResource || resource instanceof ContentResource) && !ResourceMediaType.FILE_XML.equals(accept) && !ResourceMediaType.FILE_JSON.equals(accept)) {
            FileResourceData data = singleRepositoryService.getFileResourceData(resource);
            FileResourceData wrapper = new SelfCleaningFileResourceDataWrapper(data);
            String type = resource instanceof FileResource ? ((FileResource) resource).getFileType() : ((ContentResource) resource).getFileType();
            response = toResponse(wrapper, resource.getName(), type);
        } else {
            final ToClientConverter<? super com.jaspersoft.jasperserver.api.metadata.common.domain.Resource, ? extends ClientResource> toClientConverter =
                    resourceConverterProvider.getToClientConverter(resource);
            final ClientResource clientResource = toClientConverter.toClient(resource, ToClientConversionOptions.getDefault().setExpanded(expanded));

            final HypermediaResource hypermediaResource = new HypermediaResource(clientResource);
            HypermediaResourceLinks links = new HypermediaResourceLinks();
            String selfUri = requestInfoProvider.getBaseUrl() + "rest_v2/hypermedia_ext/resources?" + hypermediaResource.getUri();
            if (_expanded != null) selfUri = selfUri + "&expanded=" + _expanded;

            if (containerType != null) {
                for (String ct : containerType) {
                    selfUri = selfUri + "&containerType=" + ct;
                }
            }
            links.setSelf(selfUri);

            if (containerType != null && containerType.contains(toClientConverter.getClientResourceType())) {
                links.setContent(buildResourcesSearchLink(null, hypermediaResource.getUri(), null, containerType, null,
                        0, 100, false, false, null, null, null, null, accept));
            } else {
                links.setContent("");
            }
            hypermediaResource.setLinks(links);

            String contentTypeTemplate = accept != null && accept.endsWith("json") ? ResourceMediaType.RESOURCE_JSON_TEMPLATE : ResourceMediaType.RESOURCE_XML_TEMPLATE;
            response = Response.ok(hypermediaResource)
                    .header(HttpHeaders.CONTENT_TYPE,
                            contentTypeTemplate.replace(ResourceMediaType.RESOURCE_TYPE_PLACEHOLDER,
                                    toClientConverter.getClientResourceType()))
                    .build();
        }

        return response;
    }

    protected Response toResponse(FileResourceData data, String name, String fileType) {
        if (!data.hasData()) {
            return Response.noContent().build();
        }
        Response.ResponseBuilder builder = Response.ok(data.getDataStream());
        builder.header("Pragma", "").header("Cache-Control", "no-store");

        if (ContentResource.TYPE_XLS.equals(fileType) || ContentResource.TYPE_XLSX.equals(fileType) || ContentResource.TYPE_DOCX.equals(fileType)) {
            builder.header("Content-Disposition", "inline");
        }

        String contentType = contentTypeMapping.get(fileType);
        if (contentType == null) {
            if (name.contains(".") && !name.endsWith(".")) {
                contentType = contentTypeMapping.get(name.substring(name.indexOf(".") + 1));
            }
        }

        if (contentType == null && ContentResource.TYPE_IMAGE.equals(fileType)) {
            try {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(data.getDataStream()));
                String format = null;
                while (readers.hasNext()) {
                    format = readers.next().getFormatName();
                }
                contentType = "image/" + (format == null ? "*" : format.toLowerCase());
            } catch (Exception e) {
                // Some unknown file, which pretend to be an image. Ignore it.
            }
        }

        return builder.type(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType).build();
    }

    protected String buildResourcesSearchLink(String q, String folderUri, List<String> type, List<String> containerType,
                                              String accessType, Integer offset, Integer limit, Boolean recursive,
                                              Boolean showHiddenItems, Boolean forceTotalCount, String sortBy,
                                              Boolean expanded, Boolean forceFullPage, String accept) {

        StringBuilder sb = new StringBuilder();
        sb.append(requestInfoProvider.getBaseUrl())
                .append("rest_v2/hypermedia_ext/resources?");

        if (!StringUtils.isEmpty(q)) sb.append("&q=").append(q);
        if (!StringUtils.isEmpty(folderUri)) sb.append("&folderUri=").append(folderUri);
        if (!StringUtils.isEmpty(accessType)) sb.append("&accessType=").append(accessType);
        if (offset != null) sb.append("&offset=").append(offset);
        if (limit != null) sb.append("&limit=").append(limit);
        if (recursive != null) sb.append("&recursive=").append(recursive);
        if (showHiddenItems != null) sb.append("&showHiddenItems=").append(showHiddenItems);
        if (forceTotalCount != null) sb.append("&forceTotalCount=").append(forceTotalCount);
        if (!StringUtils.isEmpty(sortBy)) sb.append("&sortBy=").append(sortBy);
        if (expanded != null) sb.append("&expanded=").append(expanded);
        if (forceFullPage != null) sb.append("&forceFullPage=").append(forceFullPage);
        if (!StringUtils.isEmpty(accept)) sb.append("&Accept=").append(accept);

        if (type != null) {
            for (String t : type) {
                sb.append("&type=").append(t);
            }
        }

        if (containerType != null) {
            for (String t : containerType) {
                sb.append("&containerType=").append(t);
            }
        }

        return sb.toString();
    }

}
