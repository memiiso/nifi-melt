package com.cumpel.nifi.melt.web.api.processor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import org.apache.nifi.web.ComponentDetails;
import org.apache.nifi.web.HttpServletConfigurationRequestContext;
import org.apache.nifi.web.HttpServletRequestContext;
import org.apache.nifi.web.NiFiWebConfigurationContext;
import org.apache.nifi.web.NiFiWebConfigurationRequestContext;
import org.apache.nifi.web.NiFiWebRequestContext;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.UiExtensionType;


class ProcessorWebUtils {

    static ComponentDetails getComponentDetails(final NiFiWebConfigurationContext configurationContext, final String processorId,
                                                final Long revision, final String clientId, HttpServletRequest request) {

        final NiFiWebRequestContext requestContext;

        if (processorId != null && revision != null && clientId != null) {
            requestContext = getRequestContext(processorId, revision, clientId, request);
        } else {
            requestContext = getRequestContext(processorId, request);
        }

        return configurationContext.getComponentDetails(requestContext);

    }

    static ComponentDetails getComponentDetails(final NiFiWebConfigurationContext configurationContext, final String processorId, HttpServletRequest request) {
        return getComponentDetails(configurationContext, processorId, null, null, request);
    }

    static Response.ResponseBuilder applyCacheControl(Response.ResponseBuilder response) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        return response.cacheControl(cacheControl);
    }

    static NiFiWebConfigurationRequestContext getRequestContext(final String processorId, final Long revision, final String clientId, HttpServletRequest request) {
        return new HttpServletConfigurationRequestContext(UiExtensionType.ProcessorConfiguration, request) {
            @Override
            public String getId() {
                return processorId;
            }

            @Override
            public Revision getRevision() {
                return new Revision(revision, clientId, processorId);
            }
        };
    }


    private static NiFiWebRequestContext getRequestContext(final String processorId, HttpServletRequest request) {
        return new HttpServletRequestContext(UiExtensionType.ProcessorConfiguration, request) {
            @Override
            public String getId() {
                return processorId;
            }
        };
    }


}
