/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.index.IndexWriter
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.index.IndexWriter;
import java.util.Collection;

public final class lucene$add_documents
extends AFunction {
    public static Object invokeStatic(Object writer2, Object docs) {
        Object object = writer2;
        writer2 = null;
        Object G__12310 = object;
        Object object2 = docs;
        docs = null;
        ((IndexWriter)G__12310).addDocuments((Collection)object2);
        Object var2_2 = null;
        return G__12310;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return lucene$add_documents.invokeStatic(object3, object4);
    }
}

