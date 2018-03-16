/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.web.api.processors;

import com.memiiso.nifi.melt.web.api.standard.AbstractStandardResource;
import org.apache.commons.text.StringSubstitutor;
import org.apache.nifi.web.ComponentDetails;
import org.apache.nifi.web.NiFiWebConfigurationContext;
import org.apache.nifi.web.NiFiWebConfigurationRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/ctas")
public class CTAS extends AbstractStandardResource {

    private static final Logger logger = LoggerFactory.getLogger(CTAS.class);

    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("/setproperties")
    public Response setProperties(@QueryParam("processorId") final String processorId, @QueryParam("revisionId") final Long revisionId,
                                  @QueryParam("clientId") final String clientId, Map<String, String> properties) {
        // get the web context
        final NiFiWebConfigurationContext nifiWebContext = getWebConfigurationContext();
        final NiFiWebConfigurationRequestContext niFiRequestContext = this.getRequestContext(processorId, revisionId, clientId, request);

        Map<String, String> properties1 = nifiWebContext.getComponentDetails(niFiRequestContext).getProperties();

        String melt_elt_template = properties1.get("melt_elt_template");
        properties1.remove("melt_elt_template");

        StringSubstitutor sub = new StringSubstitutor(properties1);
        String melt_elt_statement = sub.replace(melt_elt_template);
        properties.put("melt_elt_statement",melt_elt_statement);

        final ComponentDetails componentDetails = nifiWebContext.updateComponent(niFiRequestContext, null, properties);
        final Response.ResponseBuilder response = applyCacheControl(Response.ok(componentDetails));
        return response.build();
    }

}
