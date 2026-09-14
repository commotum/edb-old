/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.index.Term
 *  com.datomic.lucene.search.TermQuery
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.index.Term;
import com.datomic.lucene.search.TermQuery;

public final class lucene$term_query
extends AFunction {
    public static Object invokeStatic(Object field, Object term) {
        Object object = field;
        field = null;
        Object object2 = term;
        term = null;
        return new TermQuery(new Term((String)object, (String)object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return lucene$term_query.invokeStatic(object3, object4);
    }
}

