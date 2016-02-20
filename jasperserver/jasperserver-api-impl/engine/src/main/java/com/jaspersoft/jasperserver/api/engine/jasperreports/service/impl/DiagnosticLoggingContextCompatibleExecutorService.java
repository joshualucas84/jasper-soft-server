/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2009 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;

import com.jaspersoft.jasperserver.api.metadata.common.service.impl.ExecutorServiceWrapper;
import org.apache.log4j.MDC;

import java.util.Hashtable;
import java.util.concurrent.ExecutorService;

/**
 * This class propagates log4j's logging context (MDC) from the invoker thread into thread-pool-thread.
 * Log4j's MDC is a thread-local map which keeps info about UserId or ResourceUri for using in log messages.
 *
 * @author Yakiv Tymoshenko
 * @version $Id: DiagnosticLoggingContextCompatibleExecutorService.java 56967 2015-08-20 23:20:53Z esytnik $
 * @since 13.11.14
 */
class DiagnosticLoggingContextCompatibleExecutorService extends ExecutorServiceWrapper {

    public DiagnosticLoggingContextCompatibleExecutorService(ExecutorService wrappedExecutorService) {
        super(wrappedExecutorService);
    }

    @Override
    // Suppressing type cast warning because log4j MDC.getContext() returns legacy ungenerified  Hashtable.
    @SuppressWarnings("unchecked")
    public void execute(final Runnable command) {
        /*
            MDC.getContext() may be null if MDC.put(String, String) was never called.
            See bug http://bugzilla.jaspersoft.com/show_bug.cgi?id=42523
        */
        if (MDC.getContext() != null) {
            final Hashtable<String, Object> mdcContextCopy = new Hashtable<String, Object>();
            mdcContextCopy.putAll(MDC.getContext());
            getWrappedExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    (MDC.getContext()).putAll(mdcContextCopy);
                    command.run();
                }
            });
        } else {
            getWrappedExecutorService().execute(command);
        }
    }
}
