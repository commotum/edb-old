/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class stats$index_attr_stats$fn__17876
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"key");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p__17875) {
        Object map__17877;
        Object object;
        Object object2 = p__17875;
        p__17875 = null;
        Object map__178772 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__178772);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__178772;
            map__178772 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__178772;
            map__178772 = null;
        }
        Object object5 = map__17877 = object;
        map__17877 = null;
        Object key = RT.get((Object)object5, (Object)const__3);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = key;
        key = null;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        return object7;
    }
}

