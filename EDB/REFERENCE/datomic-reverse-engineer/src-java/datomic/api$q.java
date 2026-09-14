/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class api$q
extends RestFn {
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"q");

    public static Object invokeStatic(Object query2, ISeq inputs) {
        Object object = query2;
        query2 = null;
        ISeq iSeq = inputs;
        inputs = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)iSeq);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return api$q.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

