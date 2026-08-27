/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cluster_stack;

import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.spi.Delete;
import datomic.core2.val_store.spi.Get;
import datomic.core2.val_store.spi.Put;

public final class ValStoreWithClose
implements Get,
Delete,
AutoCloseable,
Put,
IType {
    public final Object store;
    public final Object close;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;

    public ValStoreWithClose(Object object, Object object2) {
        this.store = object;
        this.close = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"store"), (Object)Symbol.intern(null, (String)"close"));
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object _delete(Object k, Object opts) {
        Object object;
        Object object2 = this_.store;
        if (Util.classOf((Object)object2) != __cached_class__2) {
            if (object2 instanceof Delete) {
                Object object3 = k;
                k = null;
                Object object4 = opts;
                opts = null;
                object = ((Delete)object2)._delete(object3, object4);
                return object;
            }
            object2 = object2;
            __cached_class__2 = Util.classOf((Object)object2);
        }
        Object object5 = k;
        k = null;
        Object object6 = opts;
        opts = null;
        ValStoreWithClose this_ = null;
        object = const__2.getRawRoot().invoke(object2, object5, object6);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object _get(Object k, Object opts) {
        Object object;
        Object object2 = this_.store;
        if (Util.classOf((Object)object2) != __cached_class__1) {
            if (object2 instanceof Get) {
                Object object3 = k;
                k = null;
                Object object4 = opts;
                opts = null;
                object = ((Get)object2)._get(object3, object4);
                return object;
            }
            object2 = object2;
            __cached_class__1 = Util.classOf((Object)object2);
        }
        Object object5 = k;
        k = null;
        Object object6 = opts;
        opts = null;
        ValStoreWithClose this_ = null;
        object = const__1.getRawRoot().invoke(object2, object5, object6);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object _put(Object k, Object v, Object opts) {
        Object object;
        Object object2 = this_.store;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Put) {
                Object object3 = k;
                k = null;
                Object object4 = v;
                v = null;
                Object object5 = opts;
                opts = null;
                object = ((Put)object2)._put(object3, object4, object5);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object6 = k;
        k = null;
        Object object7 = v;
        v = null;
        Object object8 = opts;
        opts = null;
        ValStoreWithClose this_ = null;
        object = const__0.getRawRoot().invoke(object2, object6, object7, object8);
        return object;
    }

    public void close() throws Exception {
        ValStoreWithClose this_ = null;
        ((IFn)this_.close).invoke();
    }

    static {
        const__0 = RT.var((String)"datomic.core2.val-store.spi", (String)"-put");
        const__1 = RT.var((String)"datomic.core2.val-store.spi", (String)"-get");
        const__2 = RT.var((String)"datomic.core2.val-store.spi", (String)"-delete");
    }
}

