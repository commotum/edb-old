/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class cluster$clone_ref
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Object const__10;
    public static final AFn const__11;
    public static final AFn const__14;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs, Object from_key, Object to_key) {
        block7: {
            block6: {
                v0 = (IFn)cluster$clone_ref.const__0.getRawRoot();
                v1 = cs;
                if (Util.classOf((Object)v1) == cluster$clone_ref.__cached_class__0) ** GOTO lbl7
                if (!(v1 instanceof ClusteredStore)) {
                    v1 = v1;
                    cluster$clone_ref.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
                    // 2 sources

                    v2 = from_key;
                    from_key = null;
                    v3 = cluster$clone_ref.const__1.getRawRoot().invoke(v1, v2);
                } else {
                    v4 = from_key;
                    from_key = null;
                    v3 = ((ClusteredStore)v1).get_ref(v4);
                }
                v5 = temp__5455__auto__10653 = v0.invoke(v3);
                if (v5 == null || v5 == Boolean.FALSE) break block6;
                v6 = temp__5455__auto__10653;
                temp__5455__auto__10653 = null;
                map__10651 = v6;
                v7 = ((IFn)cluster$clone_ref.const__2.getRawRoot()).invoke(map__10651);
                if (v7 != null && v7 != Boolean.FALSE) {
                    v8 = map__10651;
                    map__10651 = null;
                    v9 = PersistentHashMap.create((ISeq)((ISeq)((IFn)cluster$clone_ref.const__3.getRawRoot()).invoke(v8)));
                } else {
                    v9 = map__10651;
                    map__10651 = null;
                }
                map__10651 = v9;
                RT.get((Object)map__10651, (Object)cluster$clone_ref.const__5);
                v10 = map__10651;
                map__10651 = null;
                key = RT.get((Object)v10, (Object)cluster$clone_ref.const__6);
                v11 = (IFn)cluster$clone_ref.const__0.getRawRoot();
                v12 = cs;
                cs = null;
                v13 = v12;
                if (Util.classOf((Object)v12) == cluster$clone_ref.__cached_class__1) ** GOTO lbl41
                if (!(v13 instanceof ClusteredStore)) {
                    v13 = v13;
                    cluster$clone_ref.__cached_class__1 = Util.classOf((Object)v13);
lbl41:
                    // 2 sources

                    v14 = to_key;
                    to_key = null;
                    v15 = key;
                    key = null;
                    v16 = cluster$clone_ref.const__9.getRawRoot().invoke(v13, v14, cluster$clone_ref.const__10, v15);
                } else {
                    v17 = to_key;
                    to_key = null;
                    v18 = key;
                    key = null;
                    v16 = ((ClusteredStore)v13).set_ref(v17, cluster$clone_ref.const__10, v18);
                }
                v19 = Util.equiv((Object)cluster$clone_ref.const__8, (Object)v11.invoke(v16)) ? cluster$clone_ref.const__11 : cluster$clone_ref.const__14;
                break block7;
            }
            v19 = null;
        }
        return v19;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cluster$clone_ref.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"rev");
        const__6 = RT.keyword(null, (String)"key");
        const__8 = RT.keyword(null, (String)"ok");
        const__9 = RT.var((String)"datomic.cluster", (String)"set-ref");
        const__10 = 0L;
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"rev"), 0L});
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"failed"), RT.keyword(null, (String)"conflict")});
    }
}

