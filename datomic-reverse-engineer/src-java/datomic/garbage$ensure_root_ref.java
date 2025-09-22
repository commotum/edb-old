/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.garbage$ensure_root_ref$create__19773;

public final class garbage$ensure_root_ref
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Var const__11;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cluster, Object forget_garbage) {
        block10: {
            block8: {
                block11: {
                    block9: {
                        v0 = temp__5457__auto__19778 = ((IFn)garbage$ensure_root_ref.const__1.getRawRoot()).invoke(cluster);
                        if (v0 == null || v0 == Boolean.FALSE) break block8;
                        v1 = temp__5457__auto__19778;
                        temp__5457__auto__19778 = null;
                        root_key = v1;
                        v2 = (IFn)garbage$ensure_root_ref.const__2.getRawRoot();
                        v3 = cluster;
                        if (Util.classOf((Object)v3) == garbage$ensure_root_ref.__cached_class__0) ** GOTO lbl12
                        if (!(v3 instanceof ClusteredStore)) {
                            v3 = v3;
                            garbage$ensure_root_ref.__cached_class__0 = Util.classOf((Object)v3);
lbl12:
                            // 2 sources

                            v4 = garbage$ensure_root_ref.const__3.getRawRoot().invoke(v3, root_key);
                        } else {
                            v4 = ((ClusteredStore)v3).get_ref(root_key);
                        }
                        v5 = and__5236__auto__19776 = (result = v2.invoke(v4));
                        if (v5 != null && v5 != Boolean.FALSE) {
                            v6 = forget_garbage;
                            forget_garbage = null;
                            v7 = ((IFn)garbage$ensure_root_ref.const__4.getRawRoot()).invoke(v6);
                        } else {
                            v7 = and__5236__auto__19776;
                            and__5236__auto__19776 = null;
                        }
                        if (v7 == null || v7 == Boolean.FALSE) break block9;
                        v8 = result;
                        result = null;
                        break block10;
                    }
                    root_uuid = ((IFn)garbage$ensure_root_ref.const__5.getRawRoot()).invoke();
                    v9 = create = new garbage$ensure_root_ref$create__19773(cluster);
                    create = null;
                    and__5236__auto__19777 = Util.equiv((Object)garbage$ensure_root_ref.const__7, (Object)((IFn)garbage$ensure_root_ref.const__2.getRawRoot()).invoke(((IFn)v9).invoke(root_uuid, ((IFn)garbage$ensure_root_ref.const__8.getRawRoot()).invoke((Object)PersistentVector.EMPTY))));
                    if (and__5236__auto__19777) {
                        v10 = root_uuid;
                        root_uuid = null;
                        v11 = Util.equiv((Object)garbage$ensure_root_ref.const__9, (Object)((IFn)garbage$ensure_root_ref.const__2.getRawRoot()).invoke(((IFn)garbage$ensure_root_ref.const__10.getRawRoot()).invoke(cluster, root_key, ((IFn)garbage$ensure_root_ref.const__11.getRawRoot()).invoke(v10))));
                    } else {
                        v11 = and__5236__auto__19777;
                    }
                    if (!v11) break block11;
                    v12 = (IFn)garbage$ensure_root_ref.const__2.getRawRoot();
                    v13 = cluster;
                    cluster = null;
                    v14 = v13;
                    if (Util.classOf((Object)v13) == garbage$ensure_root_ref.__cached_class__1) ** GOTO lbl47
                    if (!(v14 instanceof ClusteredStore)) {
                        v14 = v14;
                        garbage$ensure_root_ref.__cached_class__1 = Util.classOf((Object)v14);
lbl47:
                        // 2 sources

                        v15 = root_key;
                        root_key = null;
                        v16 = garbage$ensure_root_ref.const__3.getRawRoot().invoke(v14, v15);
                    } else {
                        v17 = root_key;
                        root_key = null;
                        v16 = ((ClusteredStore)v14).get_ref(v17);
                    }
                    v8 = v12.invoke(v16);
                    break block10;
                }
                throw (Throwable)new Exception("Garbage root creation failed");
            }
            v8 = null;
        }
        return v8;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$ensure_root_ref.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return garbage$ensure_root_ref.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.garbage", (String)"ensure-root-ref");
        const__1 = RT.var((String)"datomic.garbage", (String)"root-ref-key");
        const__2 = RT.var((String)"clojure.core", (String)"deref");
        const__3 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__4 = RT.var((String)"clojure.core", (String)"not");
        const__5 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__7 = RT.keyword(null, (String)"created");
        const__8 = RT.var((String)"datomic.garbage.fressian", (String)"->GarbageRoot");
        const__9 = RT.keyword(null, (String)"ok");
        const__10 = RT.var((String)"datomic.cluster", (String)"reset-ref");
        const__11 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    }
}

