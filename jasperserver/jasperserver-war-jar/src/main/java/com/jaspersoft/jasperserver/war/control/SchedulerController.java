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
package com.jaspersoft.jasperserver.war.control;

import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import com.jaspersoft.jasperserver.api.common.util.TimeZonesList;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportSchedulingService;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.war.common.JasperServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chaim Arbiv
 * @version $id$
 * Controller to handle all the scheduling interaction.
 * It should not depend to Web-Flow and use only the REST v2 end points.
 */
public class SchedulerController extends MultiActionController {

    private static final Log log = LogFactory.getLog(SchedulerController.class);

    public static final String REPORT_PARAMETER_NAME = "reportUnitURI";
    public static final String PARENT_REPORT_PARAMETER_NAME = "parentReportUnitURI";

    private TimeZonesList timezones;
    private ReportSchedulingService scheduler;

    @Autowired(required=true)
    private HttpServletRequest request;

    private RepositoryService repository;

    private boolean enableSaveToHostFS;

    private static final String LICENSE_MANAGER = "com.jaspersoft.ji.license.LicenseManager";
    private String enableDataSnapshot;


    public ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("modules/reportScheduling/main");
        mav.addObject("timezone", TimeZoneContextHolder.getTimeZone().getID());
        mav.addObject("isPro", isProVersion());
        mav.addObject("userTimezones", timezones.getTimeZones(request.getLocale()));

        mav.addObject("enableSaveToHostFS", getEnableSaveToHostFS());
        mav.addObject("enableDataSnapshot", getEnableDataSnapshot());
        mav.addObject("controlsDisplayForm", getParametersForm(getReportUri(request)));

        return mav;
    }

    private String getReportUri(HttpServletRequest request){
        String reportURI = request.getParameter(PARENT_REPORT_PARAMETER_NAME);

        if (reportURI == null || reportURI.isEmpty()) {
            reportURI = request.getParameter(REPORT_PARAMETER_NAME);
        }

        return reportURI;
    }

    private String getParametersForm(String reportUnitUri) {
        if (reportUnitUri == null) {
            return null;
        }

        ReportUnit reportUnit = (ReportUnit) repository.getResource(JasperServerUtil.getExecutionContext(), reportUnitUri);
        return reportUnit.getInputControlRenderingView();
    }

    public ReportSchedulingService getScheduler() {
        return scheduler;
    }

    public void setScheduler(ReportSchedulingService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Helper method to determine if we are running the Pro or Enterprise edition
     * @return boolean indicating success..
     */
    public boolean isProVersion(){
        boolean isPro = false;
        try {
            Class clazz = Class.forName(LICENSE_MANAGER);
            if(clazz != null){
                isPro = true;
            }
        } catch (ClassNotFoundException e) {
            if(log.isDebugEnabled()){
                log.info("This is not a pro version. Access is denied");
            }
        }
        return isPro;
    }

    public TimeZonesList getTimezones()
    {
        return timezones;
    }

    public void setTimezones(TimeZonesList timezones)
    {
        this.timezones = timezones;
    }

    public String getEnableSaveToHostFS() {
        return Boolean.toString(isEnableSaveToHostFS());
    }

    public boolean isEnableSaveToHostFS() {
        return enableSaveToHostFS;
    }

    public void setEnableSaveToHostFS(boolean enableSaveToHostFS) {
        this.enableSaveToHostFS = enableSaveToHostFS;
    }

    public void setEnableDataSnapshot(String enableDataSnapshot) {
        this.enableDataSnapshot = enableDataSnapshot;
    }

    public String getEnableDataSnapshot() {
        return enableDataSnapshot;
    }

    public void setRepository(RepositoryService repository) {
        this.repository = repository;
    }
}
