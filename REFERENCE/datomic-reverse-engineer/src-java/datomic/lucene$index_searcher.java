/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.index.IndexReader
 *  com.datomic.lucene.search.IndexSearcher
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.index.IndexReader;
import com.datomic.lucene.search.IndexSearcher;

public final class lucene$index_searcher
extends AFunction {
    public static Object invokeStatic(Object rdr) {
        Object object = rdr;
        rdr = null;
        return new IndexSearcher((IndexReader)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$index_searcher.invokeStatic(object2);
    }
}

