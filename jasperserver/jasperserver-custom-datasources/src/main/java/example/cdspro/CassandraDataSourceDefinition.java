/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * Licensed under commercial Jaspersoft Subscription License Agreement
 */

package example.cdspro;

import com.jaspersoft.connectors.cassandra.CassandraFieldsProvider;
import com.jaspersoft.connectors.cassandra.adapter.CassandraDataAdapter;
import com.jaspersoft.connectors.cassandra.adapter.CassandraDataAdapterImpl;
import com.jaspersoft.connectors.cassandra.connection.JSCassandraConnection;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataAdapterDefinition;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataAdapterDefinitionUtil;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDomainMetaDataImpl;

import net.sf.jasperreports.data.DataAdapter;
import net.sf.jasperreports.data.DataAdapterService;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JRDesignDataset;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author ichan
 * custom report data source definition for MongoDB Query
 * This class is meant to be instantiated as a spring bean that registers a custom data source with the system.
 */
public class CassandraDataSourceDefinition extends DataAdapterDefinition {

    public CassandraDataSourceDefinition() {
        // add additional field
        Set<String> additionalPropertySet = getAdditionalPropertySet();
        additionalPropertySet.add("query");

        // define default values for the following properties
        Map<String, String> propertyDefaultValueMap = getPropertyDefaultValueMap();
        propertyDefaultValueMap.put("name", "cassandraWithMetaData");
        propertyDefaultValueMap.put("queryExecuterMode", "true");
        propertyDefaultValueMap.put("query", "");

        // hide the following properties from UI
        Set<String> hiddenPropertySet = getHiddenPropertySet();
        hiddenPropertySet.add("name");
        hiddenPropertySet.add("queryExecuterMode");
        hiddenPropertySet.add("columnNames");
        // set query executor factory
        Map<String, String> queryExecuterMap = new HashMap<String, String>();
        queryExecuterMap.put("cql", "com.jaspersoft.connectors.cassandra.query.CassandraQueryExecuterFactory");
        setQueryExecuterMap(queryExecuterMap);
    }

    @Override
    public Map<String, Object>  customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object>  propertyValueMap) {
        if (propertyValueMap.get("hostname") != null) {
            propertyValueMap.put("hostname", ((String) propertyValueMap.get("hostname")).trim());
        }
        return propertyValueMap;
    }

    /*
    * This function is used for retrieving the metadata layer of the custom data source in form of CustomDomainMetaData
    * CustomDomainMetaData contains information JRFields, query, query language and field name mapping (actual JRField name, name used in domain)
    */
    @Override
    public CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customDataSource) throws Exception {

        // METADATA DISCOVERY
        Map<String, Object> propertyValueMap = new HashMap<String, Object>();
        propertyValueMap = getDataSourceServicePropertyMap(customDataSource, propertyValueMap);
        propertyValueMap = customizePropertyValueMap(customDataSource, propertyValueMap);

        // create MongoDB DATA ADAPTER from Custom Report Data Source properties
        // map custom report data source properties to data adapter
        CassandraDataAdapter cassandraDataAdapter = new CassandraDataAdapterImpl();
        cassandraDataAdapter = (CassandraDataAdapter) setupDataAdapter(cassandraDataAdapter, propertyValueMap);

        if (getValidator() != null) getValidator().validatePropertyValues(customDataSource, null);
        String query = (String)propertyValueMap.get("query");
        java.util.List<net.sf.jasperreports.engine.design.JRDesignField> jrDesignFields = getJRDesignFields(cassandraDataAdapter, query);

        List<String> columnNames = new ArrayList<String>();
        List<String> columnTypes = new ArrayList<String>();
        List<String> columnDescriptions = new ArrayList<String>();
        for (JRDesignField field : jrDesignFields) {
            columnNames.add(field.getName());
            if (isSupportedType(field.getValueClassName())) columnTypes.add(field.getValueClassName());
            else columnTypes.add("java.lang.String");    // if it is not supported types, converts it to string for now
            columnDescriptions.add(field.getDescription());
        }
        // create CustomDomainMetaDataImpl object
        CustomDomainMetaDataImpl sourceMetadata = new CustomDomainMetaDataImpl();
        sourceMetadata.setQueryLanguage("cql");
        sourceMetadata.setFieldNames(columnNames);
        Map<String, String> fieldMapping = new HashMap<String, String>();
        for (String str : columnNames) fieldMapping.put(str, str);
        sourceMetadata.setFieldMapping(fieldMapping);
        // set default column data type based on the actual data
        sourceMetadata.setFieldTypes(columnTypes);
        sourceMetadata.setQueryText(query);
        sourceMetadata.setFieldDescriptions(columnDescriptions);

        return sourceMetadata;

    }

    /**
	*  Return data adapter service for this custom data source
	**/	
    @Override
    public DataAdapterService getDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter) {
            return new CassandraDataAdapterService(jasperReportsContext, (CassandraDataAdapter)dataAdapter);
    }

    protected java.util.List<net.sf.jasperreports.engine.design.JRDesignField> getJRDesignFields(CassandraDataAdapter dataAdapter, String query) throws Exception {
            Map<String,Object> parameterValues = new HashMap<String, Object>();
            // convert your param map to fill params...
            Map<String, Object>  fillParams = DataAdapterDefinitionUtil.convertToFillParameters(parameterValues, getQueryExecuterFactory().getBuiltinParameters());
            JSCassandraConnection cassandraConnection = new JSCassandraConnection(dataAdapter.getHostname(), dataAdapter.getPort(), dataAdapter.getKeyspace(), dataAdapter.getUsername(), dataAdapter.getPassword());
            JRDesignDataset designDataset = new JRDesignDataset(false);
            JRDesignQuery jrquery = new JRDesignQuery();
            jrquery.setText(query);
            jrquery.setLanguage("cql");
            designDataset.setQuery(jrquery);
            return CassandraFieldsProvider.getInstance().getFields(DataAdapterDefinitionUtil.getJasperReportsContext(), cassandraConnection, designDataset, fillParams);
    }

    private static Set supportedTypeSet;
    static {
        supportedTypeSet = new HashSet();
        supportedTypeSet.add("java.lang.String");
        supportedTypeSet.add("java.lang.Byte");
        supportedTypeSet.add("java.lang.Short");
        supportedTypeSet.add("java.lang.Integer");
        supportedTypeSet.add("java.lang.Long");
        supportedTypeSet.add("java.lang.Float");
        supportedTypeSet.add("java.lang.Double");
        supportedTypeSet.add("java.lang.Number");
        supportedTypeSet.add("java.util.Date");
        supportedTypeSet.add("java.sql.Date");
        supportedTypeSet.add("java.sql.Time");
        supportedTypeSet.add("java.sql.Timestamp");
        supportedTypeSet.add("java.math.BigDecimal");
        supportedTypeSet.add("java.math.BigInteger");
        supportedTypeSet.add("java.lang.Boolean");
        supportedTypeSet.add("java.lang.Object");
    }

    private boolean isSupportedType(String type) {
        return supportedTypeSet.contains(type);
    }

}

