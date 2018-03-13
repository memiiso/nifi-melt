package com.cumpel.nifi.melt.web.api.ctasexpression.dto;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JoltSpecificationDTO implements Serializable{


    private String transform;
    private String specification;
    private String input;
    private String modules;
    private String customClass;
    private Map<String,String> expressionLanguageAttributes;

    public JoltSpecificationDTO() {
    }

    public JoltSpecificationDTO(String transform, String specification) {
        this.transform = transform;
        this.specification = specification;
    }

    public String getTransform() {
        return transform;
    }

    public void setTransform(String transform) {
        this.transform = transform;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getModules() {
        return modules;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }

    public String getCustomClass() {
        return customClass;
    }

    public void setCustomClass(String customClass) {
        this.customClass = customClass;
    }

    public Map<String, String> getExpressionLanguageAttributes() {
        return expressionLanguageAttributes;
    }

    public void setExpressionLanguageAttributes(Map<String, String> expressionLanguageAttributes) {
        this.expressionLanguageAttributes = expressionLanguageAttributes;
    }
}
