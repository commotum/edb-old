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
import clojure.lang.Var;

public final class stats$datom_counts
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"total");
    public static final Keyword const__4 = RT.keyword(null, (String)"index-datoms");
    public static final Keyword const__5 = RT.keyword(null, (String)"mid-index-datoms");
    public static final Keyword const__6 = RT.keyword(null, (String)"index+mid-datoms");
    public static final Keyword const__8 = RT.keyword(null, (String)"history-datoms");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"index"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object summary_fn, Object db2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)summary_fn;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = db2;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object index2 = iFn.invoke(iFn2.invoke(db2, object2));
        IFn iFn3 = (IFn)const__0.getRawRoot();
        IFn iFn4 = (IFn)summary_fn;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = db2;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        Object mid = iFn3.invoke(iFn4.invoke(db2, object4));
        IFn iFn5 = (IFn)const__0.getRawRoot();
        Object object5 = summary_fn;
        summary_fn = null;
        IFn iFn6 = (IFn)object5;
        Object object6 = db2;
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object7 = db2;
        db2 = null;
        Object object8 = iLookupThunk3.get(object7);
        if (iLookupThunk3 == object8) {
            __thunk__2__ = __site__2__.fault(object7);
            object8 = __thunk__2__.get(object7);
        }
        Object hist = iFn5.invoke(iFn6.invoke(object6, object8));
        Object[] objectArray = new Object[8];
        objectArray[0] = const__4;
        objectArray[1] = index2;
        objectArray[2] = const__5;
        objectArray[3] = mid;
        objectArray[4] = const__6;
        Object object9 = index2;
        index2 = null;
        Object object10 = mid;
        mid = null;
        objectArray[5] = Numbers.add((Object)object9, (Object)object10);
        objectArray[6] = const__8;
        Object object11 = hist;
        hist = null;
        objectArray[7] = object11;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return stats$datom_counts.invokeStatic(object3, object4);
    }
}

