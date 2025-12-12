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
package datomic.core2.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb$append_request
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.aws.ddb", (String)"conditional-put-request");
    public static final Keyword const__1 = RT.keyword(null, (String)"table");
    public static final Keyword const__2 = RT.keyword(null, (String)"p");
    public static final Keyword const__3 = RT.keyword(null, (String)"r");
    public static final Keyword const__4 = RT.keyword(null, (String)"item");
    public static final Keyword const__5 = RT.keyword(null, (String)"t");
    public static final Keyword const__6 = RT.keyword(null, (String)"header");
    public static final Var const__7 = RT.var((String)"datomic.java.io", (String)"clj->str");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__9 = RT.keyword(null, (String)"body");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object table, Object p, Object header, Object body) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object[] objectArray = new Object[8];
        objectArray[0] = const__1;
        Object object = table;
        table = null;
        objectArray[1] = object;
        objectArray[2] = const__2;
        objectArray[3] = const__2;
        objectArray[4] = const__3;
        objectArray[5] = const__3;
        objectArray[6] = const__4;
        Object[] objectArray2 = new Object[8];
        objectArray2[0] = const__2;
        Object object2 = p;
        p = null;
        objectArray2[1] = object2;
        objectArray2[2] = const__3;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = header;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        objectArray2[3] = object4;
        objectArray2[4] = const__6;
        Object object5 = header;
        header = null;
        objectArray2[5] = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(object5, (Object)const__5));
        objectArray2[6] = const__9;
        Object object6 = body;
        body = null;
        objectArray2[7] = object6;
        objectArray[7] = RT.mapUniqueKeys((Object[])objectArray2);
        return iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return ddb$append_request.invokeStatic(object5, object6, object7, object8);
    }
}

