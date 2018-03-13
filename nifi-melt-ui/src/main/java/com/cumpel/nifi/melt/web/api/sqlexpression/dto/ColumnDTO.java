package com.cumpel.nifi.melt.web.api.sqlexpression.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnDTO {

    @JsonProperty("name")
    private String columnName;

    @JsonProperty("type")
    private String columnType;

    public ColumnDTO(){}

    public ColumnDTO(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }
}
