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

package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeEscapeStrategy;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * The default {@link ProfileAttributesResolver} implementation.
 *
 * @author Volodya Sabadosh
 * @author Vlad Zavadskii
 * @version $Id: ProfileAttributesResolverImpl.java 58870 2015-10-27 22:30:55Z esytnik $
 */
public class ProfileAttributesResolverImpl implements ProfileAttributesResolver {
    private static final Log log = LogFactory.getLog(ProfileAttributesResolverImpl.class);

    private ProfileAttributeService profileAttributeService;
    private Pattern attributePlaceholderPattern;

    public Pattern getAttributeFunctionPattern() {
        return attributeFunctionPattern;
    }

    public void setAttributeFunctionPattern(String attributeFunctionPattern) {
        this.attributeFunctionPattern = Pattern.compile(attributeFunctionPattern, Pattern.MULTILINE);
    }

    private Pattern attributeFunctionPattern;
    private ObjectMapper objectMapper;
    private List<ProfileAttributeCategory> profileAttributeCategories;
    private MessageSource messageSource;
    private RepositoryService repositoryService;
    private Set<String> excludedResourcesFromAttrResolving;
    private boolean enabledResolving = true;

    public ProfileAttributesResolverImpl() {
        //Setup Polymorphic Object Mapper that do mapping using only class property fields (not getter and setter)
        objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public void setAttributePlaceholderPattern(String attributePlaceholderPattern) {
        this.attributePlaceholderPattern = Pattern.compile(attributePlaceholderPattern, Pattern.MULTILINE);
    }

    public void setProfileAttributeService(ProfileAttributeService profileAttributeService) {
        this.profileAttributeService = profileAttributeService;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setProfileAttributeCategories(List<ProfileAttributeCategory> profileAttributeCategories) {
        this.profileAttributeCategories = profileAttributeCategories;
    }

    public void setExcludedResourcesFromAttrResolving(Set<String> excludedResourcesFromAttrResolving) {
        this.excludedResourcesFromAttrResolving = excludedResourcesFromAttrResolving;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void setEnabledResolving(boolean enabledResolving) {
        this.enabledResolving = enabledResolving;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAttribute(String str) {
        return attributePlaceholderPattern.matcher(str).find();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParametrizedResource(Object resource, ProfileAttributeCategory... categories) {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();
            objectMapper.writeValue(outputStream, resource);
            String outputStreamStr = outputStream.toString();

            Scanner scanner = new Scanner(outputStreamStr);
            String foundAttribute;
            boolean hasAttributes = false;
            while ((foundAttribute = scanner.findInLine(attributeFunctionPattern)) !=null || scanner.hasNextLine()) {
                if (foundAttribute == null) {
                    scanner.nextLine();
                    continue;

                }

                MatchResult matchResult = scanner.match();
                String foundCategory = matchResult.group(3);
                ProfileAttributeCategory attrCategory = ProfileAttributeCategory.HIERARCHICAL;

                if (foundCategory != null) {
                    try {
                        attrCategory = ProfileAttributeCategory.valueOf(foundCategory.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                }
                hasAttributes = true;

                if (categories != null) {
                    for (ProfileAttributeCategory category : categories) {
                        if (!category.equals(attrCategory)) {
                            return false;
                        }
                    }
                } else {
                    return true;
                }
            }

            return hasAttributes;
        } catch (IOException e) {
            throw new JSException(e.toString());
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> T merge(T resource) {
        if (!enabledResolving || excludedResourcesFromAttrResolving.contains(resource.getClass().getCanonicalName())) {
            return resource;
        }

        try {
            String resourceUri = resource.getURIString();
            Resource baseResourceCopy = copyBaseResource(resource);
            escapeBaseResourceFieldsFromResolving(resource);

            OutputStream outputStream = new ByteArrayOutputStream();
            objectMapper.writeValue(outputStream, resource);
            String outputStreamStr = outputStream.toString();

            T resultResource;
            if (containsAttribute(outputStreamStr)) {
                String mergedJSON = merge(outputStream.toString(), resourceUri);

                resultResource = (T) objectMapper.readValue(mergedJSON, resource.getClass());
            } else {
                resultResource = resource;
            }
            revertEscapedBaseResourceFields(baseResourceCopy, resultResource);
            revertEscapedBaseResourceFields(baseResourceCopy, resource);

            return resultResource;
        } catch (IOException e) {
            throw new JSException(e.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    public String merge(String templateString, String identifier) {
        return merge(templateString, identifier, null);
    }

    /**
     * {@inheritDoc}
     */
    public String merge(String templateString, String identifier, ProfileAttributeEscapeStrategy escapeStrategy) {
        Map<ProfileAttributeCategory, Map<String, ProfileAttribute>> profileAttributeCategoryMap =
                new HashMap<ProfileAttributeCategory, Map<String, ProfileAttribute>>();
        String replacementString = templateString;
        Scanner scanner = new Scanner(templateString);

        String foundAttribute;
        while ((foundAttribute = scanner.findInLine(attributePlaceholderPattern)) !=null || scanner.hasNextLine()) {
            if (foundAttribute == null) {
                scanner.nextLine();
                continue;

            }
            MatchResult matchResult = scanner.match();
            String attrPlaceholder = matchResult.group(0);
            String attrName = matchResult.group(1);
            String foundCategory = matchResult.group(3);
            ProfileAttributeCategory attrCategory = ProfileAttributeCategory.HIERARCHICAL;

            String errorMessageBase = "";
            if (isNotEmpty(identifier) && !identifier.equals(Folder.SEPARATOR)) {
                errorMessageBase = messageSource.getMessage("profile.attribute.exception.substitution.base",
                        new Object[]{identifier, getErrorFieldName(templateString, attrPlaceholder)},
                        LocaleContextHolder.getLocale()) + " ";
            }

            if (foundCategory != null) {
                try {
                    attrCategory = ProfileAttributeCategory.valueOf(foundCategory.toUpperCase());
                } catch (IllegalArgumentException e) {
                    String message = errorMessageBase + messageSource.getMessage("profile.attribute.exception.substitution.category.invalid",
                            new Object[]{foundCategory, quote(attrName), profileAttributeCategories.toString()}, LocaleContextHolder.getLocale());
                    log.error(message);
                    throw new JSException(message);
                }
            }

            Map<String, ProfileAttribute> profileAttributeMap;
            if (!profileAttributeCategoryMap.containsKey(attrCategory)) {
                profileAttributeMap = getProfileAttributeMap(attrCategory);
                profileAttributeCategoryMap.put(attrCategory, profileAttributeMap);
            } else {
                profileAttributeMap = profileAttributeCategoryMap.get(attrCategory);
            }

            if (profileAttributeMap.containsKey(attrName)) {
                String attrValue = profileAttributeMap.get(attrName).getAttrValue();
                if (escapeStrategy != null) {
                    attrValue = escapeStrategy.escape(attrValue);
                }
                replacementString = replacementString.replace(attrPlaceholder, attrValue);

                log.debug(messageSource.getMessage("profile.attribute.debug.substitution.success",
                        new Object[]{quote(attrName), identifier, getErrorFieldName(templateString, attrPlaceholder), attrValue},
                        LocaleContextHolder.getLocale()));
            } else {
                String message = errorMessageBase + messageSource.getMessage("profile.attribute.exception.substitution.not.found",
                        new Object[]{quote(attrName), attrCategory.getLabel()},
                        LocaleContextHolder.getLocale());
                log.error(message);
                throw new JSException(message);
            }
        }

        return replacementString;
    }

    protected Map<String, ProfileAttribute> getProfileAttributeMap(ProfileAttributeCategory category) {
        Map<String, ProfileAttribute> profileAttributeMap = new HashMap<String, ProfileAttribute>();
        List<ProfileAttribute> profileAttributes = profileAttributeService.
                getCurrentUserProfileAttributes(ExecutionContextImpl.getRuntimeExecutionContext(), category);

        if (profileAttributes != null) {
            for (ProfileAttribute profileAttribute : profileAttributes) {
                profileAttributeMap.put(profileAttribute.getAttrName(), profileAttribute);
            }
        }

        return profileAttributeMap;
    }

    protected String getErrorFieldName(String templateString, String attrPlaceholder) {
        String attrLiteral = Pattern.quote(attrPlaceholder);
        Pattern fieldNamePattern = Pattern.compile("\"([\\w$]+)\"\\s*:\\s*\"([^\"]|(\\\\\"))*" + attrLiteral,
                Pattern.MULTILINE);
        Matcher matcher = fieldNamePattern.matcher(templateString);
        return matcher.find() ? quote(matcher.group(1)) : "";
    }

    private String quote(String messages) {
        return "'" + messages + "'";
    }

    private Resource copyBaseResource(final Resource resource) {
        Resource baseResourceCopy = new ResourceImpl() {
            @Override
            protected Class getImplementingItf() {
                return resource.getClass();
            }
        };
        baseResourceCopy.setName(resource.getName());
        baseResourceCopy.setDescription(resource.getDescription());
        baseResourceCopy.setLabel(resource.getLabel());
        baseResourceCopy.setParentFolder(resource.getParentFolder());
        baseResourceCopy.setURIString(resource.getURIString());

        return baseResourceCopy;
    }

    private void revertEscapedBaseResourceFields(Resource copyBaseResource, Resource originalResource) {
        originalResource.setName(copyBaseResource.getName());
        originalResource.setDescription(copyBaseResource.getDescription());
        originalResource.setLabel(copyBaseResource.getLabel());
        originalResource.setParentFolder(copyBaseResource.getParentFolder());
        originalResource.setURIString(copyBaseResource.getURIString());
    }

    private void escapeBaseResourceFieldsFromResolving(Resource resource) {
        resource.setName("");
        resource.setDescription("");
        resource.setLabel("");
        resource.setParentFolder("");
        resource.setURIString("");
    }
}
