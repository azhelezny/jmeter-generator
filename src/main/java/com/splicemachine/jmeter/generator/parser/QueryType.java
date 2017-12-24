package com.splicemachine.jmeter.generator.parser;

/**
 * Types of supported SQL and DDL queries
 * Created by akorotenko on 1/26/16.
 */
public enum QueryType {
    ALTER,
    SELECT,
    EXPLAIN,
    INSERT,
    CREATE,
    UPDATE,
    DELETE,
    DROP,
    CALL,
    VALUES,
    SET,
    COMMIT,
    ROLLBACK,
    AUTOCOMMIT_ON {
        @Override
        public String toString() {
            return "AUTOCOMMIT";
        }

        @Override
        public QueryType checkValue(String query, QueryType type) {
            query = query.toLowerCase();
            return query.contains("on") ? AUTOCOMMIT_ON : AUTOCOMMIT_OFF;
        }
    },
    AUTOCOMMIT_OFF {
        @Override
        public String toString() {
            return "AUTOCOMMIT";
        }

        @Override
        public QueryType checkValue(String query, QueryType type) {
            query = query.toLowerCase();
            return query.contains("on") ? AUTOCOMMIT_ON : AUTOCOMMIT_OFF;
        }
    },
    OTHER;
    ; //PREPARE, EXECUTE, REMOVE, SET;

    public QueryType checkValue(String query, QueryType type) {
        return type;
    }

    /**
     * Check type of query (Select, Insert into... etc)
     *
     * @param query string with one query
     * @return found type or QueryType.OTHER
     */
    public static QueryType getType(String query) {
        query = query.toUpperCase().trim();

        for (QueryType type : values()) {
            if (query.startsWith(type.toString())) {
                return type.checkValue(query, type);
            }
        }
        return OTHER;
    }
}
