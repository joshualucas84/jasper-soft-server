/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * Licensed under commercial Jaspersoft Subscription License Agreement
 */

package example.cdspro;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.AbstractTextDataSourceDefinition;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JRDesignField;

import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;

/**
 * Defines JSON datasource which can be read from a file or repository.
 */
public class JsonDataSourceDefinition extends AbstractTextDataSourceDefinition {

    private static final long serialVersionUID = -1698557680384988822L;
    private RepositoryService repositoryService;

    @Override
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customDataSource) throws Exception {

        Map<String, Object> propertyValueMap = new HashMap<String, Object>();
        propertyValueMap = getDataSourceServicePropertyMap(customDataSource, propertyValueMap);

        List<JRField> jrFields = getFields(customDataSource, propertyValueMap);

        return CustomDomainMetadataUtils.createCustomDomainMetaData("JsonQuery", jrFields);
    }

    private List<JRField> getFields(CustomReportDataSource customDataSource, Map<String, Object> propertyValueMap) throws JRException {
        String query = (String) propertyValueMap.get("selectExpression");
        ResourceReference resourceRef = null;
        if (customDataSource.getResources() != null) {
            resourceRef = customDataSource.getResources().get("dataFile");
        }

        JsonDataSourceUtils.RowExtractor rowExtractor = null;
        // if no resource then it's a file in a file system
        if (resourceRef == null) {
            rowExtractor = JsonDataSourceUtils.getRowExtractor((String) propertyValueMap.get("fileName"), query);
        } else {
            FileResource fileResource = getFileResource(resourceRef);
            InputStream is = getJsonInputStream(fileResource);
            rowExtractor = JsonDataSourceUtils.getRowExtractor(is, query);
        }
        List<JRField> jrFields = rowExtractor.getNextRowFields();
        for (int i = 0; (getRowCountForMetadataDiscovery() < 0) || (i < getRowCountForMetadataDiscovery()); i++) {
             List<JRField> newRowFields = rowExtractor.getNextRowFields();
            if (newRowFields == null) break;
            for (int j = 0; j < newRowFields.size(); j++) {
                String name = newRowFields.get(j).getName();
                JRField curField = findField(jrFields, name);
                if (curField == null) {
                    jrFields.add(newRowFields.get(j));
                } else {
                    ((JRDesignField)curField).setValueClassName(getCompatibleDataType(curField.getValueClassName(), newRowFields.get(j).getValueClassName()));
                }
            }
        }


        return jrFields;
    }

    private JRField findField(List<JRField>jrFields, String name) {
        for (JRField jrField: jrFields) {
            if (jrField.getName().equals(name)) return jrField;
        }
        return null;
    }


    private InputStream getJsonInputStream(FileResource fileResource) {
        InputStream is;
        if (fileResource.hasData()) {
            is = fileResource.getDataStream();
        } else {
            FileResourceData resourceData = repositoryService.getResourceData(null, fileResource.getURIString());
            is = resourceData.getDataStream();
        }
        return is;
    }

    private FileResource getFileResource(ResourceReference resourceRef) {
        FileResource fileResource;
        if (resourceRef.isLocal()) {
            fileResource = (FileResource) resourceRef.getLocalResource();
        } else {
            fileResource = (FileResource) repositoryService.getResource(null, resourceRef.getReferenceURI(), FileResource.class);
        }
        return fileResource;
    }

    @Override
    public Map<String, Object> customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object> propertyValueMap) {
        return propertyValueMap;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

}
