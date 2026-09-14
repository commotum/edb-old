/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Database;
import datomic.Entity;

public final class db$ensure_entity_BANG_
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"missing-attrs");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__5 = RT.keyword((String)"db.error", (String)"entity-attr");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"clojure.set", (String)"map-invert");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"ensure-pred");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db.entity", (String)"attrs"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"db.entity", (String)"preds"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db_before, Object db_after, Object e, Object spec, Object idmap) {
        Object v25;
        Entity entity2;
        Entity spec_ent = ((Database)db_before).entity(spec);
        ILookupThunk iLookupThunk = __thunk__0__;
        Entity entity3 = spec_ent;
        Object object = iLookupThunk.get((Object)entity3);
        if (iLookupThunk == object) {
            __thunk__0__ = __site__0__.fault((Object)entity3);
            object = __thunk__0__.get((Object)entity3);
        }
        Object required = object;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Entity entity4 = spec_ent;
        spec_ent = null;
        Object object2 = iLookupThunk2.get((Object)entity4);
        if (iLookupThunk2 == object2) {
            __thunk__1__ = __site__1__.fault((Object)entity4);
            object2 = __thunk__1__.get((Object)entity4);
        }
        Object preds = object2;
        Entity entity5 = entity2 = ((Database)db_after).entity(e);
        entity2 = null;
        Object object3 = required;
        required = null;
        Object missing = ((IFn)const__2.getRawRoot()).invoke((Object)entity5, object3);
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(missing);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = missing;
            missing = null;
            Object object6 = db_before;
            db_before = null;
            ((IFn)const__4.getRawRoot()).invoke((Object)const__5, ((IFn)const__6.getRawRoot()).invoke((Object)"Entity ", RT.get((Object)((IFn)const__8.getRawRoot()).invoke(idmap), (Object)e, (Object)e), (Object)" missing attributes ", object5, (Object)" of spec ", ((IFn)const__9.getRawRoot()).invoke(object6, spec)));
        }
        Object object7 = ((IFn)const__3.getRawRoot()).invoke(preds);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = preds;
            preds = null;
            Object seq_14075 = ((IFn)const__3.getRawRoot()).invoke(object8);
            Object chunk_14076 = null;
            long count_14077 = 0L;
            long i_14078 = 0L;
            while (true) {
                Object pred2;
                Object temp__5457__auto__14081;
                if (i_14078 < count_14077) {
                    Object pred3;
                    Object object9 = pred3 = ((Indexed)chunk_14076).nth(RT.uncheckedIntCast((long)i_14078));
                    pred3 = null;
                    ((IFn)const__12.getRawRoot()).invoke(object9, db_after, e, spec, idmap);
                    Object object10 = seq_14075;
                    seq_14075 = null;
                    Object object11 = chunk_14076;
                    chunk_14076 = null;
                    ++i_14078;
                    chunk_14076 = object11;
                    seq_14075 = object10;
                    continue;
                }
                Object object12 = seq_14075;
                seq_14075 = null;
                Object object13 = temp__5457__auto__14081 = ((IFn)const__3.getRawRoot()).invoke(object12);
                if (object13 == null || object13 == Boolean.FALSE) break;
                Object object14 = temp__5457__auto__14081;
                temp__5457__auto__14081 = null;
                Object seq_140752 = object14;
                Object object15 = ((IFn)const__14.getRawRoot()).invoke(seq_140752);
                if (object15 != null && object15 != Boolean.FALSE) {
                    Object c__5719__auto__14080 = ((IFn)const__15.getRawRoot()).invoke(seq_140752);
                    Object object16 = seq_140752;
                    seq_140752 = null;
                    Object object17 = c__5719__auto__14080;
                    Object object18 = c__5719__auto__14080;
                    c__5719__auto__14080 = null;
                    i_14078 = (int)0L;
                    count_14077 = RT.count((Object)object18);
                    chunk_14076 = object17;
                    seq_14075 = ((IFn)const__16.getRawRoot()).invoke(object16);
                    continue;
                }
                Object object19 = pred2 = ((IFn)const__19.getRawRoot()).invoke(seq_140752);
                pred2 = null;
                ((IFn)const__12.getRawRoot()).invoke(object19, db_after, e, spec, idmap);
                Object object20 = seq_140752;
                seq_140752 = null;
                i_14078 = 0L;
                count_14077 = 0L;
                chunk_14076 = null;
                seq_14075 = ((IFn)const__20.getRawRoot()).invoke(object20);
            }
            v25 = null;
        } else {
            v25 = null;
        }
        return v25;
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
        return db$ensure_entity_BANG_.invokeStatic(object6, object7, object8, object9, object10);
    }
}

