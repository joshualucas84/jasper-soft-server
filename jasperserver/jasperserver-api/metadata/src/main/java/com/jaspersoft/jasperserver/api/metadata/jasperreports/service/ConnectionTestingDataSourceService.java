package com.jaspersoft.jasperserver.api.metadata.jasperreports.service;

import com.jaspersoft.jasperserver.api.JasperServerAPI;

/**
 * A Data Source Service which can test its connection.
 */
@JasperServerAPI
public interface ConnectionTestingDataSourceService {

    /**
     * Tests connection.
     * Implementors can indicate failed test by throwing an exception or returning <code>false</code>.
     * @return <code>true</code> if connection was tested successfully.
     * @throws Exception
     */
    boolean testConnection() throws Exception;
}
