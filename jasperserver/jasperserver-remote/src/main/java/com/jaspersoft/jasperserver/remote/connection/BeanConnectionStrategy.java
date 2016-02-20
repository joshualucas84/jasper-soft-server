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
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.BeanReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.dto.resources.ClientBeanDataSource;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.resources.converters.BeanDataSourceResourceConverter;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id: BeanConnectionStrategy.java 57603 2015-09-15 17:20:48Z psavushc $
 */
@Service
public class BeanConnectionStrategy implements ConnectionManagementStrategy<ClientBeanDataSource> {
    @Resource(name = "beanDataSourceServiceFactory")
    private ReportDataSourceServiceFactory dataSourceFactory;
    @Resource
    private BeanDataSourceResourceConverter beanDataSourceResourceConverter;
    @Resource
    private SecureExceptionHandler secureExceptionHandler;

    @Override
    public ClientBeanDataSource createConnection(ClientBeanDataSource connectionDescription, Map<String, Object> data) throws IllegalParameterValueException {
        // functionality of this method is copied from com.jaspersoft.jasperserver.war.action.DataSourceAction.testBeanDataSource()
        Exception exception = null;
        boolean passed = false;
        BeanReportDataSource beanReportDataSource = beanDataSourceResourceConverter.
                toServer(connectionDescription, ToServerConversionOptions.getDefault().setSuppressValidation(true));

        try {
            ReportDataSourceService beanReportDataSourceService = dataSourceFactory.createService(beanReportDataSource);
            if (beanReportDataSourceService != null) {
                passed = true;
            }
        } catch(Exception e) {
            exception = e;
        }

        if(!passed){
            throw new ConnectionFailedException(connectionDescription, exception, secureExceptionHandler);
        }


        return connectionDescription;
    }

    @Override
    public void deleteConnection(ClientBeanDataSource connectionDescription, Map<String, Object> data) {
    }

    @Override
    public ClientBeanDataSource modifyConnection(ClientBeanDataSource newConnectionDescription, ClientBeanDataSource oldConnectionDescription, Map<String, Object> data) throws IllegalParameterValueException {
        // here is nothing to update, just check if it can be connected.
        return createConnection(newConnectionDescription, data);
    }

    @Override
    public ClientBeanDataSource secureGetConnection(ClientBeanDataSource connectionDescription, Map<String, Object> data) {
        // no hidden attributes
        return connectionDescription;
    }
}
