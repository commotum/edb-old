/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.document.Document
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.document.Document;

public final class lucene$get_field
extends AFunction {
    public static Object invokeStatic(Object doc, Object s) {
        Object object = doc;
        doc = null;
        Object object2 = s;
        s = null;
        return ((Document)object).getFieldable((String)object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return lucene$get_field.invokeStatic(object3, object4);
    }
}

