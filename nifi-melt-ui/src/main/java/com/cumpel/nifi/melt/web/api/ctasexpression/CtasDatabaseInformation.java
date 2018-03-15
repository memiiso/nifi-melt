package com.cumpel.nifi.melt.web.api.ctasexpression;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cumpel.nifi.melt.web.api.ctasexpression.dto.ColumnDTO;
import com.cumpel.nifi.melt.web.api.ctasexpression.dto.DatabaseDTO;
import com.cumpel.nifi.melt.web.api.ctasexpression.dto.SchemaDTO;
import com.cumpel.nifi.melt.web.api.ctasexpression.dto.TableDTO;

@Path("/melt")
public class CtasDatabaseInformation {

    private static final Logger logger = LoggerFactory.getLogger(CtasDatabaseInformation.class);
    private static final int TABLE_COUNT = 18;
    private static final int COLUMN_COUNT = 28;
    private static final int SCHEMA_COUNT = 5;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/ctas/databases")
    public Response getDatabaseInformation() {

        logger.info("calling get db infos!");

        // TODO: Replace this code and get informations from the connected database pool
        DatabaseDTO databaseDTO = new DatabaseDTO();
        SchemaDTO schemaDTO = new SchemaDTO("emp_service");
        TableDTO tableDTO = new TableDTO("employee");
        ColumnDTO idColumn = new ColumnDTO("id", "int(11) AI PK");
        ColumnDTO nameColumn = new ColumnDTO("name", "varchar(20)");
        ColumnDTO depthColumn = new ColumnDTO("depth", "varchar(10)");
        ColumnDTO salaryColumn = new ColumnDTO("salary", "int(10)");
        tableDTO.addColumn(idColumn);
        tableDTO.addColumn(nameColumn);
        tableDTO.addColumn(depthColumn);
        tableDTO.addColumn(salaryColumn);
        schemaDTO.addTable(tableDTO);
        databaseDTO.addSchema(schemaDTO);

        for (int i = 0; i < SCHEMA_COUNT; i++) {
            schemaDTO = new SchemaDTO(RandomStringUtils.randomAlphabetic(10,55));

            TableDTO tableDTO1;
            for (int j = 0; j < TABLE_COUNT ; j++) {
                tableDTO1 = new TableDTO(RandomStringUtils.randomAlphabetic(10,75));

                ColumnDTO columnDTO;
                for (int k = 0; k < COLUMN_COUNT; k++) {
                    columnDTO  = new ColumnDTO(RandomStringUtils.randomAlphabetic(2,20), RandomStringUtils.randomAlphabetic(6,40));
                    tableDTO1.addColumn(columnDTO);
                }
                schemaDTO.addTable(tableDTO1);
            }
            databaseDTO.addSchema(schemaDTO);
        }


        // generate the response
        final Response.ResponseBuilder response = Response.ok(databaseDTO);
        return noCache(response).build();
    }

    private Response.ResponseBuilder noCache(Response.ResponseBuilder response) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        return response.cacheControl(cacheControl);
    }

}
