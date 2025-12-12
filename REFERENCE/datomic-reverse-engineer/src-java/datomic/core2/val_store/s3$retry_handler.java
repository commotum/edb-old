/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.val_store.s3$retry_handler$fn__21417;
import datomic.core2.val_store.s3$retry_handler$metric_cb__21414;

public final class s3$retry_handler
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"backoff");
    public static final Object const__7 = 200L;
    public static final Keyword const__8 = RT.keyword(null, (String)"base");
    public static final Object const__9 = 2L;
    public static final Keyword const__10 = RT.keyword(null, (String)"retriable?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__12 = RT.var((String)"datomic.core2.retry", (String)"limiting-retry");
    public static final Object const__13 = 5L;

    public static Object invokeStatic(Object f, Object op, Object p__21411) {
        Object object;
        Object object2 = p__21411;
        p__21411 = null;
        Object map__21412 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__21412);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__21412);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = map__21412;
                map__21412 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object5)));
            } else {
                Object object6 = ((IFn)const__3.getRawRoot()).invoke(map__21412);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = map__21412;
                    map__21412 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object7);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__21412;
            map__21412 = null;
        }
        Object map__214122 = object;
        Object backoff = RT.get((Object)map__214122, (Object)const__6, (Object)const__7);
        Object base = RT.get((Object)map__214122, (Object)const__8, (Object)const__9);
        Object object8 = map__214122;
        map__214122 = null;
        Object retriable_QMARK_ = RT.get((Object)object8, (Object)const__10, (Object)((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot(), const__13));
        Object object9 = op;
        op = null;
        s3$retry_handler$metric_cb__21414 metric_cb = new s3$retry_handler$metric_cb__21414(object9);
        Object object10 = base;
        base = null;
        Object object11 = f;
        f = null;
        s3$retry_handler$metric_cb__21414 s3$retry_handler$metric_cb__21414 = metric_cb;
        metric_cb = null;
        Object object12 = retriable_QMARK_;
        retriable_QMARK_ = null;
        Object object13 = backoff;
        backoff = null;
        return new s3$retry_handler$fn__21417(object10, object11, (Object)s3$retry_handler$metric_cb__21414, object12, object13);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$retry_handler.invokeStatic(object4, object5, object6);
    }
}

