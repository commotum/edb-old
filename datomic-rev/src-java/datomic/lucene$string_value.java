/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.document.Field
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.document.Field;

public final class lucene$string_value
extends AFunction {
    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return ((Field)object).stringValue();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$string_value.invokeStatic(object2);
    }
}

