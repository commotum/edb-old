/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.IValueBackup;

public final class backup$backup_db$fn__20298$fn__20302
extends AFunction {
    Object t;
    Object to_storage;
    Object log_root_node;
    Object job;
    Object backup;
    Object index_top_node;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public backup$backup_db$fn__20298$fn__20302(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.t = object;
        this.to_storage = object2;
        this.log_root_node = object3;
        this.job = object4;
        this.backup = object5;
        this.index_top_node = object6;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = backup$backup_db$fn__20298$fn__20302.const__0;
            v1 = this.backup;
            if (Util.classOf((Object)v1) == backup$backup_db$fn__20298$fn__20302.__cached_class__0) ** GOTO lbl9
            if (!(v1 instanceof IValueBackup)) {
                v1 = v1;
                backup$backup_db$fn__20298$fn__20302.__cached_class__0 = Util.classOf((Object)v1);
lbl9:
                // 2 sources

                this.index_top_node = null;
                v2 = backup$backup_db$fn__20298$fn__20302.const__1.getRawRoot().invoke(v1, this.index_top_node);
            } else {
                this.index_top_node = null;
                v2 = ((IValueBackup)v1).backup_node(this.index_top_node);
            }
            v3 = this.backup;
            this.backup = null;
            v4 = v3;
            if (Util.classOf((Object)v3) == backup$backup_db$fn__20298$fn__20302.__cached_class__1) ** GOTO lbl21
            if (!(v4 instanceof IValueBackup)) {
                v4 = v4;
                backup$backup_db$fn__20298$fn__20302.__cached_class__1 = Util.classOf((Object)v4);
lbl21:
                // 2 sources

                this.log_root_node = null;
                v5 = backup$backup_db$fn__20298$fn__20302.const__1.getRawRoot().invoke(v4, this.log_root_node);
            } else {
                this.log_root_node = null;
                v5 = ((IValueBackup)v4).backup_node(this.log_root_node);
            }
            v6 = backup$backup_db$fn__20298$fn__20302.__thunk__0__;
            this.job = null;
            this.t = null;
            this.to_storage = null;
            v7 = ((IFn)backup$backup_db$fn__20298$fn__20302.const__3.getRawRoot()).invoke(this.job, this.t, this.to_storage);
            v8 = v6.get(v7);
            if (v6 == v8) {
                backup$backup_db$fn__20298$fn__20302.__thunk__0__ = backup$backup_db$fn__20298$fn__20302.__site__0__.fault(v7);
                v8 = backup$backup_db$fn__20298$fn__20302.__thunk__0__.get(v7);
            }
            if (v8 == null || v8 == Boolean.FALSE) {
                throw (Throwable)new RuntimeException("Backup roots failed");
            }
            v0[1] = backup$backup_db$fn__20298$fn__20302.const__4;
            var1_1 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v9 = new Object[2];
            v9[0] = backup$backup_db$fn__20298$fn__20302.const__5;
            t__8983__auto__ = null;
            v9[1] = t__8983__auto__;
            var1_1 = RT.mapUniqueKeys((Object[])v9);
        }
        return var1_1;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"datomic.backup", (String)"backup-node");
        const__3 = RT.var((String)"datomic.backup", (String)"backup-roots");
        const__4 = RT.keyword(null, (String)"succeeded");
        const__5 = RT.keyword(null, (String)"threw");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"k"));
        __thunk__0__ = __site__0__;
    }
}

