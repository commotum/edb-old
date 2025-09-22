/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.ValueBackup$fn__20091$fn__20093;
import datomic.cluster.ClusteredStore;

public final class ValueBackup$fn__20091
extends AFunction {
    Object from_cluster;
    Object backup_k;
    Object progress;
    Object value_storage;
    Object k;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__6;
    public static final Var const__7;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Keyword const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Keyword const__15;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public ValueBackup$fn__20091(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.from_cluster = object;
        this.backup_k = object2;
        this.progress = object3;
        this.value_storage = object4;
        this.k = object5;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = ValueBackup$fn__20091.const__0;
            v1 = (IFn)ValueBackup$fn__20091.const__1.getRawRoot();
            v2 = this.from_cluster;
            if (Util.classOf((Object)v2) == ValueBackup$fn__20091.__cached_class__0) ** GOTO lbl10
            if (!(v2 instanceof ClusteredStore)) {
                v2 = v2;
                ValueBackup$fn__20091.__cached_class__0 = Util.classOf((Object)v2);
lbl10:
                // 2 sources

                v3 = ValueBackup$fn__20091.const__2.getRawRoot().invoke(v2, this.k);
            } else {
                v3 = ((ClusteredStore)v2).get_val(this.k);
            }
            v4 = temp__5455__auto__20096 = v1.invoke(v3);
            if (v4 != null && v4 != Boolean.FALSE) {
                v5 = temp__5455__auto__20096;
                temp__5455__auto__20096 = null;
                map__20092 = v5;
                v6 = ((IFn)ValueBackup$fn__20091.const__3.getRawRoot()).invoke(map__20092);
                if (v6 != null && v6 != Boolean.FALSE) {
                    v7 = map__20092;
                    map__20092 = null;
                    v8 = PersistentHashMap.create((ISeq)((ISeq)((IFn)ValueBackup$fn__20091.const__4.getRawRoot()).invoke(v7)));
                } else {
                    v8 = map__20092;
                    map__20092 = null;
                }
                v9 = map__20092 = v8;
                map__20092 = null;
                buf = RT.get((Object)v9, (Object)ValueBackup$fn__20091.const__6);
                this.backup_k = null;
                v10 = buf;
                buf = null;
                result = ((IFn)ValueBackup$fn__20091.const__7.getRawRoot()).invoke((Object)new ValueBackup$fn__20091$fn__20093(this.backup_k, this.value_storage, v10));
                v11 = ValueBackup$fn__20091.__thunk__0__;
                v12 = result;
                v13 = v11.get(v12);
                if (v11 == v13) {
                    ValueBackup$fn__20091.__thunk__0__ = ValueBackup$fn__20091.__site__0__.fault(v12);
                    v13 = ValueBackup$fn__20091.__thunk__0__.get(v12);
                }
                if (v13 != null && v13 != Boolean.FALSE) {
                    v14 = ((IFn)this.progress).invoke((Object)ValueBackup$fn__20091.const__9);
                } else {
                    this.k = null;
                    v15 = result;
                    result = null;
                    v14 = ((IFn)ValueBackup$fn__20091.const__10.getRawRoot()).invoke((Object)ValueBackup$fn__20091.const__11, ((IFn)ValueBackup$fn__20091.const__12.getRawRoot()).invoke((Object)"Unable to backup ", this.k), v15);
                }
            } else {
                v14 = ((IFn)ValueBackup$fn__20091.const__10.getRawRoot()).invoke((Object)ValueBackup$fn__20091.const__13, ((IFn)ValueBackup$fn__20091.const__12.getRawRoot()).invoke((Object)"Unable to read ", this.k), (Object)RT.mapUniqueKeys((Object[])new Object[]{ValueBackup$fn__20091.const__14, this.k = null}));
            }
            v0[1] = v14;
            var6_8 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v16 = new Object[2];
            v16[0] = ValueBackup$fn__20091.const__15;
            t__8983__auto__ = null;
            v16[1] = t__8983__auto__;
            var6_8 = RT.mapUniqueKeys((Object[])v16);
        }
        return var6_8;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"clojure.core", (String)"deref");
        const__2 = RT.var((String)"datomic.cluster", (String)"get-val");
        const__3 = RT.var((String)"clojure.core", (String)"seq?");
        const__4 = RT.var((String)"clojure.core", (String)"seq");
        const__6 = RT.keyword(null, (String)"buf");
        const__7 = RT.var((String)"datomic.backup", (String)"retry");
        const__9 = RT.keyword(null, (String)"copied");
        const__10 = RT.var((String)"datomic.error", (String)"raise");
        const__11 = RT.keyword((String)"backup", (String)"value-failed");
        const__12 = RT.var((String)"clojure.core", (String)"str");
        const__13 = RT.keyword((String)"backup", (String)"read-failed");
        const__14 = RT.keyword(null, (String)"key");
        const__15 = RT.keyword(null, (String)"threw");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"k"));
        __thunk__0__ = __site__0__;
    }
}

