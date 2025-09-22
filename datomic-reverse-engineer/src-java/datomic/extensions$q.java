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
import datomic.impl.Circular;

public final class extensions$q
extends RestFn {
    public static Object invokeStatic(Object query2, ISeq srcs) {
        Object object = query2;
        query2 = null;
        ISeq iSeq = srcs;
        srcs = null;
        return Circular.q(object, iSeq);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return extensions$q.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

