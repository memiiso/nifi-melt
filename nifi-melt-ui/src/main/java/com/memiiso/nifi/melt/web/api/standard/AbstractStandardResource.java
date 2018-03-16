/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.web.api.standard;

import com.memiiso.nifi.melt.db.MeltDBConnection;
import com.memiiso.nifi.melt.dbcp.MeltDBCPService;
import org.apache.nifi.web.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


public abstract class AbstractStandardResource {

    @Context
    protected ServletContext servletContext;

    @Context
    protected HttpServletRequest request;


    protected NiFiWebConfigurationContext getWebConfigurationContext() {
        return (NiFiWebConfigurationContext) servletContext.getAttribute("nifi-web-configuration-context");
    }

    // TODO review
    protected ComponentDetails getComponentDetails(final NiFiWebConfigurationContext configurationContext, final String processorId,
                                                   final Long revision, final String clientId, HttpServletRequest request) {

        final NiFiWebRequestContext requestContext;

        if (processorId != null && revision != null && clientId != null) {
            requestContext = getRequestContext(processorId, revision, clientId, request);
        } else {
            requestContext = getRequestContext(processorId, request);
        }

        return configurationContext.getComponentDetails(requestContext);

    }

    protected ComponentDetails getComponentDetails(final NiFiWebConfigurationContext configurationContext, final String processorId, HttpServletRequest request) {
        return getComponentDetails(configurationContext, processorId, null, null, request);
    }

    protected static Response.ResponseBuilder applyCacheControl(Response.ResponseBuilder response) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        return response.cacheControl(cacheControl);
    }

    protected NiFiWebConfigurationRequestContext getRequestContext(final String processorId, final Long revision, final String clientId, HttpServletRequest request) {
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


    protected static NiFiWebRequestContext getRequestContext(final String processorId, HttpServletRequest request) {
        return new HttpServletRequestContext(UiExtensionType.ProcessorConfiguration, request) {
            @Override
            public String getId() {
                return processorId;
            }
        };
    }

    protected MeltDBConnection getMeltDBConnection(String processorId){
        final NiFiWebConfigurationContext nifiWebContext = getWebConfigurationContext();
        final ComponentDetails componentDetails = this.getComponentDetails(nifiWebContext, processorId, request);
        MeltDBCPService cs = (MeltDBCPService) this.getWebConfigurationContext().getControllerService(componentDetails.getProperties().get("melt_dbcp_service"), processorId);
        return cs.getMeltDBConnection();
    }

}
