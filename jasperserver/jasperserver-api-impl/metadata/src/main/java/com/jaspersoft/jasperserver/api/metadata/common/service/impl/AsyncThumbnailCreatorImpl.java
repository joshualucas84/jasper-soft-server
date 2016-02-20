package com.jaspersoft.jasperserver.api.metadata.common.service.impl;

import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.ThumbnailGenerationService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.HibernateRepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.ReportThumbnailService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of {@link AsyncThumbnailCreator}
 *
 * @author Grant Bacon (gbacon@jaspersoft.com)
 * @version
 */
public class AsyncThumbnailCreatorImpl implements AsyncThumbnailCreator {

    private ThumbnailGenerationService generatorService;
    private ReportThumbnailService storageService;
    private HibernateRepositoryService repositoryService;

    protected static final Log log = LogFactory.getLog(AsyncThumbnailCreatorImpl.class);

    private int threadsMax;
    private ExecutorService threadPool;

    protected class ThumbnailRunnable implements Runnable {

        private JasperPrintAccessor accessor;
        private JasperReportsContext reportContext;
        private User user;
        private Resource resource;

        public ThumbnailRunnable(JasperPrintAccessor accessor, JasperReportsContext reportContext, User user, Resource resource)
        {
            this.accessor = accessor;
            this.reportContext = reportContext;
            this.user = user;
            this.resource = resource;
        }

        @Override
        public void run() {
            try {
                JasperPrint jasperPrint = accessor.getFinalJasperPrint(); // this call blocks waiting for final version of page
                ByteArrayOutputStream thumbnail = (ByteArrayOutputStream) generatorService.createThumbnail(jasperPrint, reportContext);

                storageService.saveReportThumbnail(thumbnail, user, resource);
            } catch (Exception e) {
                log.error(e, e);
            }
        }

    }

    public AsyncThumbnailCreatorImpl() {}

    private void createThreadPool() {
        this.threadPool = Executors.newFixedThreadPool(threadsMax);
    }

    public void createAndStoreThumbnail(JasperPrintAccessor jpAccessor, JasperReportsContext reportContext, User user, Resource resource)
    {
        if (this.threadPool == null) {
            createThreadPool();
        }
        threadPool.submit(new ThumbnailRunnable(jpAccessor, reportContext, user, resource));
    }

    public ThumbnailGenerationService getGeneratorService() {
        return generatorService;
    }

    public void setGeneratorService(ThumbnailGenerationService generatorService) {
        this.generatorService = generatorService;
    }

    public ReportThumbnailService getStorageService() {
        return storageService;
    }

    public void setStorageService(ReportThumbnailService storageService) {
        this.storageService = storageService;
    }

    public int getThreadsMax() {
        return threadsMax;
    }

    public void setThreadsMax(int threadsMax) {
        this.threadsMax = threadsMax;
    }

    public HibernateRepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(HibernateRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
}
