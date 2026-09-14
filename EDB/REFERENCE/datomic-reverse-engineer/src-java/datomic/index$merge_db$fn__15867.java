/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.db.Db;
import datomic.index$merge_db$fn__15867$fn__15868;
import datomic.index$merge_db$fn__15867$fn__15871;

public final class index$merge_db$fn__15867
extends AFunction {
    Object cstore;
    Object db;
    Object olookup;
    Object as_of_t;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Object const__19;
    public static final Keyword const__20;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public index$merge_db$fn__15867(Object object, Object object2, Object object3, Object object4) {
        this.cstore = object;
        this.db = object2;
        this.olookup = object3;
        this.as_of_t = object4;
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = index$merge_db$fn__15867.const__0;
            index_ref_key = ((IFn)index$merge_db$fn__15867.const__1.getRawRoot()).invoke(this.cstore);
            v1 = (IFn)index$merge_db$fn__15867.const__2.getRawRoot();
            v2 = this.cstore;
            if (Util.classOf((Object)v2) == index$merge_db$fn__15867.__cached_class__0) ** GOTO lbl11
            if (!(v2 instanceof ClusteredStore)) {
                v2 = v2;
                index$merge_db$fn__15867.__cached_class__0 = Util.classOf((Object)v2);
lbl11:
                // 2 sources

                v3 = index$merge_db$fn__15867.const__3.getRawRoot().invoke(v2, ((IFn)index$merge_db$fn__15867.const__1.getRawRoot()).invoke(this.cstore));
            } else {
                v3 = ((ClusteredStore)v2).get_ref(((IFn)index$merge_db$fn__15867.const__1.getRawRoot()).invoke(this.cstore));
            }
            storage_index_root_ref = v1.invoke(v3);
            v4 = (IFn)index$merge_db$fn__15867.const__4.getRawRoot();
            v5 = index$merge_db$fn__15867.__thunk__0__;
            v6 = storage_index_root_ref;
            v7 = v5.get(v6);
            if (v5 == v7) {
                index$merge_db$fn__15867.__thunk__0__ = index$merge_db$fn__15867.__site__0__.fault(v6);
                v7 = index$merge_db$fn__15867.__thunk__0__.get(v6);
            }
            storage_index_root_id = v4.invoke(v7);
            root_map = ((IFn)index$merge_db$fn__15867.const__6.getRawRoot()).invoke(this.olookup, storage_index_root_id);
            this.db = null;
            db = ((IFn)index$merge_db$fn__15867.const__7.getRawRoot()).invoke(this.db, this.olookup, root_map);
            v8 = root_map;
            root_map = null;
            if (Numbers.gt((Object)((IFn)index$merge_db$fn__15867.const__6.getRawRoot()).invoke(v8, (Object)index$merge_db$fn__15867.const__9), (Object)((Db)db).index_rev)) {
                v9 = new Object[2];
                v9[0] = index$merge_db$fn__15867.const__10;
                this.olookup = null;
                v10 = storage_index_root_id;
                storage_index_root_id = null;
                v9[1] = ((IFn)index$merge_db$fn__15867.const__11.getRawRoot()).invoke(this.olookup, v10);
                v11 /* !! */  = RT.mapUniqueKeys((Object[])v9);
            } else {
                v12 = (IFn)index$merge_db$fn__15867.const__12.getRawRoot();
                v13 = (IFn)index$merge_db$fn__15867.const__13.getRawRoot();
                v14 = temp__5457__auto__15877 = ((IFn)index$merge_db$fn__15867.const__15.getRawRoot()).invoke((Object)"datomic.indexSegsPerSecond");
                if (v14 != null && v14 != Boolean.FALSE) {
                    v15 = temp__5457__auto__15877;
                    temp__5457__auto__15877 = null;
                    v16 = sps = v15;
                    sps = null;
                    calc = ((IFn)index$merge_db$fn__15867.const__16.getRawRoot()).invoke((Object)Numbers.divide((Object)v16, (long)10L), index$merge_db$fn__15867.const__19);
                    calc = null;
                    v17 = new index$merge_db$fn__15867$fn__15868(calc);
                } else {
                    v17 = null;
                }
                v12.invoke(v13.invoke((Object)index$merge_db$fn__15867.const__14, v17));
                this.cstore = null;
                v18 = storage_index_root_ref;
                storage_index_root_ref = null;
                this.olookup = null;
                v19 = db;
                db = null;
                v20 = index_ref_key;
                index_ref_key = null;
                this.as_of_t = null;
                v11 /* !! */  = ((IFn)new index$merge_db$fn__15867$fn__15871(this.cstore, v18, this.olookup, v19, v20, this.as_of_t)).invoke();
            }
            v0[1] = v11 /* !! */ ;
            var9_11 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v21 = new Object[2];
            v21[0] = index$merge_db$fn__15867.const__20;
            t__8983__auto__ = null;
            v21[1] = t__8983__auto__;
            var9_11 = RT.mapUniqueKeys((Object[])v21);
        }
        return var9_11;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        const__2 = RT.var((String)"clojure.core", (String)"deref");
        const__3 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__4 = RT.var((String)"datomic.cluster", (String)"val-key->uuid");
        const__6 = RT.var((String)"datomic.common", (String)"getx");
        const__7 = RT.var((String)"datomic.index", (String)"repair-disjoined");
        const__9 = RT.keyword(null, (String)"rev");
        const__10 = RT.keyword(null, (String)"new-index");
        const__11 = RT.var((String)"datomic.index", (String)"load-index");
        const__12 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
        const__13 = RT.var((String)"clojure.core", (String)"hash-map");
        const__14 = RT.var((String)"datomic.index", (String)"*pace-index-fn*");
        const__15 = RT.var((String)"datomic.config", (String)"property");
        const__16 = RT.var((String)"datomic.index", (String)"create-pace-calculator");
        const__19 = 100L;
        const__20 = RT.keyword(null, (String)"threw");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
    }
}

