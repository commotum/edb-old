/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import java.util.Arrays;

public final class cluster$update_pod
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Object const__6;
    public static final Var const__7;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object cs, Object pod_key, Object rev, Object etag, Object buf, Object metamap) {
        Object object;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(metamap));
        if (object2 == null) throw (Throwable)((Object)new AssertionError(((IFn)const__4.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__5.getRawRoot()).invoke(const__6))));
        if (object2 == Boolean.FALSE) throw (Throwable)((Object)new AssertionError(((IFn)const__4.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__5.getRawRoot()).invoke(const__6))));
        Object object3 = cs;
        cs = null;
        Object object4 = object3;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object4 instanceof ClusteredStore) {
                Object object5 = pod_key;
                pod_key = null;
                Object object6 = rev;
                rev = null;
                Object object7 = etag;
                etag = null;
                Object object8 = buf;
                buf = null;
                Object object9 = metamap;
                metamap = null;
                object = ((ClusteredStore)object4).update_pod_STAR_(object5, object6, object7, object8, object9);
                return object;
            }
            object4 = object4;
            __cached_class__0 = Util.classOf((Object)object4);
        }
        Object object10 = pod_key;
        pod_key = null;
        Object object11 = rev;
        rev = null;
        Object object12 = etag;
        etag = null;
        Object object13 = buf;
        buf = null;
        Object object14 = metamap;
        metamap = null;
        object = const__7.getRawRoot().invoke(object4, object10, object11, object12, object13, object14);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return cluster$update_pod.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    public static Object invokeStatic(Object cs, Object pod_key, Object rev, Object etag, Object buf) {
        Object object = cs;
        cs = null;
        Object object2 = pod_key;
        pod_key = null;
        Object object3 = rev;
        rev = null;
        Object object4 = etag;
        etag = null;
        Object object5 = buf;
        buf = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, object4, object5, null);
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
        return cluster$update_pod.invokeStatic(object6, object7, object8, object9, object10);
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"update-pod");
        const__1 = RT.var((String)"clojure.core", (String)"every?");
        const__2 = RT.var((String)"clojure.core", (String)"namespace");
        const__3 = RT.var((String)"clojure.core", (String)"keys");
        const__4 = RT.var((String)"clojure.core", (String)"str");
        const__5 = RT.var((String)"clojure.core", (String)"pr-str");
        const__6 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"every?"), Symbol.intern(null, (String)"namespace"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"keys"), Symbol.intern(null, (String)"metamap")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 32}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 14}));
        const__7 = RT.var((String)"datomic.cluster", (String)"update-pod*");
    }
}

