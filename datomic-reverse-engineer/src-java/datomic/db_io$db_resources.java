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

public final class db_io$db_resources
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db-io", (String)"index-db");
    public static final Var const__1 = RT.var((String)"datomic.db-io", (String)"log");
    public static final Keyword const__2 = RT.keyword(null, (String)"db");
    public static final Var const__3 = RT.var((String)"datomic.log", (String)"catchup");
    public static final Keyword const__4 = RT.keyword(null, (String)"log");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cr) {
        Object db2 = ((IFn)const__0.getRawRoot()).invoke(cr);
        Object object = cr;
        cr = null;
        Object log2 = ((IFn)const__1.getRawRoot()).invoke(object);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = db2;
        db2 = null;
        Object object3 = ((IFn)const__3.getRawRoot()).invoke(object2, log2);
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        Object db3 = object4;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__2;
        Object object5 = db3;
        db3 = null;
        objectArray[1] = object5;
        objectArray[2] = const__4;
        Object object6 = log2;
        log2 = null;
        objectArray[3] = object6;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db_io$db_resources.invokeStatic(object2);
    }
}

