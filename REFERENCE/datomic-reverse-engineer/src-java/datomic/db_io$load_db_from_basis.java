/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class db_io$load_db_from_basis
extends RestFn {
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"find-index-root-id");
    public static final Var const__3 = RT.var((String)"datomic.log", (String)"find-log");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"db");
    public static final Var const__5 = RT.var((String)"datomic.index", (String)"load-index");
    public static final Var const__6 = RT.var((String)"datomic.db-io", (String)"most-current-db");
    public static final Var const__8 = RT.var((String)"datomic.log", (String)"catchup");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_db_conn, Object olookup, Object db_id, ISeq p__17074) {
        ISeq vec__17075;
        ISeq iSeq = p__17074;
        p__17074 = null;
        ISeq iSeq2 = vec__17075 = iSeq;
        vec__17075 = null;
        Object basis_db = RT.nth((Object)iSeq2, (int)RT.intCast((long)0L), null);
        Object idxroot = ((IFn)const__2.getRawRoot()).invoke(cluster_db_conn);
        Object object = cluster_db_conn;
        cluster_db_conn = null;
        Object log2 = ((IFn)const__3.getRawRoot()).invoke(object, olookup);
        Object object2 = db_id;
        db_id = null;
        Object object3 = olookup;
        olookup = null;
        Object object4 = idxroot;
        idxroot = null;
        Object db2 = ((IFn)const__4.getRawRoot()).invoke(object2, ((IFn)const__5.getRawRoot()).invoke(object3, object4));
        Object object5 = basis_db;
        basis_db = null;
        Object object6 = db2;
        db2 = null;
        Object db3 = ((IFn)const__6.getRawRoot()).invoke(object5, object6);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = db3;
        db3 = null;
        Object object8 = log2;
        log2 = null;
        Object object9 = ((IFn)const__8.getRawRoot()).invoke(object7, object8, (Object)Boolean.TRUE);
        Object object10 = iLookupThunk.get(object9);
        if (iLookupThunk == object10) {
            __thunk__0__ = __site__0__.fault(object9);
            object10 = __thunk__0__.get(object9);
        }
        return object10;
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return db_io$load_db_from_basis.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

