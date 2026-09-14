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
package datomic.core2.val_store.s3;

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

public final class sdkv1$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"read-pool");
    public static final Keyword const__7 = RT.keyword(null, (String)"write-pool");
    public static final Keyword const__8 = RT.keyword(null, (String)"retry-fn");
    public static final Keyword const__9 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__10 = RT.keyword(null, (String)"client");
    public static final Keyword const__11 = RT.keyword(null, (String)"prefix");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__14 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"read-pool"), Symbol.intern(null, (String)"write-pool"), Symbol.intern(null, (String)"retry-fn"), Symbol.intern(null, (String)"bucket"), Symbol.intern(null, (String)"client"), Symbol.intern(null, (String)"prefix")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__15 = RT.var((String)"datomic.core2.val-store.s3.sdkv1", (String)"->ValStore");

    public static Object invokeStatic(Object p__21775) {
        Object object;
        Object and__5579__auto__21782;
        Object object2;
        Object object3 = p__21775;
        p__21775 = null;
        Object map__21776 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21776);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__21776);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__21776;
                map__21776 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__21776);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__21776;
                    map__21776 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__21776;
            map__21776 = null;
        }
        Object map__217762 = object2;
        Object read_pool = RT.get((Object)map__217762, (Object)const__6);
        Object write_pool = RT.get((Object)map__217762, (Object)const__7);
        Object retry_fn2 = RT.get((Object)map__217762, (Object)const__8);
        Object bucket = RT.get((Object)map__217762, (Object)const__9);
        Object client2 = RT.get((Object)map__217762, (Object)const__10);
        Object object9 = map__217762;
        map__217762 = null;
        Object prefix = RT.get((Object)object9, (Object)const__11);
        Object object10 = and__5579__auto__21782 = read_pool;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object and__5579__auto__21781;
            Object object11 = and__5579__auto__21781 = write_pool;
            if (object11 != null && object11 != Boolean.FALSE) {
                Object and__5579__auto__21780;
                Object object12 = and__5579__auto__21780 = retry_fn2;
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object and__5579__auto__21779;
                    Object object13 = and__5579__auto__21779 = bucket;
                    if (object13 != null && object13 != Boolean.FALSE) {
                        Object and__5579__auto__21778;
                        Object object14 = and__5579__auto__21778 = client2;
                        if (object14 != null && object14 != Boolean.FALSE) {
                            object = prefix;
                        } else {
                            object = and__5579__auto__21778;
                            and__5579__auto__21778 = null;
                        }
                    } else {
                        object = and__5579__auto__21779;
                        and__5579__auto__21779 = null;
                    }
                } else {
                    object = and__5579__auto__21780;
                    and__5579__auto__21780 = null;
                }
            } else {
                object = and__5579__auto__21781;
                and__5579__auto__21781 = null;
            }
        } else {
            object = and__5579__auto__21782;
            and__5579__auto__21782 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__12.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__13.getRawRoot()).invoke(const__14))));
        }
        Object object15 = read_pool;
        read_pool = null;
        Object object16 = write_pool;
        write_pool = null;
        Object object17 = retry_fn2;
        retry_fn2 = null;
        Object object18 = client2;
        client2 = null;
        Object object19 = bucket;
        bucket = null;
        Object object20 = prefix;
        prefix = null;
        return ((IFn)const__15.getRawRoot()).invoke(object15, object16, object17, object18, object19, object20);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return sdkv1$create.invokeStatic(object2);
    }
}

