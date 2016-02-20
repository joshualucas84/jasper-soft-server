package com.jaspersoft.jasperserver.api.metadata.olap.service;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link XMLATestResult} class.
 *
 * @author Andrey Kasych
 */
public class XMLATestResultTest extends UnitilsJUnit4 {

    @Test
    public void obfuscatePasswordInErrorMessage() {

        XMLATestResult xmlaTestResult;
        String message;
        String expectedResult;

        message = "java.io.FileNotFoundException: http://jasperadmin|organization_1:my_password@localhost:8080/jasperserver-pro/xmla";
        expectedResult = "java.io.FileNotFoundException: http://jasperadmin|organization_1:********@localhost:8080/jasperserver-pro/xmla";
        xmlaTestResult = new XMLATestResult(XMLATestResult.XMLATestCode.BAD_CREDENTIALS, new ErrorDescriptor().setMessage(message));
        assertEquals("Password exposed in error message", expectedResult, xmlaTestResult.getMessage());

        message = "https://jasperadmin:my@password@jaspersoft.com/~js-pro-instance/xmla";
        expectedResult = "https://jasperadmin:********@jaspersoft.com/~js-pro-instance/xmla";
        xmlaTestResult = new XMLATestResult(XMLATestResult.XMLATestCode.BAD_CREDENTIALS, new ErrorDescriptor().setMessage(message));
        assertEquals("Password exposed in error message", expectedResult, xmlaTestResult.getMessage());
    }
}
