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
* along with this program.&nbsp; If not, see <http://www.gnu.org/licenses/>.
*/
package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.common.error.handling.ExceptionOutputManager;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandlerImpl;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.BeanReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.BeanReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.BeanReportDataSourceImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.resources.ClientBeanDataSource;
import com.jaspersoft.jasperserver.remote.resources.converters.BeanDataSourceResourceConverter;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Resource;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.testng.Assert.assertEquals;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id: BeanConnectionStrategyTest.java 57603 2015-09-15 17:20:48Z psavushc $
 */
public class BeanConnectionStrategyTest {
    private static ClientBeanDataSource INITIAL_CONNECTION_DESCRIPTION = new ClientBeanDataSource().
            setBeanName("test bean").setBeanMethod("test bean method");

    @InjectMocks
    private final BeanConnectionStrategy strategy = new BeanConnectionStrategy();

    @Mock
    private BeanReportDataSourceServiceFactory beanDataSourceFactory;
    @Mock
    private BeanDataSourceResourceConverter beanDataSourceResourceConverter;
    @Mock
    private SecureExceptionHandler secureExceptionHandlerMock;

    @Mock
    private ReportDataSourceService reportDataSourceService;
    private BeanReportDataSource serverBeanReportDataSource;
    private ClientBeanDataSource testConnectionDescription;

    @BeforeClass
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void resetMocks(){
        reset(beanDataSourceFactory, beanDataSourceResourceConverter, reportDataSourceService);
        testConnectionDescription = new ClientBeanDataSource(INITIAL_CONNECTION_DESCRIPTION);
        serverBeanReportDataSource = new BeanReportDataSourceImpl();

        when(beanDataSourceResourceConverter.toServer(same(INITIAL_CONNECTION_DESCRIPTION),
                any(ToServerConversionOptions.class))).thenReturn(serverBeanReportDataSource);
        when(beanDataSourceFactory.createService(serverBeanReportDataSource)).thenReturn(reportDataSourceService);

        when(secureExceptionHandlerMock.handleException(isA(Throwable.class), isA(ErrorDescriptor.class))).thenReturn(new ErrorDescriptor().setMessage("test"));
    }

    @Test
    public void createConnection_factoryCreatesReportDataSourceService_success(){
        ClientBeanDataSource result = strategy.createConnection(INITIAL_CONNECTION_DESCRIPTION, null);
        assertEquals(result.getBeanName(), testConnectionDescription.getBeanName());
        assertEquals(result.getBeanMethod(), testConnectionDescription.getBeanMethod());
    }


    @Test(expectedExceptions = ConnectionFailedException.class)
    public void createConnection_factoryNotCreatesReportDataSourceService_null(){
        doThrow(new RuntimeException()).when(beanDataSourceFactory).createService(serverBeanReportDataSource);
        strategy.createConnection(INITIAL_CONNECTION_DESCRIPTION, null);
    }
}
