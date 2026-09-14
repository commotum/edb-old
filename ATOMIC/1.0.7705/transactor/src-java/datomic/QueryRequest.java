package datomic;

import clojure.lang.RT;
import java.util.HashMap;
import java.util.Map;

/**
 * Query, input, and execution parameters for {@link Peer#query(QueryRequest)}.
 */
public class QueryRequest {
    final Map m;
    /** Request key containing query inputs. */
    public static final Object ARGS = RT.keyword(null, (String)"args");
    /** Request key containing the query data structure. */
    public static final Object QUERY = RT.keyword(null, (String)"query");
    /** Request key containing the approximate timeout in milliseconds. */
    public static final Object TIMEOUT = RT.keyword(null, (String)"timeout");

    /**
     * Creates a query request.
     *
     * @param query query map, list form, or EDN string
     * @param inputs values bound by the query's {@code :in} clause
     * @return a request containing the query and inputs
     */
    public static QueryRequest create(Object query, Object ... inputs) {
        return new QueryRequest(query, inputs);
    }

    QueryRequest(Object query, Object[] inputs) {
        this.m = new HashMap();
        this.m.put(QUERY, query);
        this.m.put(ARGS, inputs);
    }

    QueryRequest(Map m) {
        this.m = m;
    }

    /**
     * Returns a request with an approximate query timeout.
     *
     * <p>The timeout protects against long-running work and may be observed
     * shortly after the requested duration.</p>
     *
     * @param timeoutMsec milliseconds after which the query may be stopped
     * @return an updated request suitable for method chaining
     */
    public QueryRequest timeout(long timeoutMsec) {
        Map nm = new HashMap(this.m);
        nm.put(TIMEOUT, timeoutMsec);
        return new QueryRequest(nm);
    }

    /**
     * Returns a readable representation of this request's data.
     */
    @Override
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

    /**
     * Returns the data representation consumed by the query engine.
     *
     * @return the query request data
     */
    public Map asData() {
        return this.m;
    }
}
