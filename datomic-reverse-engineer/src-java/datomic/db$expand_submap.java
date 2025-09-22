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
 *  clojure.lang.Tuple
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
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.AssignPartitions;
import datomic.db.Attribute;
import datomic.db.LocalizeTempid;

public final class db$expand_submap
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final Var const__9;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object db, Object parentid, Object attrid, Object v, Object part_reqs, Object local_tempids) {
        v0 = v;
        v = null;
        v = ((IFn)db$expand_submap.const__0.getRawRoot()).invoke(db, ((IFn)db$expand_submap.const__1.getRawRoot()).invoke(v0));
        v1 = db$expand_submap.__thunk__0__;
        v2 = v;
        v3 = v1.get(v2);
        if (v1 == v3) {
            db$expand_submap.__thunk__0__ = db$expand_submap.__site__0__.fault(v2);
            v3 = db$expand_submap.__thunk__0__.get(v2);
        }
        v4 = or__5238__auto__13722 = v3;
        if (v4 != null && v4 != Boolean.FALSE) {
            v5 = or__5238__auto__13722;
            or__5238__auto__13722 = null;
        } else {
            v5 = v6 = ((IFn)db$expand_submap.const__4.getRawRoot()).invoke(db, parentid, attrid, v);
        }
        if (Util.classOf((Object)v5) == db$expand_submap.__cached_class__0) ** GOTO lbl21
        if (!(v6 instanceof LocalizeTempid)) {
            v6 = v6;
            db$expand_submap.__cached_class__0 = Util.classOf((Object)v6);
lbl21:
            // 2 sources

            v7 = db$expand_submap.const__2.getRawRoot().invoke(v6, db, null, local_tempids);
        } else {
            v7 = ((LocalizeTempid)v6).local_id(db, null, local_tempids);
        }
        childid = v7;
        v8 = ((Attribute)((IFn)db$expand_submap.const__5.getRawRoot()).invoke((Object)db, (Object)attrid)).isComponent;
        if (v8 != null && v8 != Boolean.FALSE) {
            ((AssignPartitions)part_reqs).matchPart(childid, parentid);
        }
        v9 = parentid;
        parentid = null;
        v10 = attrid;
        attrid = null;
        v11 = Tuple.create((Object)db$expand_submap.const__7, (Object)v9, (Object)v10, (Object)childid);
        v12 = db;
        db = null;
        v13 = v;
        v = null;
        v14 = childid;
        childid = null;
        v15 = part_reqs;
        part_reqs = null;
        v16 = local_tempids;
        local_tempids = null;
        return ((IFn)db$expand_submap.const__6.getRawRoot()).invoke((Object)v11, ((IFn)db$expand_submap.const__8.getRawRoot()).invoke(v12, ((IFn)db$expand_submap.const__9.getRawRoot()).invoke(v13, (Object)db$expand_submap.const__3, v14), v15, v16));
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
        return db$expand_submap.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    static {
        const__0 = RT.var((String)"datomic.db", (String)"force-map-keywords");
        const__1 = RT.var((String)"datomic.db", (String)"normalize-map");
        const__2 = RT.var((String)"datomic.db", (String)"local-id");
        const__3 = RT.keyword((String)"db", (String)"id");
        const__4 = RT.var((String)"datomic.db", (String)"make-child-id");
        const__5 = RT.var((String)"datomic.db", (String)"attribute");
        const__6 = RT.var((String)"clojure.core", (String)"cons");
        const__7 = RT.keyword((String)"db", (String)"add");
        const__8 = RT.var((String)"datomic.db", (String)"expand-map");
        const__9 = RT.var((String)"clojure.core", (String)"assoc");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"id"));
        __thunk__0__ = __site__0__;
    }
}

