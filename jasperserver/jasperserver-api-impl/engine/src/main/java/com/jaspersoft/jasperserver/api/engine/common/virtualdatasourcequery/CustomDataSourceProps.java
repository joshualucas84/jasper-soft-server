package com.jaspersoft.jasperserver.api.engine.common.virtualdatasourcequery;

import com.jaspersoft.jasperserver.api.common.virtualdatasourcequery.CustomDataSource;

/**
 * Not a complete collection of custom datasource properties.
 * These properties used in {@link CustomDataSource#getPropertyMap()}.
 */
public final class CustomDataSourceProps {

    /**
     * Property name for a custom datasource resource.
     */
    public static final String CDS_RESOURCE = "_cds_resource";

    private CustomDataSourceProps() {
        // non instantiable
    }

}
