/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.index.IndexReader
 *  com.datomic.lucene.index.IndexWriter
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.index.IndexReader;
import com.datomic.lucene.index.IndexWriter;

public final class lucene$fn__12288
extends AFunction {
    public static Object invokeStatic(Object this_) {
        Object object = this_;
        this_ = null;
        return IndexReader.open((IndexWriter)((IndexWriter)object), (boolean)Boolean.FALSE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$fn__12288.invokeStatic(object2);
    }
}

