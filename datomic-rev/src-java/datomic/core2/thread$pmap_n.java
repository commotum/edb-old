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
import datomic.core2.thread$pmap_n$fn__21021;
import datomic.core2.thread$pmap_n$fn__21038;
import datomic.core2.thread$pmap_n$step__21026;
import datomic.core2.thread$pmap_n$step__21034;

public final class thread$pmap_n
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"drop");
    public static final Var const__2 = RT.var((String)"datomic.core2.thread", (String)"pmap-n");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"cons");

    public static Object invokeStatic(Object n, Object f, Object coll, ISeq colls) {
        thread$pmap_n$step__21034 step = new thread$pmap_n$step__21034();
        Object object = n;
        n = null;
        Object object2 = f;
        f = null;
        thread$pmap_n$step__21034 thread$pmap_n$step__21034 = step;
        step = null;
        Object object3 = coll;
        coll = null;
        ISeq iSeq = colls;
        colls = null;
        return ((IFn)const__2.getRawRoot()).invoke(object, (Object)new thread$pmap_n$fn__21038(object2), ((IFn)thread$pmap_n$step__21034).invoke(((IFn)const__3.getRawRoot()).invoke(object3, (Object)iSeq)));
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
        return thread$pmap_n.invokeStatic(object5, object6, object7, iSeq);
    }

    public static Object invokeStatic(Object n, Object f, Object coll) {
        thread$pmap_n$step__21026 step;
        Object object = f;
        f = null;
        Object object2 = coll;
        coll = null;
        Object rets = ((IFn)const__0.getRawRoot()).invoke((Object)new thread$pmap_n$fn__21021(object), object2);
        thread$pmap_n$step__21026 thread$pmap_n$step__21026 = step = new thread$pmap_n$step__21026();
        step = null;
        Object object3 = rets;
        Object object4 = n;
        n = null;
        Object object5 = rets;
        rets = null;
        return ((IFn)thread$pmap_n$step__21026).invoke(object3, ((IFn)const__1.getRawRoot()).invoke(object4, object5));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return thread$pmap_n.invokeStatic(object4, object5, object6);
    }

    public int getRequiredArity() {
        return 3;
    }
}

