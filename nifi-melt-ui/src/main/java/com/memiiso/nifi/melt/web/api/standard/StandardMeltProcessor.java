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
import com.memiiso.nifi.melt.db.objects.Database;
import com.memiiso.nifi.melt.db.objects.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.nifi.web.ComponentDetails;
import org.apache.nifi.web.NiFiWebConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/standard/processor")
public class StandardMeltProcessor extends AbstractStandardResource {

    private static final Logger logger = LoggerFactory.getLogger(StandardMeltProcessor.class);

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/details")
    public Response getDetails(@QueryParam("processorId") final String processorId) {
        // get the web context
        final NiFiWebConfigurationContext nifiWebContext = getWebConfigurationContext();
        // load the processor configuration
        final ComponentDetails componentDetails = this.getComponentDetails(nifiWebContext, processorId, request);

        final Response.ResponseBuilder response = applyCacheControl(Response.ok(componentDetails));
        return response.build();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/databaseInformation")
    public Response getDatabaseInformation(@QueryParam("processorId") final String processorId) {
        logger.info("getting db info for processor["+processorId+"]");

        Database database ;
        try {
            final MeltDBConnection meltDBConnection = this.getMeltDBConnection(processorId);
            database = meltDBConnection.getDatabaseInformation();
            logger.info(new ObjectMapper().writeValueAsString(database));
        } catch (Exception e){
            logger.error(e.getMessage());
            database = new Database();
            Schema schema = new Schema("Error! "+e.getMessage());
            database.addSchema(schema);
        }

        final Response.ResponseBuilder response = Response.ok(database);
        return applyCacheControl(response).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    //@Consumes({MediaType.APPLICATION_JSON})
    @Path("/validateSelect")
    public Response validateSelect(@QueryParam("processorId") final String processorId, Map<String, String> properties) {
        String selectStatement = properties.get("melt_source");

        logger.info("running validate for processor["+processorId+"]"+selectStatement);
        String validationMessage;

        if (selectStatement==null || selectStatement.isEmpty()){
            validationMessage = "Select Statement is empty!";
        } else {
            try {
                final MeltDBConnection meltDBConnection = this.getMeltDBConnection(processorId);
                validationMessage = meltDBConnection.validateQuery(selectStatement);
            } catch (Exception e) {
                logger.error(e.getMessage());
                validationMessage = e.getMessage();
            }
        }
        final Response.ResponseBuilder response = Response.ok(new ResponseMessage(true,validationMessage));
        return applyCacheControl(response).build();
    }
}
