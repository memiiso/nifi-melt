/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.processors;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@CapabilityDescription("Execute provided SQL select query and creates table with output. This processor can be scheduled to run on "
        + "a timer, or cron expression, using the standard scheduling methods, or it can be triggered by an incoming FlowFile. ")
public class CTAS extends AbstractMeltProcessor {

    public static final PropertyDescriptor MELT_TEMPLATE = new PropertyDescriptor.Builder()
            .name("melt_elt_template")
            .displayName("ELT Template.")
            .description("${melt_target_table} is the Table Name and ${melt_source} is SQL Select StatEment.")
            .defaultValue("DROP TABLE IF EXISTS ${melt_target_table};\nCREATE TABLE ${melt_target_table} AS \n ${melt_source}")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();

    public CTAS() {
        this.setRelationships();
        ConcurrentHashMap<String, PropertyDescriptor> props = this.getMeltCommonPropertyDescriptors();
        // replace super property.
        props.remove("melt_elt_template");
        props.put(MELT_TEMPLATE.getName(),MELT_TEMPLATE);
        final List<PropertyDescriptor> pds = new ArrayList<>(props.values());
        propDescriptors = Collections.unmodifiableList(pds);
    }
}
