package com.tracetrail.ingestion.parser;

import com.tracetrail.ingestion.parser.records.traces.AnyValue;
import com.tracetrail.ingestion.parser.records.traces.Attribute;

import java.util.List;

public final class Attributes {

    public static String findAttribute(List<Attribute> attrs, String key){
        if (attrs == null){
            return null;
        }
        for (Attribute attr: attrs){
            if (attr.key().equals(key)){
                AnyValue value = attr.value();
                if (!value.stringValue().isEmpty()){
                    return  value.stringValue();
                } else if (value.boolValue() != null){
                    return String.valueOf(value.boolValue());
                } else if (value.intValue() != null) {
                    return String.valueOf(value.intValue());
                } else if (value.doubleValue() != null) {
                    return String.valueOf(value.doubleValue());
                }
            }
        }
        return null;
    }
}
