package com.jaspersoft.jasperserver.jaxrs.common;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.server.impl.application.ExceptionMapperFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Component
public class JSExceptionWrapperMapper implements ExceptionMapper<JSExceptionWrapper> {

    @Context
    private ProviderServices providerServices;

    @Override
    public Response toResponse(JSExceptionWrapper exception) {
        Exception rootException = getRootException(exception);

        ExceptionMapperFactory factory = new ExceptionMapperFactory();
        factory.init(providerServices);
        ExceptionMapper em = factory.find(rootException.getClass());

        return em.toResponse(rootException);
    }

    private Exception getRootException(JSExceptionWrapper exceptionWrapper) {
        Exception originalException = exceptionWrapper.getOriginalException();

        if (originalException == null) return exceptionWrapper;

        if (originalException instanceof JSExceptionWrapper) {
            return getRootException((JSExceptionWrapper) originalException);
        }

        return originalException;
    }

}
