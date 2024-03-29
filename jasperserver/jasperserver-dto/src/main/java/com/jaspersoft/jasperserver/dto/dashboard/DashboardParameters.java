package com.jaspersoft.jasperserver.dto.dashboard;

import com.jaspersoft.jasperserver.dto.reports.ReportParameter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>DTO for dashboard execution parameters</p>
 *
 * @author Zakhar Tomchenko
 * @version $Id: $
 */
@XmlRootElement
public class DashboardParameters {

    private List<ReportParameter> dashboardParameters;

    public DashboardParameters() { }

    public DashboardParameters(List<ReportParameter> dashboardParameters) {
        this.dashboardParameters = dashboardParameters;
    }

    public DashboardParameters(DashboardParameters other) {
        final List<ReportParameter> reportParameterList = other.getDashboardParameters();
        if(reportParameterList != null){
            dashboardParameters = new ArrayList<ReportParameter>(other.getDashboardParameters().size());
            for(ReportParameter reportParameter : reportParameterList){
                dashboardParameters.add(new ReportParameter(reportParameter));
            }
        }
    }

    @XmlElement(name = "dashboardParameter")
    public List<ReportParameter> getDashboardParameters() {
        return dashboardParameters;
    }

    public void setDashboardParameters(List<ReportParameter> dashboardParameters) {
        this.dashboardParameters = dashboardParameters;
    }

    @XmlTransient
    public Map<String, String[]> getRawParameters(){
        Map<String, String[]> rawParameters = new HashMap<String, String[]>();
        if(dashboardParameters != null){
            for(ReportParameter currentParameter : dashboardParameters){
                rawParameters.put(
                        currentParameter.getName(),
                        currentParameter.getValues().toArray(new String[currentParameter.getValues().size()])
                );
            }
        }
        return rawParameters;
    }

    @Override
    public String toString() {
        return "DashboardParameters{" +
                "dashboardParameters=" + dashboardParameters +
                '}';
    }
}
