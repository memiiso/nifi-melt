package com.cumpel.nifi.melt.web.api.sqlexpression.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class TableDTO {

    @JsonProperty("name")
    private String tableName;

    @JsonProperty("columns")
    private List<ColumnDTO> columnList;

    public TableDTO() {
    }

    public TableDTO(String tableName) {
        this.tableName = tableName;
        this.columnList = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColumnList(List<ColumnDTO> columnList) {
        this.columnList = columnList;
    }

    public void addColumn(ColumnDTO column) {
        this.columnList.add(column);
    }
}
