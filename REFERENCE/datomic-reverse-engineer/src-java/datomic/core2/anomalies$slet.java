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
package datomic.core2;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class anomalies$slet
extends RestFn {
    public static final Var const__0 = RT.var((String)"datomic.core2.anomalies", (String)"-slet*");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"destructure");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object bindings, ISeq body) {
        Object object = bindings;
        bindings = null;
        ISeq iSeq = body;
        body = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object), (Object)iSeq);
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return anomalies$slet.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

