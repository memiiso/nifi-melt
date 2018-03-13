package com.cumpel.nifi.melt.web.api.ctasexpression;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cumpel.nifi.melt.web.api.ctasexpression.dto.ColumnDTO;
import com.cumpel.nifi.melt.web.api.ctasexpression.dto.DatabaseDTO;
import com.cumpel.nifi.melt.web.api.ctasexpression.dto.SchemaDTO;
import com.cumpel.nifi.melt.web.api.ctasexpression.dto.TableDTO;

@Path("/melt")
public class CtasDatabaseInformation {

    private static final Logger logger = LoggerFactory.getLogger(CtasDatabaseInformation.class);

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/ctas/database")
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

        return Response.ok(databaseDTO).build();
    }

}
