package com.jaspersoft.jasperserver.api.engine.scheduling.security;

import com.jaspersoft.jasperserver.api.engine.scheduling.ReportJobsInternalService;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJob;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobIdHolder;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSummary;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InternalURI;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.InternalURIDefinition;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.ResourceObjectIdentityRetrievalStrategyImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.acls.model.ObjectIdentity;

/**
 * @author Oleg.Gavavka
 */

public class ReportJobResourceObjectIdentityRetrievalStrategy extends ResourceObjectIdentityRetrievalStrategyImpl {
    private static final Log log = LogFactory.getLog(ReportJobResourceObjectIdentityRetrievalStrategy.class);

    private ReportJobsInternalService reportJobsInternalService;

    @Override
    public ObjectIdentity getObjectIdentity(Object domainObject) {
        if (domainObject instanceof ReportJob) {
            String resourceUri = ((ReportJob) domainObject).getSource().getReportUnitURI();
            InternalURI internalURI = new InternalURIDefinition(resourceUri);
            return internalURI;

        }
        if (domainObject instanceof ReportJobIdHolder) {
            Long jobId = ((ReportJobIdHolder) domainObject).getId();
            String resourceUri = getReportJobsInternalService().getSourceUriByJobId(jobId);
            InternalURI internalURI = new InternalURIDefinition(resourceUri);
            return internalURI;
        }

        if (domainObject instanceof ReportJobSummary) {
            String resourceUri = ((ReportJobSummary) domainObject).getReportUnitURI();
            InternalURI internalURI = new InternalURIDefinition(resourceUri);
            return internalURI;
        }
        return super.getObjectIdentity(domainObject);
    }

    public ReportJobsInternalService getReportJobsInternalService() {
        return reportJobsInternalService;
    }

    public void setReportJobsInternalService(ReportJobsInternalService reportJobsInternalService) {
        this.reportJobsInternalService = reportJobsInternalService;
    }
}
