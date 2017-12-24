package com.splicemachine.jmeter.generator.parser;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Contains one query and its type
 * Created by akorotenko on 1/26/16.
 */
public class QueryHolder {
    private final QueryType type;
    private final JMeterQueryType jMeterQueryType;
    private final String value;
    private String comments;

    public QueryHolder(QueryType type, String value) {
        this(type, value, "");
    }

    public QueryHolder(QueryType type, String value, String comments) {
        this.type = type;
        this.value = StringEscapeUtils.escapeXml10(value);
        this.comments = StringEscapeUtils.escapeXml10(comments);
        jMeterQueryType = JMeterQueryType.getType(type);
    }

    public QueryType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public JMeterQueryType getjMeterQueryType() {
        return jMeterQueryType;
    }
}
