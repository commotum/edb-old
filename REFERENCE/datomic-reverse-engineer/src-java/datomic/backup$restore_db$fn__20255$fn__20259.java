/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.IValueRestore;

public final class backup$restore_db$fn__20255$fn__20259
extends AFunction {
    Object log_root_node;
    Object job;
    Object index_top_node;
    Object to_cluster;
    Object restore;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__5;

    public backup$restore_db$fn__20255$fn__20259(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.log_root_node = object;
        this.job = object2;
        this.index_top_node = object3;
        this.to_cluster = object4;
        this.restore = object5;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = backup$restore_db$fn__20255$fn__20259.const__0;
            v1 = ((IFn)backup$restore_db$fn__20255$fn__20259.const__1.getRawRoot()).invoke(this.to_cluster, (Object)backup$restore_db$fn__20255$fn__20259.const__2);
            if (v1 == null || v1 == Boolean.FALSE) {
                throw (Throwable)new Error("Unable to clear garbage root");
            }
            v2 = this.restore;
            if (Util.classOf((Object)v2) == backup$restore_db$fn__20255$fn__20259.__cached_class__0) ** GOTO lbl12
            if (!(v2 instanceof IValueRestore)) {
                v2 = v2;
                backup$restore_db$fn__20255$fn__20259.__cached_class__0 = Util.classOf((Object)v2);
lbl12:
                // 2 sources

                this.log_root_node = null;
                v3 = backup$restore_db$fn__20255$fn__20259.const__3.getRawRoot().invoke(v2, this.log_root_node);
            } else {
                this.log_root_node = null;
                v3 = ((IValueRestore)v2).restore_node(this.log_root_node);
            }
            v4 = this.restore;
            this.restore = null;
            v5 = v4;
            if (Util.classOf((Object)v4) == backup$restore_db$fn__20255$fn__20259.__cached_class__1) ** GOTO lbl24
            if (!(v5 instanceof IValueRestore)) {
                v5 = v5;
                backup$restore_db$fn__20255$fn__20259.__cached_class__1 = Util.classOf((Object)v5);
lbl24:
                // 2 sources

                this.index_top_node = null;
                v6 = backup$restore_db$fn__20255$fn__20259.const__3.getRawRoot().invoke(v5, this.index_top_node);
            } else {
                this.index_top_node = null;
                v6 = ((IValueRestore)v5).restore_node(this.index_top_node);
            }
            this.job = null;
            this.to_cluster = null;
            v0[1] = ((IFn)backup$restore_db$fn__20255$fn__20259.const__4.getRawRoot()).invoke(this.job, this.to_cluster);
            var1_1 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v7 = new Object[2];
            v7[0] = backup$restore_db$fn__20255$fn__20259.const__5;
            t__8983__auto__ = null;
            v7[1] = t__8983__auto__;
            var1_1 = RT.mapUniqueKeys((Object[])v7);
        }
        return var1_1;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"datomic.garbage", (String)"ensure-root-ref");
        const__2 = RT.keyword(null, (String)"forget-garbage");
        const__3 = RT.var((String)"datomic.backup", (String)"restore-node");
        const__4 = RT.var((String)"datomic.backup", (String)"restore-roots");
        const__5 = RT.keyword(null, (String)"threw");
    }
}

