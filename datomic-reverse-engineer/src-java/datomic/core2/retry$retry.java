/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class retry$retry
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"on-success");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"identity");
    public static final Keyword const__8 = RT.keyword(null, (String)"on-failure");
    public static final Keyword const__10 = RT.keyword(null, (String)"ok?");
    public static final Keyword const__11 = RT.keyword(null, (String)"i");
    public static final Keyword const__12 = RT.keyword(null, (String)"result");
    public static final Keyword const__13 = RT.keyword(null, (String)"start-ms");
    public static final Keyword const__14 = RT.keyword(null, (String)"end-ms");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__16 = RT.keyword(null, (String)"backoff-ms");

    public static Object invokeStatic(Object f, Object pred2, Object retry_QMARK_, Object calc_backoff, Object p__20988) {
        Object object;
        block14: {
            Object result2;
            Object object2;
            Object object3 = p__20988;
            p__20988 = null;
            Object map__20989 = object3;
            Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__20989);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__20989);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__20989;
                    map__20989 = null;
                    object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
                } else {
                    Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__20989);
                    if (object7 != null && object7 != Boolean.FALSE) {
                        Object object8 = map__20989;
                        map__20989 = null;
                        object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                    } else {
                        object2 = PersistentArrayMap.EMPTY;
                    }
                }
            } else {
                object2 = map__20989;
                map__20989 = null;
            }
            Object map__209892 = object2;
            Object on_success = RT.get((Object)map__209892, (Object)const__6, (Object)const__7.getRawRoot());
            Object object9 = map__209892;
            map__209892 = null;
            Object on_failure = RT.get((Object)object9, (Object)const__8, (Object)const__7.getRawRoot());
            long start_ms = System.currentTimeMillis();
            long i = 0L;
            while (true) {
                Object ok_QMARK_2;
                Object map__20990;
                Object object10;
                IPersistentMap round_map;
                result2 = ((IFn)f).invoke();
                long end_ms = System.currentTimeMillis();
                IPersistentMap iPersistentMap = round_map = RT.mapUniqueKeys((Object[])new Object[]{const__10, ((IFn)pred2).invoke(result2), const__11, Numbers.num((long)i), const__12, result2, const__13, Numbers.num((long)start_ms), const__14, Numbers.num((long)end_ms)});
                Object[] objectArray = new Object[2];
                objectArray[0] = const__16;
                IPersistentMap iPersistentMap2 = round_map;
                round_map = null;
                objectArray[1] = ((IFn)calc_backoff).invoke((Object)iPersistentMap2);
                Object map__209902 = ((IFn)const__15.getRawRoot()).invoke((Object)iPersistentMap, (Object)RT.mapUniqueKeys((Object[])objectArray));
                Object object11 = ((IFn)const__0.getRawRoot()).invoke(map__209902);
                if (object11 != null && object11 != Boolean.FALSE) {
                    Object object12 = ((IFn)const__1.getRawRoot()).invoke(map__209902);
                    if (object12 != null && object12 != Boolean.FALSE) {
                        Object object13 = map__209902;
                        map__209902 = null;
                        object10 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object13)));
                    } else {
                        Object object14 = ((IFn)const__3.getRawRoot()).invoke(map__209902);
                        if (object14 != null && object14 != Boolean.FALSE) {
                            Object object15 = map__209902;
                            map__209902 = null;
                            object10 = ((IFn)const__4.getRawRoot()).invoke(object15);
                        } else {
                            object10 = PersistentArrayMap.EMPTY;
                        }
                    }
                } else {
                    object10 = map__209902;
                    map__209902 = null;
                }
                Object round_map2 = map__20990 = object10;
                Object backoff_ms = RT.get((Object)map__20990, (Object)const__16);
                Object object16 = map__20990;
                map__20990 = null;
                Object object17 = ok_QMARK_2 = RT.get((Object)object16, (Object)const__10);
                ok_QMARK_2 = null;
                if (object17 != null && object17 != Boolean.FALSE) {
                    Object object18 = round_map2;
                    round_map2 = null;
                    ((IFn)on_success).invoke(object18);
                    object = result2;
                    result2 = null;
                    break block14;
                }
                ((IFn)on_failure).invoke(round_map2);
                Object object19 = round_map2;
                round_map2 = null;
                Object object20 = ((IFn)retry_QMARK_).invoke(object19);
                if (object20 == null || object20 == Boolean.FALSE) break;
                Object object21 = backoff_ms;
                backoff_ms = null;
                Thread.sleep(RT.longCast((Object)((Number)object21)));
                i = Numbers.inc((long)i);
            }
            object = result2;
            result2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return retry$retry.invokeStatic(object6, object7, object8, object9, object10);
    }
}

