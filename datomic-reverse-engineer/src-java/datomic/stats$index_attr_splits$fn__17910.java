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
 *  clojure.lang.Tuple
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
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class stats$index_attr_splits$fn__17910
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"key");
    public static final Keyword const__4 = RT.keyword(null, (String)"count");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p__17909) {
        Object object;
        Object object2 = p__17909;
        p__17909 = null;
        Object map__17911 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__17911);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__17911;
            map__17911 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__17911;
            map__17911 = null;
        }
        Object map__179112 = object;
        Object key = RT.get((Object)map__179112, (Object)const__3);
        Object object5 = map__179112;
        map__179112 = null;
        Object count2 = RT.get((Object)object5, (Object)const__4);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = key;
        key = null;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        Object object8 = count2;
        count2 = null;
        return Tuple.create((Object)object7, (Object)object8);
    }
}

