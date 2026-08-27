/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.common$coerce_args$fn__9055;

public final class common$coerce_args
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__1 = 2L;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object m, ISeq predfns) {
        Object predfns2;
        ISeq iSeq = predfns;
        predfns = null;
        Object object = predfns2 = ((IFn)const__0.getRawRoot()).invoke(const__1, (Object)iSeq);
        predfns2 = null;
        Object object2 = m;
        m = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)new common$coerce_args$fn__9055(object), (Object)PersistentArrayMap.EMPTY, object2);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return common$coerce_args.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

