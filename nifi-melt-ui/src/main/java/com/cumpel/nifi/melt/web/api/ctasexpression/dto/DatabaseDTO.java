package com.cumpel.nifi.melt.web.api.ctasexpression.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class DatabaseDTO {
    @JsonProperty("schemas")
    private ArrayList<SchemaDTO> schemaList;

    public DatabaseDTO() {
        this.schemaList = new ArrayList<>();
    }

    public List<SchemaDTO> getSchemaList() {
        return schemaList;
    }

    public void setSchemaList(ArrayList<SchemaDTO> schemaList) {
        this.schemaList = schemaList;
    }

    public void addSchema(SchemaDTO schema) {
        this.schemaList.add(schema);
    }
}
