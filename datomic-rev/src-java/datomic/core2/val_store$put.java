/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.spi.Put;
import java.util.Arrays;

public final class val_store$put
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Object const__4;
    public static final Var const__5;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object val_store2, Object k, Object v, Object opts) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = v;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 == null) throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__4))));
        if (object3 == Boolean.FALSE) throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__4))));
        Object object4 = val_store2;
        val_store2 = null;
        Object object5 = object4;
        if (Util.classOf((Object)object4) != __cached_class__0) {
            if (object5 instanceof Put) {
                Object object6 = k;
                k = null;
                Object object7 = v;
                v = null;
                Object object8 = opts;
                opts = null;
                object = ((Put)object5)._put(object6, object7, object8);
                return object;
            }
            object5 = object5;
            __cached_class__0 = Util.classOf((Object)object5);
        }
        Object object9 = k;
        k = null;
        Object object10 = v;
        v = null;
        Object object11 = opts;
        opts = null;
        object = const__5.getRawRoot().invoke(object5, object9, object10, object11);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return val_store$put.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object val_store2, Object k, Object v) {
        Object object = val_store2;
        val_store2 = null;
        Object object2 = k;
        k = null;
        Object object3 = v;
        v = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, null);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return val_store$put.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.core2.val-store", (String)"put");
        const__2 = RT.var((String)"clojure.core", (String)"str");
        const__3 = RT.var((String)"clojure.core", (String)"pr-str");
        const__4 = ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"val"), Symbol.intern(null, (String)"v")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 12}));
        const__5 = RT.var((String)"datomic.core2.val-store.spi", (String)"-put");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
        __thunk__0__ = __site__0__;
    }
}

