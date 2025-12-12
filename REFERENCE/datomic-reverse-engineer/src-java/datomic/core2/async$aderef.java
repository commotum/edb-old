/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class async$aderef
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.async", (String)"aderef");
    public static final Object const__1 = 1000L;
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"interrupted");
    public static final Keyword const__4 = RT.keyword(null, (String)"timeout-ms");
    public static final Var const__5 = RT.var((String)"clojure.core.async", (String)"timeout");
    public static final Var const__6 = RT.var((String)"clojure.core.async", (String)"alts!!");

    public static Object invokeStatic(Object ch, Object timeout_ms, Object timeout_val) {
        Object object;
        Object port;
        Object to;
        Object object2 = timeout_ms;
        timeout_ms = null;
        Object object3 = to = ((IFn.LO)const__5.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object2)));
        to = null;
        Object vec__19470 = ((IFn)const__6.getRawRoot()).invoke((Object)Tuple.create((Object)ch, (Object)object3));
        Object v = RT.nth((Object)vec__19470, (int)RT.intCast((long)0L), null);
        Object object4 = vec__19470;
        vec__19470 = null;
        Object object5 = port = RT.nth((Object)object4, (int)RT.intCast((long)1L), null);
        port = null;
        Object object6 = ch;
        ch = null;
        if (Util.equiv((Object)object5, (Object)object6)) {
            object = v;
            v = null;
        } else {
            object = timeout_val;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return async$aderef.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object ch, Object timeout_ms) {
        Object object = ch;
        ch = null;
        Object object2 = timeout_ms;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__2;
        objectArray[1] = const__3;
        objectArray[2] = const__4;
        Object object3 = timeout_ms;
        timeout_ms = null;
        objectArray[3] = object3;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return async$aderef.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object ch) {
        Object object = ch;
        ch = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return async$aderef.invokeStatic(object2);
    }
}

