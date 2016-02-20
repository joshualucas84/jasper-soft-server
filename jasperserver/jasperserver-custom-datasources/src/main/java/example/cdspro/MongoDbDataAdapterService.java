/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * Licensed under commercial Jaspersoft Subscription License Agreement
 */
package example.cdspro;

import java.util.Map;

import net.sf.jasperreports.data.AbstractDataAdapterService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import com.jaspersoft.mongodb.adapter.MongoDbDataAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jaspersoft.mongodb.connection.MongoDbConnection;

/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: MongoDbDataAdapterService.java 45929 2014-09-25 18:28:22Z ichan $
 * @author Eric Diaz
 */
public class MongoDbDataAdapterService extends AbstractDataAdapterService {
    private static final Log log = LogFactory.getLog(MongoDbDataAdapterService.class);

    private MongoDbConnection connection;

    private MongoDbDataAdapter dataAdapter;


    public MongoDbDataAdapterService(JasperReportsContext jasperReportsContext, MongoDbDataAdapter dataAdapter) {
        super(jasperReportsContext, dataAdapter);
        this.dataAdapter=dataAdapter;
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

    private void createConnection() throws JRException {
        connection = new MongoDbConnection(dataAdapter.getMongoURI(), dataAdapter.getUsername(),
        		 dataAdapter.getPassword());
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
                createConnection();
            }
            connection.test();
        } finally {
            dispose();
        }
    }
}
