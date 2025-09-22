/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LOO
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

public final class retry$limiting_retry
extends AFunction
implements IFn.LOO {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"i");

    public static Object invokeStatic(long iter2, Object object) {
        Object i;
        Object map__20993;
        Object object2;
        Object object3 = object;
        object = null;
        Object map__209932 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__209932);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__209932);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__209932;
                map__209932 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__209932);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__209932;
                    map__209932 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__209932;
            map__209932 = null;
        }
        Object object9 = map__20993 = object2;
        map__20993 = null;
        Object object10 = i = RT.get((Object)object9, (Object)const__6);
        i = null;
        return Numbers.lte((long)RT.longCast((Object)((Number)object10)), (long)iter2) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object2;
        object2 = null;
        return retry$limiting_retry.invokeStatic(RT.longCast((Object)((Number)object)), object3);
    }

    public final Object invokePrim(long l, Object object) {
        Object object2 = object;
        object = null;
        return retry$limiting_retry.invokeStatic(l, object2);
    }
}

