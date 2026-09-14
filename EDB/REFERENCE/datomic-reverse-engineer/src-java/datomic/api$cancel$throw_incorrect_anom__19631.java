/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class api$cancel$throw_incorrect_anom__19631
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"incorrect");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"message");

    public Object invoke(Object p1__19628_SHARP_) {
        Object object = p1__19628_SHARP_;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        objectArray[1] = const__2;
        objectArray[2] = const__3;
        Object object2 = p1__19628_SHARP_;
        p1__19628_SHARP_ = null;
        objectArray[3] = object2;
        throw (Throwable)((IFn)const__0.getRawRoot()).invoke(object, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }
}

