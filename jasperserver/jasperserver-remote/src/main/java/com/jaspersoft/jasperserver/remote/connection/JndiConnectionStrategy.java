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
package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.BaseJdbcDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.dto.resources.ClientJndiJdbcDataSource;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.resources.converters.JndiJdbcDataSourceResourceConverter;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Map;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id: JndiConnectionStrategy.java 57603 2015-09-15 17:20:48Z psavushc $
 */
@Service
public class JndiConnectionStrategy implements ConnectionManagementStrategy<ClientJndiJdbcDataSource> {
    protected final Log log = LogFactory.getLog(getClass());
    @Resource(name = "jndiJdbcDataSourceServiceFactory")
    private ReportDataSourceServiceFactory dataSourceFactory;
    @Resource
    private JndiJdbcDataSourceResourceConverter dataSourceResourceConverter;
    @Resource
    private SecureExceptionHandler secureExceptionHandler;

    @Override
    public ClientJndiJdbcDataSource createConnection(ClientJndiJdbcDataSource connectionDescription, Map<String, Object> data) throws IllegalParameterValueException {
        boolean passed = false;
        Throwable exception = null;
        JndiJdbcReportDataSource jndiJdbcReportDataSource = dataSourceResourceConverter.
                toServer(connectionDescription, ToServerConversionOptions.getDefault().setSuppressValidation(true));

        try {
            passed = ((BaseJdbcDataSource)dataSourceFactory.createService(jndiJdbcReportDataSource)).testConnection();
        } catch (SQLException vex) {
            if (vex.getMessage().indexOf("[JI_CONNECTION_VALID]") >= 0) passed = true;
            exception = vex;
        } catch(Throwable e) {
            exception = e;
        }
        if(!passed){
            throw new ConnectionFailedException(connectionDescription.getJndiName(), "jndiName", "Invalid JNDI name: "
                    + connectionDescription.getJndiName(), exception, secureExceptionHandler);
        }
        return connectionDescription;
    }

    @Override
    public void deleteConnection(ClientJndiJdbcDataSource connectionDescription, Map<String, Object> data) {
    }

    @Override
    public ClientJndiJdbcDataSource modifyConnection(ClientJndiJdbcDataSource newConnectionDescription, ClientJndiJdbcDataSource oldConnectionDescription, Map<String, Object> data) throws IllegalParameterValueException {
        // here is nothing to update, just check if it can be connected.
        return createConnection(newConnectionDescription, data);
    }

    @Override
    public ClientJndiJdbcDataSource secureGetConnection(ClientJndiJdbcDataSource connectionDescription, Map<String, Object> data) {
        // no hidden attributes
        return connectionDescription;
    }

}
