/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.ValueRestore$fn__20174$fn__20175;
import datomic.cluster.ClusteredStore;

public final class ValueRestore$fn__20174
extends AFunction {
    Object value_storage;
    Object progress;
    Object k__GT_backup_k;
    Object to_cluster;
    Object k;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Keyword const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;

    public ValueRestore$fn__20174(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.value_storage = object;
        this.progress = object2;
        this.k__GT_backup_k = object3;
        this.to_cluster = object4;
        this.k = object5;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            block7: {
                block6: {
                    v0 = new Object[2];
                    v0[0] = ValueRestore$fn__20174.const__0;
                    v1 = temp__5455__auto__20179 = ((IFn)ValueRestore$fn__20174.const__1.getRawRoot()).invoke((Object)new ValueRestore$fn__20174$fn__20175(this.value_storage, this.k__GT_backup_k, this.k));
                    if (v1 == null || v1 == Boolean.FALSE) break block6;
                    v2 = temp__5455__auto__20179;
                    temp__5455__auto__20179 = null;
                    map__20177 = v2;
                    v3 = ((IFn)ValueRestore$fn__20174.const__2.getRawRoot()).invoke(map__20177);
                    if (v3 != null && v3 != Boolean.FALSE) {
                        v4 = map__20177;
                        map__20177 = null;
                        v5 = PersistentHashMap.create((ISeq)((ISeq)((IFn)ValueRestore$fn__20174.const__3.getRawRoot()).invoke(v4)));
                    } else {
                        v5 = map__20177;
                        map__20177 = null;
                    }
                    v6 = map__20177 = v5;
                    map__20177 = null;
                    v = RT.get((Object)v6, (Object)ValueRestore$fn__20174.const__5);
                    v7 = (IFn)ValueRestore$fn__20174.const__6.getRawRoot();
                    v8 = this.to_cluster;
                    if (Util.classOf((Object)v8) == ValueRestore$fn__20174.__cached_class__0) ** GOTO lbl26
                    if (!(v8 instanceof ClusteredStore)) {
                        v8 = v8;
                        ValueRestore$fn__20174.__cached_class__0 = Util.classOf((Object)v8);
lbl26:
                        // 2 sources

                        this.k = null;
                        v9 = v;
                        v = null;
                        v10 = ValueRestore$fn__20174.const__7.getRawRoot().invoke(v8, this.k, v9);
                    } else {
                        this.k = null;
                        v11 = v;
                        v = null;
                        v10 = ((ClusteredStore)v8).create_val(this.k, v11);
                    }
                    v7.invoke(v10);
                    v12 = ((IFn)this.progress).invoke((Object)ValueRestore$fn__20174.const__8);
                    break block7;
                }
                v12 = ((IFn)ValueRestore$fn__20174.const__9.getRawRoot()).invoke((Object)ValueRestore$fn__20174.const__10, ((IFn)ValueRestore$fn__20174.const__11.getRawRoot()).invoke((Object)"Unable to read ", this.k), (Object)RT.mapUniqueKeys((Object[])new Object[]{ValueRestore$fn__20174.const__12, this.k = null}));
            }
            v0[1] = v12;
            var5_7 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v13 = new Object[2];
            v13[0] = ValueRestore$fn__20174.const__13;
            t__8983__auto__ = null;
            v13[1] = t__8983__auto__;
            var5_7 = RT.mapUniqueKeys((Object[])v13);
        }
        return var5_7;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"datomic.backup", (String)"retry");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"v");
        const__6 = RT.var((String)"clojure.core", (String)"deref");
        const__7 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__8 = RT.keyword(null, (String)"copied");
        const__9 = RT.var((String)"datomic.error", (String)"raise");
        const__10 = RT.keyword((String)"restore", (String)"read-failed");
        const__11 = RT.var((String)"clojure.core", (String)"str");
        const__12 = RT.keyword(null, (String)"key");
        const__13 = RT.keyword(null, (String)"threw");
    }
}

