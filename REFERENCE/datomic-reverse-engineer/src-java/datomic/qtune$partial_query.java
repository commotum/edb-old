/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.qtune$partial_query$fn__23429;
import datomic.qtune$partial_query$fxp_QMARK___23427;

public final class qtune$partial_query
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__2 = RT.var((String)"datomic.qtune", (String)"cbinds");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__4 = RT.var((String)"datomic.qtune", (String)"cvars");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"set");
    public static final Keyword const__7 = RT.keyword(null, (String)"find");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__9 = RT.var((String)"clojure.set", (String)"intersection");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"filter");
    public static final Keyword const__11 = RT.keyword(null, (String)"in");
    public static final Keyword const__12 = RT.keyword(null, (String)"where");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"concat");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"in"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object qmap, Object preds, Object clauses, Object clause, Object rclauses, Object allow_cross) {
        IPersistentMap iPersistentMap;
        Object object;
        qtune$partial_query$fxp_QMARK___23427 fxp_QMARK_;
        Object bindings = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), clauses));
        qtune$partial_query$fxp_QMARK___23427 qtune$partial_query$fxp_QMARK___23427 = fxp_QMARK_ = new qtune$partial_query$fxp_QMARK___23427();
        fxp_QMARK_ = null;
        Object object2 = ((IFn)qtune$partial_query$fxp_QMARK___23427).invoke(clause);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = ((IFn)const__3.getRawRoot()).invoke(bindings, ((IFn)const__4.getRawRoot()).invoke(clause));
        } else {
            Object or__5238__auto__23432;
            Object object3 = allow_cross;
            allow_cross = null;
            Object object4 = or__5238__auto__23432 = object3;
            if (object4 != null && object4 != Boolean.FALSE) {
                object = or__5238__auto__23432;
                or__5238__auto__23432 = null;
            } else {
                object = ((IFn)const__5.getRawRoot()).invoke(bindings, ((IFn)const__4.getRawRoot()).invoke(clause));
            }
        }
        if (object != null && object != Boolean.FALSE) {
            Object remvars;
            Object object5 = bindings;
            bindings = null;
            Object bindings2 = ((IFn)const__0.getRawRoot()).invoke(object5, ((IFn)const__2.getRawRoot()).invoke(clause));
            IFn iFn = (IFn)const__0.getRawRoot();
            IFn iFn2 = (IFn)const__6.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = qmap;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            Object object8 = rclauses;
            rclauses = null;
            Object object9 = remvars = iFn.invoke(iFn2.invoke(object7), ((IFn)const__1.getRawRoot()).invoke(const__4.getRawRoot(), object8));
            remvars = null;
            Object next_find = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(bindings2, object9));
            Object object10 = bindings2;
            bindings2 = null;
            Object object11 = preds;
            preds = null;
            Object next_preds = ((IFn)const__10.getRawRoot()).invoke((Object)new qtune$partial_query$fn__23429(object10), object11);
            Object[] objectArray = new Object[6];
            objectArray[0] = const__7;
            Object object12 = next_find;
            next_find = null;
            objectArray[1] = object12;
            objectArray[2] = const__11;
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object13 = qmap;
            qmap = null;
            Object object14 = iLookupThunk2.get(object13);
            if (iLookupThunk2 == object14) {
                __thunk__1__ = __site__1__.fault(object13);
                object14 = __thunk__1__.get(object13);
            }
            objectArray[3] = object14;
            objectArray[4] = const__12;
            Object object15 = clauses;
            clauses = null;
            Object object16 = clause;
            clause = null;
            Object object17 = next_preds;
            next_preds = null;
            objectArray[5] = ((IFn)const__13.getRawRoot()).invoke(object15, (Object)Tuple.create((Object)object16), object17);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return qtune$partial_query.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}

