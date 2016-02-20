/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * Licensed under commercial Jaspersoft Subscription License Agreement
 */
 
package example.cdspro;

import com.jaspersoft.connectors.cassandra.adapter.CassandraDataAdapter;
import com.jaspersoft.connectors.cassandra.connection.JSCassandraConnection;
import net.sf.jasperreports.data.AbstractDataAdapterService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReportsContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: elrull
 * Date: 1/30/14
 */
public class CassandraDataAdapterService extends AbstractDataAdapterService {

    private static final Log log = LogFactory.getLog(CassandraDataAdapterService.class);

    private JSCassandraConnection connection;

    private CassandraDataAdapter dataAdapter;

    public CassandraDataAdapterService(JasperReportsContext jContext, CassandraDataAdapter dataAdapter){
        super(jContext, dataAdapter);
        this.dataAdapter = dataAdapter;
    }

    @Override
    public void contributeParameters(Map<String, Object> parameters) throws JRException {
        if (connection != null) {
            dispose();
        }
        if (dataAdapter != null) {
            try {
                createConnection();
                parameters.put(JRParameter.REPORT_CONNECTION, connection);
            } catch (Exception e) {
                throw new JRException(e);
            }
        }
    }
    private void createConnection() throws Exception {
        System.out.println("hostname: " + dataAdapter.getHostname());
        System.out.println("port:" + dataAdapter.getPort());
        System.out.println("keyspace:" + dataAdapter.getKeyspace());
        System.out.println("username: " + dataAdapter.getUsername());
        System.out.println("password: " + dataAdapter.getPassword());

        connection = new JSCassandraConnection(
            dataAdapter.getHostname(), 
            dataAdapter.getPort(), 
            dataAdapter.getKeyspace(), 
            dataAdapter.getUsername(), 
            dataAdapter.getPassword()
        );
    }

    @Override
    public void dispose() {
        try {
            if (connection != null)
                connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (log.isErrorEnabled())
                log.error("Error while closing the connection.", e);
        }
    }

    @Override
    public void test() throws JRException {
        try {
            if (connection != null) {
            } else {
                try {
                    createConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new JRException(e);
                }
            }
            connection.test();
        } finally {
            dispose();
        }
    }
}
