package com.jaspersoft.jasperserver.api.engine.jasperreports.util;


import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ichan
 * Date: 2/12/14
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class XlsxDataSourceValidator extends XlsDataSourceValidator {

	public void validatePropertyValues(CustomReportDataSource ds, Errors errors) {
        super.validatePropertyValues(ds, errors);
	}

}
