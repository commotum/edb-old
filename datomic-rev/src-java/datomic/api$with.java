/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ISeq
 *  clojure.lang.RestFn
 */
package datomic;

import clojure.lang.ISeq;
import clojure.lang.RestFn;
import datomic.Database;
import java.util.List;

public final class api$with
extends RestFn {
    public static Object invokeStatic(Object db2, Object tx_data2, ISeq opts) {
        Object object = db2;
        db2 = null;
        Object object2 = tx_data2;
        tx_data2 = null;
        ISeq iSeq = opts;
        opts = null;
        return ((Database)object).with((List)object2, iSeq);
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return api$with.invokeStatic(object4, object5, iSeq);
    }

    public static Object invokeStatic(Object db2, Object tx_data2) {
        Object object = db2;
        db2 = null;
        Object object2 = tx_data2;
        tx_data2 = null;
        return ((Database)object).with((List)object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return api$with.invokeStatic(object3, object4);
    }

    public int getRequiredArity() {
        return 2;
    }
}

