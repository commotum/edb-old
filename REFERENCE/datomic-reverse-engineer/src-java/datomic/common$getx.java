/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class common$getx
extends AFunction {
    public static final Keyword const__1 = RT.keyword((String)"datomic.common", (String)"getx-sentinel-42");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object m, Object k) {
        Object object = m;
        m = null;
        Object e = RT.get((Object)object, (Object)k, (Object)const__1);
        Object object2 = ((IFn)const__2.getRawRoot()).invoke((Object)(Util.equiv((Object)e, (Object)const__1) ? Boolean.TRUE : Boolean.FALSE));
        if (object2 == null || object2 == Boolean.FALSE) {
            Object object3 = k;
            k = null;
            throw (Throwable)new Exception((String)((IFn)const__4.getRawRoot()).invoke((Object)"Key not found: ", object3));
        }
        Object object4 = e;
        e = null;
        return object4;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$getx.invokeStatic(object3, object4);
    }
}

