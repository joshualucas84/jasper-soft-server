package com.jaspersoft.jasperserver.dto.dashboard;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>DTO for dashboard executions.</p>
 *
 * @author Zakhar Tomchenko
 * @version $Id: $
 */
@XmlRootElement(name = "dashboardExportExecution")
public class DashboardExportExecution {
    private int width;
    private int height;
    private ExportFormat format;
    private String uri;
    private String id;
    private DashboardParameters parameters;
    private String markup;
    private List<String> jrStyle;

    public int getWidth() {
        return width;
    }

    public DashboardExportExecution setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public DashboardExportExecution setHeight(int heigt) {
        this.height = heigt;
        return this;
    }

    public ExportFormat getFormat() {
        return format;
    }

    public DashboardExportExecution setFormat(ExportFormat format) {
        this.format = format;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public DashboardExportExecution setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public DashboardParameters getParameters() {
        return parameters;
    }

    public DashboardExportExecution setParameters(DashboardParameters parameters) {
        this.parameters = parameters;
        return this;
    }

    public String getId() {
        return id;
    }

    public DashboardExportExecution setId(String id) {
        this.id = id;
        return this;
    }

    public String getMarkup() {
        return markup;
    }

    public DashboardExportExecution setMarkup(String markup) {
        this.markup = markup;
        return this;
    }

    public List<String> getJrStyle() {
        return jrStyle;
    }

    public DashboardExportExecution setJrStyle(List<String> jrStyle) {
        this.jrStyle = jrStyle;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardExportExecution execution = (DashboardExportExecution) o;

        if (width != execution.width) return false;
        if (height != execution.height) return false;
        if (format != execution.format) return false;
        if (uri != null ? !uri.equals(execution.uri) : execution.uri != null) return false;
        if (id != null ? !id.equals(execution.id) : execution.id != null) return false;
        if (parameters != null ? !parameters.equals(execution.parameters) : execution.parameters != null) return false;
        if (markup != null ? !markup.equals(execution.markup) : execution.markup != null) return false;
        return !(jrStyle != null ? !jrStyle.equals(execution.jrStyle) : execution.jrStyle != null);

    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (markup != null ? markup.hashCode() : 0);
        result = 31 * result + (jrStyle != null ? jrStyle.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DashboardExportExecution{" +
                "width=" + width +
                ", height=" + height +
                ", format=" + format +
                ", uri='" + uri + '\'' +
                ", id='" + id + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    public enum ExportFormat {
        png("image/png"),
        docx("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        odt("application/vnd.oasis.opendocument.text"),
        pdf("application/pdf");

        private String mime;
        ExportFormat(String mime){
            this.mime = mime;
        }

        public String getMimeFormat(){
            return mime;
        }
    }
}
