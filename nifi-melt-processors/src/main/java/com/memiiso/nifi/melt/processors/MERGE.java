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

@CapabilityDescription("Executes MERGE statement using provided SQL select query configuration. This processor can be scheduled to run on "
        + "a timer, or cron expression, using the standard scheduling methods, or it can be triggered by an incoming FlowFile. ")
public class MERGE extends AbstractMeltProcessor {

    public static final PropertyDescriptor MELT_TEMPLATE = new PropertyDescriptor.Builder()
            .name("melt_elt_template")
            .displayName("ELT Template.")
            .description( "Merge statement template")
            .defaultValue("MERGE INTO ${melt_target_table} t USING (${melt_source}) d \n" +
                    "ON ${melt_merge_join_condition} \n" +
                    "WHEN MATCHED ${merge_update_filter} THEN UPDATE SET ${melt_merge_update_column_list}  \n" +
                    "WHEN NOT MATCHED ${merge_insert_filter} THEN INSERT ${melt_merge_insert_column_list}  \n" +
                    "VALUES ${melt_merge_insert_column_values}; \n")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();
    public static final PropertyDescriptor MELT_MERGE_JOIN_CONDITION = new PropertyDescriptor.Builder()
            .name("melt_merge_join_condition")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();
    public static final PropertyDescriptor MELT_MERGE_UPDATE_FILTER = new PropertyDescriptor.Builder()
            .name("melt_merge_update_filter")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();
    public static final PropertyDescriptor MELT_MERGE_UPDATE_COLUMN_LIST = new PropertyDescriptor.Builder()
            .name("melt_merge_update_column_list")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();
    public static final PropertyDescriptor MELT_MERGE_INSERT_FILTER = new PropertyDescriptor.Builder()
            .name("melt_merge_insert_filter")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();
    public static final PropertyDescriptor MELT_MERGE_INSERT_COLUMN_LIST = new PropertyDescriptor.Builder()
            .name("melt_merge_insert_column_list")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();
    public static final PropertyDescriptor MELT_MERGE_INSERT_COLUMN_VALUES = new PropertyDescriptor.Builder()
            .name("melt_merge_insert_column_values")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();

    public MERGE() {
        this.setRelationships();
        ConcurrentHashMap<String, PropertyDescriptor> props = this.getMeltCommonPropertyDescriptors();
        // replace super property.
        props.remove("melt_elt_template");
        props.put(MELT_TEMPLATE.getName(),MELT_TEMPLATE);
        props.put(MELT_MERGE_JOIN_CONDITION.getName(),MELT_MERGE_JOIN_CONDITION);
        props.put(MELT_MERGE_UPDATE_FILTER.getName(),MELT_MERGE_UPDATE_FILTER);
        props.put(MELT_MERGE_UPDATE_COLUMN_LIST.getName(),MELT_MERGE_UPDATE_COLUMN_LIST);
        props.put(MELT_MERGE_INSERT_FILTER.getName(),MELT_MERGE_INSERT_FILTER);
        props.put(MELT_MERGE_INSERT_COLUMN_LIST.getName(),MELT_MERGE_INSERT_COLUMN_LIST);
        props.put(MELT_MERGE_INSERT_COLUMN_VALUES.getName(),MELT_MERGE_INSERT_COLUMN_VALUES);
        final List<PropertyDescriptor> pds = new ArrayList<>(props.values());
        propDescriptors = Collections.unmodifiableList(pds);
    }
}
