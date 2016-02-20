package com.jaspersoft.jasperserver.dto.dashboard;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>DTO for dashboard execution status.</p>
 *
 * @author Zakhar Tomchenko
 * @version $Id: $
 */
@XmlRootElement(name = "dashboardExportExecutionStatus")
public class DashboardExportExecutionStatus {
    private String id;
    private int progress;
    private Status status;

    public String getId() {
        return id;
    }

    public DashboardExportExecutionStatus setId(String id) {
        this.id = id;
        return this;
    }

    public int getProgress() {
        return progress;
    }

    public DashboardExportExecutionStatus setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public DashboardExportExecutionStatus setStatus(Status status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardExportExecutionStatus that = (DashboardExportExecutionStatus) o;

        if (progress != that.progress) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return status == that.status;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + progress;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DashboardExportExecutionStatus{" +
                "id='" + id + '\'' +
                ", progress=" + progress +
                ", status=" + status +
                '}';
    }

    public enum Status {
        execution, ready, failed, cancelled
    }

}
