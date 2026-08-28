/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.RT;
import java.util.HashMap;
import java.util.Map;

public class QueryRequest {
    final Map m;
    public static final Object ARGS = RT.keyword(null, (String)"args");
    public static final Object QUERY = RT.keyword(null, (String)"query");
    public static final Object TIMEOUT = RT.keyword(null, (String)"timeout");

    public static QueryRequest create(Object query2, Object ... inputs) {
        return new QueryRequest(query2, inputs);
    }

    QueryRequest(Object query2, Object[] inputs) {
        this.m = new HashMap();
        this.m.put(QUERY, query2);
        this.m.put(ARGS, inputs);
    }

    QueryRequest(Map m) {
        this.m = m;
    }

    public QueryRequest timeout(long timeoutMsec) {
        Map nm = new HashMap(this.m);
        nm.put(TIMEOUT, timeoutMsec);
        return new QueryRequest(nm);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("{");
        for (Object key : this.m.keySet()) {
            sb.append(key.toString()).append("=");
            Object value = this.m.get(key);
            if (value instanceof Object[]) {
                Object[] values = (Object[])value;
                sb.append("[");
            String args = "";
            for (Object value1 : values) {
                args = args + value1.toString() + ",";
            }
            sb.append(args.substring(0, args.length() - 1));
                sb.append("]");
            } else {
                sb.append(value.toString());
            }
            sb.append("}\n");
        }
        return new String(sb);
    }

    public Map asData() {
        return this.m;
    }
}
