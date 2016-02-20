package com.jaspersoft.jasperserver.dto.dashboard;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * <p>DTO for dashboard executions.</p>
 *
 * @author Zakhar Tomchenko
 * @version $Id: $
 */
@XmlRootElement(name = "dashboardExportExecutions")
public class DashboardExportExecutionListWrapper {
    private List<DashboardExportExecution> executions;

    @XmlElement(name = "dashboardExportExecution")
    public List<DashboardExportExecution> getExecutions() {
        return executions;
    }

    public DashboardExportExecutionListWrapper setExecutions(List<DashboardExportExecution> executions) {
        this.executions = executions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardExportExecutionListWrapper that = (DashboardExportExecutionListWrapper) o;

        return !(executions != null ? !executions.equals(that.executions) : that.executions != null);

    }

    @Override
    public int hashCode() {
        return executions != null ? executions.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DashboardReportExecutionListWrapper{" +
                "executions=" + executions +
                '}';
    }
}

