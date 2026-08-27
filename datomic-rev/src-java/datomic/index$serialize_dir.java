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
import clojure.lang.Var;

public final class index$serialize_dir
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"key");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"fress");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"transpose");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Keyword const__7 = RT.keyword(null, (String)"segid");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Keyword const__9 = RT.keyword(null, (String)"offset");
    public static final Keyword const__10 = RT.keyword(null, (String)"count");
    public static final Var const__11 = RT.var((String)"datomic.index", (String)"dir-node");
    public static final Var const__12 = RT.var((String)"datomic.index", (String)"common-write-handlers");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object des) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = ((IFn)const__1.getRawRoot()).invoke(des);
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object keydata = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__0, des)));
        Object segids = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__7, des));
        Object offsets = ((IFn)const__8.getRawRoot()).invoke(Integer.TYPE, ((IFn)const__5.getRawRoot()).invoke((Object)const__9, des));
        Object object3 = des;
        des = null;
        Object counts = ((IFn)const__8.getRawRoot()).invoke(Integer.TYPE, ((IFn)const__5.getRawRoot()).invoke((Object)const__10, object3));
        Object object4 = keydata;
        keydata = null;
        Object object5 = segids;
        segids = null;
        Object object6 = offsets;
        offsets = null;
        Object object7 = counts;
        counts = null;
        return Tuple.create((Object)object2, (Object)((IFn)const__2.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object4, object5, object6, object7), const__12.getRawRoot()));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$serialize_dir.invokeStatic(object2);
    }
}

