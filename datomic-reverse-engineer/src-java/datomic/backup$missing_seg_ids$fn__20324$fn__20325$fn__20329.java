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
import datomic.backup$missing_seg_ids$fn__20324$fn__20325$fn__20329$fn__20330;
import datomic.backup.Storage;

public final class backup$missing_seg_ids$fn__20324$fn__20325$fn__20329
extends AFunction {
    Object seg_id_set;
    Object value_substorage;
    Object fill;
    Object prefix;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__6;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public backup$missing_seg_ids$fn__20324$fn__20325$fn__20329(Object object, Object object2, Object object3, Object object4) {
        this.seg_id_set = object;
        this.value_substorage = object2;
        this.fill = object3;
        this.prefix = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.const__0;
            v1 = (IFn)backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.const__1.getRawRoot();
            this.fill = null;
            v2 = (IFn)backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.const__2.getRawRoot();
            this.seg_id_set = null;
            v3 = new backup$missing_seg_ids$fn__20324$fn__20325$fn__20329$fn__20330(this.seg_id_set);
            v4 = backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.__thunk__0__;
            this.value_substorage = null;
            this.prefix = null;
            v5 = ((IFn)backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.const__5.getRawRoot()).invoke(this.value_substorage, this.prefix);
            if (Util.classOf((Object)v5) == backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.__cached_class__0) ** GOTO lbl17
            if (!(v5 instanceof Storage)) {
                v5 = v5;
                backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.__cached_class__0 = Util.classOf((Object)v5);
lbl17:
                // 2 sources

                v6 = backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.const__4.getRawRoot().invoke(v5, (Object)"");
            } else {
                v6 = ((Storage)v5).list_keys("");
            }
            if (v4 == (v7 = v4.get(v6))) {
                backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.__thunk__0__ = backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.__site__0__.fault(v6);
                v7 = backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.__thunk__0__.get(v6);
            }
            v0[1] = v1.invoke(this.fill, v2.invoke((Object)v3, v7));
            var1_1 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v8 = new Object[2];
            v8[0] = backup$missing_seg_ids$fn__20324$fn__20325$fn__20329.const__6;
            t__8983__auto__ = null;
            v8[1] = t__8983__auto__;
            var1_1 = RT.mapUniqueKeys((Object[])v8);
        }
        return var1_1;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"clojure.core", (String)"run!");
        const__2 = RT.var((String)"clojure.core", (String)"filter");
        const__4 = RT.var((String)"datomic.backup", (String)"list-keys");
        const__5 = RT.var((String)"datomic.backup", (String)"substorage");
        const__6 = RT.keyword(null, (String)"threw");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"ks"));
        __thunk__0__ = __site__0__;
    }
}

