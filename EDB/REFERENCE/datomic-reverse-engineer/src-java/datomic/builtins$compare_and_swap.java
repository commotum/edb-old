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
 *  clojure.lang.Util
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
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class builtins$compare_and_swap
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"invalid-cas");
    public static final Keyword const__4 = RT.keyword((String)"datomic", (String)"cancelled");
    public static final Keyword const__5 = RT.keyword(null, (String)"e");
    public static final Keyword const__6 = RT.keyword(null, (String)"a");
    public static final Keyword const__7 = RT.keyword(null, (String)"v-old");
    public static final Keyword const__8 = RT.keyword(null, (String)"v-new");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Keyword const__13 = RT.keyword((String)"db.error", (String)"invalid-cas-many");
    public static final Keyword const__14 = RT.keyword(null, (String)"v");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__17 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__18 = RT.keyword((String)"db", (String)"add");
    public static final Var const__19 = RT.var((String)"datomic.error", (String)"state");
    public static final Keyword const__20 = RT.keyword((String)"db.error", (String)"cas-failed");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object e, Object a, Object v_old, Object v_new) {
        Object object;
        Object v_cur;
        Object object2;
        Object and__5236__auto__23414;
        Object object3 = and__5236__auto__23414 = e;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object and__5236__auto__23413;
            Object object4 = and__5236__auto__23413 = a;
            if (object4 != null && object4 != Boolean.FALSE) {
                object2 = ((IFn)const__0.getRawRoot()).invoke((Object)(Util.identical((Object)v_new, null) ? Boolean.TRUE : Boolean.FALSE));
            } else {
                object2 = and__5236__auto__23413;
                and__5236__auto__23413 = null;
            }
        } else {
            object2 = and__5236__auto__23414;
            and__5236__auto__23414 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
        } else {
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)"entity, attribute, and new-value must be specified", (Object)RT.mapUniqueKeys((Object[])new Object[]{const__4, Boolean.TRUE, const__5, e, const__6, a, const__7, v_old, const__8, v_new}));
        }
        if (Util.equiv((long)36L, (Object)((Attribute)((IFn)builtins$compare_and_swap.const__11.getRawRoot()).invoke((Object)db2, (Object)((IFn)builtins$compare_and_swap.const__12.getRawRoot()).invoke((Object)db2, (Object)a))).cardinality)) {
            ((IFn)const__2.getRawRoot()).invoke((Object)const__13, (Object)"attribute must be cardinality-one", (Object)RT.mapUniqueKeys((Object[])new Object[]{const__4, Boolean.TRUE, const__5, e, const__6, a, const__7, v_old, const__8, v_new}));
        }
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object5 = db2;
        db2 = null;
        Object object6 = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(object5, (Object)const__17, (Object)Tuple.create((Object)e, (Object)a)));
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = v_cur = __thunk__0__.get(object6);
        }
        if (Util.equiv((Object)v_cur, (Object)v_old)) {
            Object object8 = e;
            e = null;
            Object object9 = a;
            a = null;
            Object object10 = v_new;
            v_new = null;
            object = Tuple.create((Object)Tuple.create((Object)const__18, (Object)object8, (Object)object9, (Object)object10));
        } else {
            Object object11 = ((IFn)const__21.getRawRoot()).invoke((Object)"Compare failed: ", v_old, (Object)" ", v_cur);
            Object[] objectArray = new Object[10];
            objectArray[0] = const__4;
            objectArray[1] = Boolean.TRUE;
            objectArray[2] = const__5;
            Object object12 = e;
            e = null;
            objectArray[3] = object12;
            objectArray[4] = const__6;
            Object object13 = a;
            a = null;
            objectArray[5] = object13;
            objectArray[6] = const__7;
            Object object14 = v_old;
            v_old = null;
            objectArray[7] = object14;
            objectArray[8] = const__14;
            Object object15 = v_cur;
            v_cur = null;
            objectArray[9] = object15;
            object = ((IFn)const__19.getRawRoot()).invoke((Object)const__20, object11, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return builtins$compare_and_swap.invokeStatic(object6, object7, object8, object9, object10);
    }
}

