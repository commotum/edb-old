/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.document.Document
 *  com.datomic.lucene.index.IndexWriter
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.document.Document;
import com.datomic.lucene.index.IndexWriter;

public final class lucene$add_document
extends AFunction {
    public static Object invokeStatic(Object writer2, Object doc) {
        Object object = writer2;
        writer2 = null;
        Object G__12308 = object;
        Object object2 = doc;
        doc = null;
        ((IndexWriter)G__12308).addDocument((Document)object2);
        Object var2_2 = null;
        return G__12308;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return lucene$add_document.invokeStatic(object3, object4);
    }
}

