/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * Licensed under commercial Jaspersoft Subscription License Agreement
 */


package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import java.util.Map;
import java.util.Set;


/**
 * @author ichan
 * custom report data source definition for SPARK QUERY
 * This class is meant to be instantiated as a spring bean that registers a custom data source with the system.
 */
public class MongoDbJDBCDataSourceDefinition extends JDBCQueryDataSourceDefinition {

    public static String FILE_NAME_PROP = "fileName";
    public static String DATA_FILE_RESOURCE_ALIAS = "dataFile";

    public MongoDbJDBCDataSourceDefinition() {
        super();
        // add additional field
        Set<String> additionalPropertySet = getAdditionalPropertySet();
        additionalPropertySet.remove("query");
        additionalPropertySet.add("portNumber");
        additionalPropertySet.add("connectionOptions");
        additionalPropertySet.add("timeZone");
        additionalPropertySet.add(FILE_NAME_PROP);

        // define default values for the following properties
        Map<String, String> propertyDefaultValueMap = getPropertyDefaultValueMap();
        propertyDefaultValueMap.remove("query");
        propertyDefaultValueMap.put("name", "MongoDbJDBCDataSource");
        propertyDefaultValueMap.put("driver", "tibcosoftware.jdbc.mongodb.MongoDBDriver");
        propertyDefaultValueMap.put("portNumber", "27017");
        propertyDefaultValueMap.put("timeZone", null);

        // hide the following properties from UI
        Set<String> hiddenPropertySet = getHiddenPropertySet();
        hiddenPropertySet.remove("serverAddress");
        hiddenPropertySet.remove("database");
        hiddenPropertySet.add("driver");
        hiddenPropertySet.add("url");
    }

    @Override
    public Map<String, Object>  customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object>  propertyValueMap) {
        if(customReportDataSource.getResources() != null && customReportDataSource.getResources().get(DATA_FILE_RESOURCE_ALIAS) != null){
            // if data source has a data file as a sub resource, then take it's URI as file name property
            propertyValueMap.put(FILE_NAME_PROP, "repo:" + customReportDataSource.getResources()
                    .get(DATA_FILE_RESOURCE_ALIAS).getTargetURI());
        }
        return propertyValueMap;
    }

    /*
    * This function is used for retrieving the metadata layer of the custom data source in form of CustomDomainMetaData
    * CustomDomainMetaData contains information JRFields, query, query language and field name mapping (actual JRField name, name used in domain)
    */
    @Override
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customDataSource) throws Exception {
        return null;
    }

}

