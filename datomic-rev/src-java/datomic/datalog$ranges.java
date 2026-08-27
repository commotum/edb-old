/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$ranges$fn__18863;
import datomic.datalog$ranges$fn__18866;

public final class datalog$ranges
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce-kv");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"=");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"=");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"<");
    public static final Var const__5 = RT.var((String)"datomic.extensions", (String)"<");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"<=");
    public static final Var const__7 = RT.var((String)"datomic.extensions", (String)"<=");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"range-starts"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"range-whiles"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object in_consts, Object query2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        datalog$ranges$fn__18863 datalog$ranges$fn__18863 = new datalog$ranges$fn__18863(in_consts);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = query2;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = iFn.invoke((Object)datalog$ranges$fn__18863, (Object)PersistentArrayMap.EMPTY, object2);
        IPersistentMap cmps = RT.mapUniqueKeys((Object[])new Object[]{const__2, const__3.getRawRoot(), const__4, const__5.getRawRoot(), const__6, const__7.getRawRoot()});
        IFn iFn2 = (IFn)const__0.getRawRoot();
        Object object4 = in_consts;
        in_consts = null;
        IPersistentMap iPersistentMap = cmps;
        cmps = null;
        datalog$ranges$fn__18866 datalog$ranges$fn__18866 = new datalog$ranges$fn__18866(object4, iPersistentMap);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object5 = query2;
        query2 = null;
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        return Tuple.create((Object)object3, (Object)iFn2.invoke((Object)datalog$ranges$fn__18866, (Object)PersistentArrayMap.EMPTY, object6));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$ranges.invokeStatic(object3, object4);
    }
}

