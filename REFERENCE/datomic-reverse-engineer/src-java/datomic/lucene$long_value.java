/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.document.NumericField
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.document.NumericField;

public final class lucene$long_value
extends AFunction {
    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return ((NumericField)object).getNumericValue();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$long_value.invokeStatic(object2);
    }
}

