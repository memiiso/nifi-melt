package com.cumpel.nifi.melt.web.api.ctasexpression.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ValidationDTO implements Serializable {

    private Boolean valid;
    private String message;

    public ValidationDTO() {
    }

    public ValidationDTO(Boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public Boolean isValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
