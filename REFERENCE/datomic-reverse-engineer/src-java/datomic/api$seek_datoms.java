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

public final class api$seek_datoms
extends RestFn {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"seek-datoms");

    public static Object invokeStatic(Object db2, Object index2, ISeq components) {
        Object object = db2;
        db2 = null;
        Object object2 = index2;
        index2 = null;
        ISeq iSeq = components;
        components = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)iSeq);
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return api$seek_datoms.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

