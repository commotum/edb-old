/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class async$aderef_n
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.async", (String)"aderef-n");
    public static final Object const__1 = 1000L;
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"interrupted");
    public static final Keyword const__4 = RT.keyword(null, (String)"timeout-ms");
    public static final Var const__5 = RT.var((String)"clojure.core.async", (String)"timeout");
    public static final Var const__8 = RT.var((String)"clojure.core.async", (String)"alts!!");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"conj");

    public static Object invokeStatic(Object n, Object ch, Object timeout_ms, Object timeout_val) {
        Object object;
        block2: {
            Object object2 = timeout_ms;
            timeout_ms = null;
            Object to = ((IFn.LO)const__5.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object2)));
            Object results = PersistentVector.EMPTY;
            while (true) {
                Object port;
                if (Util.equiv((long)RT.count((Object)results), (Object)n)) {
                    object = results;
                    results = null;
                    break block2;
                }
                Object vec__19474 = ((IFn)const__8.getRawRoot()).invoke((Object)Tuple.create((Object)ch, (Object)to));
                Object v = RT.nth((Object)vec__19474, (int)RT.intCast((long)0L), null);
                Object object3 = vec__19474;
                vec__19474 = null;
                Object object4 = port = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
                port = null;
                if (!Util.equiv((Object)object4, (Object)ch)) break;
                PersistentVector persistentVector = results;
                results = null;
                Object object5 = v;
                v = null;
                results = ((IFn)const__12.getRawRoot()).invoke((Object)persistentVector, object5);
            }
            PersistentVector persistentVector = results;
            results = null;
            object = ((IFn)const__12.getRawRoot()).invoke((Object)persistentVector, timeout_val);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return async$aderef_n.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object n, Object ch, Object timeout_ms) {
        Object object = n;
        n = null;
        Object object2 = ch;
        ch = null;
        Object object3 = timeout_ms;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__2;
        objectArray[1] = const__3;
        objectArray[2] = const__4;
        Object object4 = timeout_ms;
        timeout_ms = null;
        objectArray[3] = object4;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return async$aderef_n.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object n, Object ch) {
        Object object = n;
        n = null;
        Object object2 = ch;
        ch = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, const__1);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return async$aderef_n.invokeStatic(object3, object4);
    }
}

