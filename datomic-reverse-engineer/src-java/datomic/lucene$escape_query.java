/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.queryParser.QueryParser
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.queryParser.QueryParser;

public final class lucene$escape_query
extends AFunction {
    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return QueryParser.escape((String)((String)object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$escape_query.invokeStatic(object2);
    }
}

