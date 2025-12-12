/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class integrity$cauterize
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Keyword const__2 = RT.keyword(null, (String)"index");
    public static final Keyword const__3 = RT.keyword(null, (String)"history");
    public static final Keyword const__4 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__5 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__6 = RT.keyword(null, (String)"memidx");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"mem-index-set");

    public static Object invokeStatic(Object db2, ISeq ks) {
        Object object = db2;
        db2 = null;
        ISeq iSeq = ks;
        ks = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__2, null, const__3, null, const__4, null, const__5, null, const__6, const__7.getRawRoot()}), (Object)iSeq));
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return integrity$cauterize.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

