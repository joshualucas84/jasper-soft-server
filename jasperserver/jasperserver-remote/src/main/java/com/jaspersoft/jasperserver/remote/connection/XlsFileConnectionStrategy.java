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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import com.jaspersoft.jasperserver.dto.connection.XlsFileConnection;
import com.jaspersoft.jasperserver.dto.connection.metadata.XlsFileMetadata;
import com.jaspersoft.jasperserver.dto.connection.metadata.XlsSheet;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id: XlsFileConnectionStrategy.java 54728 2015-04-24 15:28:20Z tdanciu $
 */
@Service
public class XlsFileConnectionStrategy extends AbstractFileConnectionStrategy<XlsFileConnection> {
    @Override
    protected XlsFileConnection cloneConnection(XlsFileConnection connectionDescription) {
        return new XlsFileConnection(connectionDescription);
    }

    @Override
    protected Object internalBuildMetadata(XlsFileConnection connection, InputStream inputStream) throws IOException {
        List<XlsSheet> sheets = new ArrayList<XlsSheet>();

        final HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int s = 0; s < numberOfSheets; s++) {
        	HSSFSheet currentSheet = workbook.getSheetAt(s);
            List<String> columns = new ArrayList<String>();
            final XlsSheet clientSheet = new XlsSheet().setName(currentSheet.getSheetName()).setColumns(columns);
            sheets.add(clientSheet);
            HSSFRow firstRow = currentSheet.getRow(0);
            int numberOfColumns = firstRow.getLastCellNum();
            for (int i = 0; i < numberOfColumns; i++) {
                if (connection.hasHeaderLine()) {
                    columns.add(firstRow.getCell(i).getStringCellValue());
                } else {
                    columns.add(buildColumnName (i));
                }
            }
        }
        
        return new XlsFileMetadata().setSheets(sheets);
    }
}
