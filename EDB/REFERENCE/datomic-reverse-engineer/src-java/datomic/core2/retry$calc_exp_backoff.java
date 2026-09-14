/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class retry$calc_exp_backoff
extends AFunction
implements IFn.LLOL {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"i");

    public static long invokeStatic(long backoff, long p__20996, Object object) {
        Object i;
        Object map__20997;
        Object object2;
        Object object3 = object;
        object = null;
        Object map__209972 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__209972);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__209972);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__209972;
                map__209972 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__209972);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__209972;
                    map__209972 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__209972;
            map__209972 = null;
        }
        Object object9 = map__20997 = object2;
        map__20997 = null;
        Object object10 = i = RT.get((Object)object9, (Object)const__6);
        i = null;
        return RT.longCast((double)Numbers.multiply((long)backoff, (double)Math.pow(RT.doubleCast((Object)Numbers.num((long)p__20996)), RT.doubleCast((Object)((Number)object10)))));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object3;
        object3 = null;
        return retry$calc_exp_backoff.invokeStatic(RT.longCast((Object)((Number)object)), RT.longCast((Object)((Number)object2)), object4);
    }

    public final long invokePrim(long l, long l2, Object object) {
        Object object2 = object;
        object = null;
        return retry$calc_exp_backoff.invokeStatic(l, l2, object2);
    }
}

