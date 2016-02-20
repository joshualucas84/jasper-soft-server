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
public class TextDataSourceValidator extends XlsDataSourceValidator {

	public void validatePropertyValues(CustomReportDataSource ds, Errors errors) {
        super.validatePropertyValues(ds, errors);
		String fieldDelimiter = null;
		Map props = ds.getPropertyMap();
		if (props != null) {
			fieldDelimiter = (String) ds.getPropertyMap().get("fieldDelimiter");
		}

        if (fieldDelimiter == null || fieldDelimiter.length() == 0) {
            reject(errors, "fieldDelimiter", "Please enter field delimiter");
        } else if (fieldDelimiter.length() > 1) {
            reject(errors, "fieldDelimiter", "Invalid field delimiter:  field delimiter cannot have more than 1 character");
        }
	}

}
