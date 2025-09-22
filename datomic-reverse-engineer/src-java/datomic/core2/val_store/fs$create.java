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

public final class fs$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"delete-pool");
    public static final Keyword const__7 = RT.keyword(null, (String)"get-pool");
    public static final Keyword const__8 = RT.keyword(null, (String)"path");
    public static final Keyword const__9 = RT.keyword(null, (String)"put-pool");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__12 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"delete-pool"), Symbol.intern(null, (String)"get-pool"), Symbol.intern(null, (String)"path"), Symbol.intern(null, (String)"put-pool")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__13 = RT.var((String)"datomic.core2.val-store.fs", (String)"->FS");

    public static Object invokeStatic(Object p__21341) {
        Object object;
        Object and__5579__auto__21346;
        Object object2;
        Object object3 = p__21341;
        p__21341 = null;
        Object map__21342 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21342);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__21342);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__21342;
                map__21342 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__21342);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__21342;
                    map__21342 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__21342;
            map__21342 = null;
        }
        Object map__213422 = object2;
        Object delete_pool = RT.get((Object)map__213422, (Object)const__6);
        Object get_pool = RT.get((Object)map__213422, (Object)const__7);
        Object path2 = RT.get((Object)map__213422, (Object)const__8);
        Object object9 = map__213422;
        map__213422 = null;
        Object put_pool = RT.get((Object)object9, (Object)const__9);
        Object object10 = and__5579__auto__21346 = delete_pool;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object and__5579__auto__21345;
            Object object11 = and__5579__auto__21345 = get_pool;
            if (object11 != null && object11 != Boolean.FALSE) {
                Object and__5579__auto__21344;
                Object object12 = and__5579__auto__21344 = path2;
                if (object12 != null && object12 != Boolean.FALSE) {
                    object = put_pool;
                } else {
                    object = and__5579__auto__21344;
                    and__5579__auto__21344 = null;
                }
            } else {
                object = and__5579__auto__21345;
                and__5579__auto__21345 = null;
            }
        } else {
            object = and__5579__auto__21346;
            and__5579__auto__21346 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke(const__12))));
        }
        Object object13 = get_pool;
        get_pool = null;
        Object object14 = put_pool;
        put_pool = null;
        Object object15 = path2;
        path2 = null;
        Object object16 = delete_pool;
        delete_pool = null;
        return ((IFn)const__13.getRawRoot()).invoke(object13, object14, object15, object16);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fs$create.invokeStatic(object2);
    }
}

