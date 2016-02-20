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
package com.jaspersoft.jasperserver.api.metadata.common.service.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author Yakiv Tymoshenko
 * @version $Id: ExecutorServiceWrapper.java 56967 2015-08-20 23:20:53Z esytnik $
 * @since 25.06.2015
 */
public abstract class ExecutorServiceWrapper implements Executor {

    private ExecutorService wrappedExecutorService;

    public ExecutorServiceWrapper(ExecutorService wrappedExecutorService) {
        this.wrappedExecutorService = wrappedExecutorService;
    }

    /*
    Must be overridden to pass a thread-local context from the main thread into a thread-pool-thread.
     */
    @Override
    public abstract void execute(Runnable command);

    public ExecutorService getWrappedExecutorService() {
        return wrappedExecutorService;
    }
}
