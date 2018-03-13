package com.cumpel.nifi.melt.web.api.processor;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.nifi.web.ComponentDetails;
import org.apache.nifi.web.NiFiWebConfigurationContext;
import org.apache.nifi.web.NiFiWebConfigurationRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cumpel.nifi.melt.web.api.AbstractStandardResource;

@Path("/standard/processor")
public class ProcessorResource extends AbstractStandardResource {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorResource.class);

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/details")
    public Response getDetails(@QueryParam("processorId") final String processorId) {
        final NiFiWebConfigurationContext nifiWebContext = getWebConfigurationContext();
        final ComponentDetails componentDetails = ProcessorWebUtils.getComponentDetails(nifiWebContext, processorId, request);
        final Response.ResponseBuilder response = ProcessorWebUtils.applyCacheControl(Response.ok(componentDetails));


        return response.build();
    }

    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("/properties")
    public Response setProperties(@QueryParam("processorId") final String processorId, @QueryParam("revisionId") final Long revisionId,
            @QueryParam("clientId") final String clientId, Map<String, String> properties) {
        final NiFiWebConfigurationContext nifiWebContext = getWebConfigurationContext();
        final NiFiWebConfigurationRequestContext niFiRequestContext = ProcessorWebUtils.getRequestContext(processorId, revisionId, clientId, request);
        final ComponentDetails componentDetails = nifiWebContext.updateComponent(niFiRequestContext, null, properties);
        final Response.ResponseBuilder response = ProcessorWebUtils.applyCacheControl(Response.ok(componentDetails));
        return response.build();
    }

}
