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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Db;

public final class db$prepare_for_indexing
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__2 = RT.keyword(null, (String)"memidx");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"mem-index-set");
    public static final Keyword const__4 = RT.keyword(null, (String)"indexingNextT");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"nextT"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        Object object;
        Object object2 = ((Db)db2).indexing;
        if (object2 != null && object2 != Boolean.FALSE) {
            object = db2;
            db2 = null;
        } else {
            IFn iFn = (IFn)const__0.getRawRoot();
            Object object3 = db2;
            Object object4 = ((Db)db2).memidx;
            Object object5 = const__3.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = db2;
            db2 = null;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            object = iFn.invoke(object3, (Object)const__1, object4, (Object)const__2, object5, (Object)const__4, object7);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$prepare_for_indexing.invokeStatic(object2);
    }
}

