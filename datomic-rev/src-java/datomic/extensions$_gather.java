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
import datomic.extensions$_gather$fn__18029;

public final class extensions$_gather
extends RestFn {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"resolve-id");

    public static Object invokeStatic(Object db2, Object e, ISeq attrs) {
        Object row;
        int cnt = RT.count((Object)attrs);
        Object object = e;
        e = null;
        Object eid = ((IFn)const__1.getRawRoot()).invoke(db2, object);
        Object[] arr = RT.object_array((Object)cnt);
        ISeq iSeq = attrs;
        attrs = null;
        Object object2 = db2;
        db2 = null;
        Object object3 = eid;
        eid = null;
        Object[] objectArray = arr;
        arr = null;
        Object object4 = row = ((IFn)new extensions$_gather$fn__18029(iSeq, cnt, object2, object3, objectArray)).invoke();
        row = null;
        return object4;
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return extensions$_gather.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

