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
import datomic.index$idx_key$fn__15551;

public final class index$idx_key
extends RestFn {
    public static Object invokeStatic(ISeq ks) {
        ISeq iSeq = ks;
        ks = null;
        return new index$idx_key$fn__15551(iSeq);
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return index$idx_key.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

