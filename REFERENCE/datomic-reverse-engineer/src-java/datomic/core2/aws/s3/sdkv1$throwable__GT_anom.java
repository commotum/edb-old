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
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class sdkv1$throwable__GT_anom
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"throwable-category");
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"message");
    public static final Keyword const__3 = RT.keyword(null, (String)"error");

    public static Object invokeStatic(Object t) {
        Object cat = ((IFn)const__0.getRawRoot()).invoke(t);
        Object[] objectArray = new Object[6];
        objectArray[0] = const__1;
        Object object = cat;
        cat = null;
        objectArray[1] = object;
        objectArray[2] = const__2;
        objectArray[3] = ((Throwable)t).getMessage();
        objectArray[4] = const__3;
        Object object2 = t;
        t = null;
        objectArray[5] = ((Throwable)object2).getMessage();
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return sdkv1$throwable__GT_anom.invokeStatic(object2);
    }
}

