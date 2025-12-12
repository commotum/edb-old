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
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db_io$index_includes_some_log_tail_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"db");
    public static final Keyword const__4 = RT.keyword(null, (String)"log");
    public static final Var const__8 = RT.var((String)"datomic.log", (String)"last-tree-tx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object p__17066) {
        Object object;
        Object object2 = p__17066;
        p__17066 = null;
        Object map__17067 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__17067);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__17067;
            map__17067 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__17067;
            map__17067 = null;
        }
        Object map__170672 = object;
        Object db2 = RT.get((Object)map__170672, (Object)const__3);
        Object object5 = map__170672;
        map__170672 = null;
        Object log2 = RT.get((Object)object5, (Object)const__4);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = db2;
        db2 = null;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object8 = log2;
        log2 = null;
        Object object9 = ((IFn)const__8.getRawRoot()).invoke(object8);
        Object object10 = iLookupThunk2.get(object9);
        if (iLookupThunk2 == object10) {
            __thunk__1__ = __site__1__.fault(object9);
            object10 = __thunk__1__.get(object9);
        }
        return Numbers.gt((Object)object7, (Object)object10) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db_io$index_includes_some_log_tail_QMARK_.invokeStatic(object2);
    }
}

