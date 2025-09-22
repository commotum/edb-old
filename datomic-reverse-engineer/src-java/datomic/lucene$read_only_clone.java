/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.index.IndexReader
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.index.IndexReader;

public final class lucene$read_only_clone
extends AFunction {
    public static Object invokeStatic(Object rdr) {
        Object object = rdr;
        rdr = null;
        return ((IndexReader)object).clone(Boolean.TRUE.booleanValue());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$read_only_clone.invokeStatic(object2);
    }
}

