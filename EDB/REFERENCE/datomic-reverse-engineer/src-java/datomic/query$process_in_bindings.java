/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$process_in_bindings$binding_QMARK___19303;
import datomic.query$process_in_bindings$fn__19307;

public final class query$process_in_bindings
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"in");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map-indexed");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"vector");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"in"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object qmap, Object prefix) {
        Object object;
        Object temp__5455__auto__19318;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = qmap;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = temp__5455__auto__19318 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5455__auto__19318;
            temp__5455__auto__19318 = null;
            Object ins = object5;
            query$process_in_bindings$binding_QMARK___19303 binding_QMARK_ = new query$process_in_bindings$binding_QMARK___19303();
            Object object6 = prefix;
            prefix = null;
            query$process_in_bindings$binding_QMARK___19303 query$process_in_bindings$binding_QMARK___19303 = binding_QMARK_;
            binding_QMARK_ = null;
            Object object7 = qmap;
            qmap = null;
            Object object8 = ins;
            ins = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)new query$process_in_bindings$fn__19307(object6, (Object)query$process_in_bindings$binding_QMARK___19303), ((IFn)const__2.getRawRoot()).invoke(object7, (Object)const__0, (Object)PersistentVector.EMPTY), ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), object8));
        } else {
            object = qmap;
            Object object9 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$process_in_bindings.invokeStatic(object3, object4);
    }
}

