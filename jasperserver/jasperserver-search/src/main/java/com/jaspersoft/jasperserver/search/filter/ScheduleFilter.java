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

package com.jaspersoft.jasperserver.search.filter;

import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobSummary;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportJobsPersistenceService;
import com.jaspersoft.jasperserver.api.search.SearchCriteria;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.engine.scheduling.hibernate.PersistentReportJob;
import com.jaspersoft.jasperserver.search.common.SearchAttributes;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.transformedCollection;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Scheduler filter.
 *
 * @author Yuriy Plakosh
 * @version $Id$
 */
public class ScheduleFilter extends BaseSearchFilter implements Serializable {

    protected ReportJobsPersistenceService jobsService;

    public void applyRestrictions(String type, ExecutionContext context, SearchCriteria criteria) {
        SearchAttributes searchAttributes = getSearchAttributes(context);
        User user = null;

        if (context.getAttributes() != null) {
            for(Object o : context.getAttributes()) {
                if (o instanceof User) {
                    user = (User) o;
                }
            }
        }

        if (searchAttributes != null && searchAttributes.getState() != null) {
            String scheduleFilter = searchAttributes.getState().getCustomFiltersMap().get("scheduleFilter");
            if (scheduleFilter != null && !scheduleFilter.equals("scheduleFilter-anySchedule")) {
                if (scheduleFilter.equals("scheduleFilter-scheduled")) {
                    createCriteria(criteria, user, true, false);
                } else if (scheduleFilter.equals("scheduleFilter-scheduledByMe")) {
                    createCriteria(criteria, user, true, true);
                } else if (scheduleFilter.equals("scheduleFilter-notScheduled")) {
                    createCriteria(criteria, user, false, false);
                } 
            }
        }
    }

    private void createCriteria(SearchCriteria criteria, User user, boolean scheduled, boolean scheduledByUser) {
        List<ReportJobSummary> jobSummaries = jobsService.listJobs(null);

        if (!jobSummaries.isEmpty()) {
            SearchCriteria idCriteria = SearchCriteria.forClass(RepoResource.class);

            Disjunction disjunction = Restrictions.disjunction();
            String alias = idCriteria.getAlias("parent", "p");

            for (ReportJobSummary job : jobSummaries) {
                String uri = job.getReportUnitURI();

                disjunction.add(getResourceCriterion(alias, uri));
            }

            idCriteria.add(disjunction);
            idCriteria.addProjection(Projections.id());

            List idList = getHibernateTemplate().findByCriteria(idCriteria);

            if (!idList.isEmpty()) {
                if (scheduled) {
                    criteria.add(Restrictions.in("id", idList));
                } else {
                    criteria.add(Restrictions.not(Restrictions.in("id", idList)));
                }
            } else {
                throw new RuntimeException("No resources found for URI list " +
                        transformedCollection(jobSummaries, new Transformer() {
                            @Override
                            public Object transform(Object o) {
                                return ((ReportJobSummary) o).getReportUnitURI();
                            }
                        }));
            }
        } else {
             if (scheduled || scheduledByUser) {
                 criteria.add(Restrictions.isNull("id"));
             }
        }
    }

    /**
     * Deprecated because we replaced our custom logic with existing service
     * {@link ReportJobsPersistenceService#listJobs(com.jaspersoft.jasperserver.api.common.domain.ExecutionContext)}.
     * Which has ACL check enabled.
     *
     * {@see http://bugzilla.jaspersoft.com/show_bug.cgi?id=41783}
     *
     * @param criteria
     * @param user
     */
    @Deprecated
    protected void addOwnerCriteria(SearchCriteria criteria, User user) {
        String alias = criteria.getAlias("owner", "o");
        criteria.add(Restrictions.eq(alias + ".username", user.getUsername()));
    }

    private Criterion getResourceCriterion(String alias, String uri) {
        int lastSlashPos = uri.lastIndexOf(Folder.SEPARATOR);

        String folderUri = uri.substring(0, lastSlashPos);
        String resourceName = uri.substring(lastSlashPos + 1);

        if (folderUri.length() == 0) {
            folderUri = Folder.SEPARATOR;
        }

        return Restrictions.and(Restrictions.eq("name", resourceName), Restrictions.eq(alias + ".URI", folderUri));
    }

    public void setJobsService(ReportJobsPersistenceService jobsService) {
        this.jobsService = jobsService;
    }
}
