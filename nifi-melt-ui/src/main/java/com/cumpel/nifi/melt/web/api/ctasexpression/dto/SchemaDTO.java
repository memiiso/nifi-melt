package com.cumpel.nifi.melt.web.api.ctasexpression.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchemaDTO {

    @JsonProperty("name")
    private String schemaName;

    @JsonProperty("tables")
    private ArrayList<TableDTO> tableList;

    public SchemaDTO() {
    }

    public SchemaDTO(String schemaName) {
        this.schemaName = schemaName;
        this.tableList = new ArrayList<>();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setTableList(ArrayList<TableDTO> tableList) {
        this.tableList = tableList;
    }

    public void addTable(TableDTO table) {
        this.tableList.add(table);
    }
}
