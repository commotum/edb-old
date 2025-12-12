/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class double_store$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"near-store");
    public static final Keyword const__7 = RT.keyword(null, (String)"far-store");
    public static final Keyword const__8 = RT.keyword(null, (String)"repair-metric");
    public static final Keyword const__9 = RT.keyword(null, (String)"fs.repair");
    public static final Keyword const__10 = RT.keyword(null, (String)"get-fallback-msec");
    public static final Object const__11 = 20L;
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__14 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"near-store"), Symbol.intern(null, (String)"far-store")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__15 = RT.var((String)"datomic.core2.val-store.double-store", (String)"->ValStore");

    public static Object invokeStatic(Object p__21251) {
        Object object;
        Object and__5579__auto__21254;
        Object object2;
        Object object3 = p__21251;
        p__21251 = null;
        Object map__21252 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21252);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__21252);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__21252;
                map__21252 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__21252);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__21252;
                    map__21252 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__21252;
            map__21252 = null;
        }
        Object map__212522 = object2;
        Object near_store = RT.get((Object)map__212522, (Object)const__6);
        Object far_store = RT.get((Object)map__212522, (Object)const__7);
        Object repair_metric = RT.get((Object)map__212522, (Object)const__8, (Object)const__9);
        Object object9 = map__212522;
        map__212522 = null;
        Object get_fallback_msec = RT.get((Object)object9, (Object)const__10, (Object)const__11);
        Object object10 = and__5579__auto__21254 = near_store;
        if (object10 != null && object10 != Boolean.FALSE) {
            object = far_store;
        } else {
            object = and__5579__auto__21254;
            and__5579__auto__21254 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__12.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__13.getRawRoot()).invoke(const__14))));
        }
        Object object11 = near_store;
        near_store = null;
        Object object12 = far_store;
        far_store = null;
        Object object13 = repair_metric;
        repair_metric = null;
        Object object14 = get_fallback_msec;
        get_fallback_msec = null;
        return ((IFn)const__15.getRawRoot()).invoke(object11, object12, object13, object14);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return double_store$create.invokeStatic(object2);
    }
}

