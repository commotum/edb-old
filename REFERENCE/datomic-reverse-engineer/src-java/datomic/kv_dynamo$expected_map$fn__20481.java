/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class kv_dynamo$expected_map$fn__20481
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"name");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"exists"), Boolean.FALSE});
    public static final Keyword const__7 = RT.keyword(null, (String)"value");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"number?");
    public static final Keyword const__9 = RT.keyword(null, (String)"n");
    public static final Keyword const__10 = RT.keyword(null, (String)"s");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");

    public Object invoke(Object p__20480) {
        AFn aFn;
        Object object = p__20480;
        p__20480 = null;
        Object vec__20482 = object;
        Object k = RT.nth((Object)vec__20482, (int)RT.intCast((long)0L), null);
        Object object2 = vec__20482;
        vec__20482 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(object3);
        if (Util.identical((Object)v, null)) {
            aFn = const__6;
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__7;
            Object[] objectArray2 = new Object[2];
            Object object5 = ((IFn)const__8.getRawRoot()).invoke(v);
            objectArray2[0] = object5 != null && object5 != Boolean.FALSE ? const__9 : const__10;
            Object object6 = v;
            v = null;
            objectArray2[1] = ((IFn)const__11.getRawRoot()).invoke(object6);
            objectArray[1] = RT.mapUniqueKeys((Object[])objectArray2);
            aFn = RT.mapUniqueKeys((Object[])objectArray);
        }
        return Tuple.create((Object)object4, (Object)aFn);
    }
}

