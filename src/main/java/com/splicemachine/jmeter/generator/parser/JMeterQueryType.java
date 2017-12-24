package com.splicemachine.jmeter.generator.parser;

/**
 * Created by akorotenko on 1/26/16.
 */
public enum JMeterQueryType {

    SELECT("Select Statement", QueryType.SELECT, QueryType.EXPLAIN, QueryType.VALUES),
    UPDATE("Update Statement", QueryType.CREATE, QueryType.UPDATE, QueryType.DELETE, QueryType.DROP, QueryType.INSERT, QueryType.ALTER, QueryType.SET),
    AUTOCOMMIT_ON("AutoCommit(true)", QueryType.AUTOCOMMIT_ON),
    AUTOCOMMIT_OFF("AutoCommit(false)", QueryType.AUTOCOMMIT_OFF),
    COMMIT("Commit", QueryType.COMMIT),
    ROLLBACK("Rollback", QueryType.ROLLBACK),
    CALL("Callable Statement", QueryType.CALL),
    OTHER("Select Statement", QueryType.OTHER); //, QueryType.EXECUTE, QueryType.REMOVE, QueryType.SET

    private final String displayName;
    private QueryType[] types;

    JMeterQueryType(String displayName, QueryType... types) {
        this.displayName = displayName;
        this.types = types;
    }

    public static JMeterQueryType getType(QueryType queryType) {
        for (JMeterQueryType type : values()) {
            for (int i = 0; i < type.types.length; i++) {
                if (type.types[i].equals(queryType)) {
                    return type;
                }
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return displayName;
    }
}
