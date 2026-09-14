/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$needs_new_avet$fn__15582;
import datomic.index$needs_new_avet$fn__15584;

public final class index$needs_new_avet
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"instance?");
    public static final Object const__5 = RT.classForName((String)"datomic.db.Attribute");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"elements"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        index$needs_new_avet$fn__15582 index$needs_new_avet$fn__15582 = new index$needs_new_avet$fn__15582();
        IFn iFn3 = (IFn)const__2.getRawRoot();
        index$needs_new_avet$fn__15584 index$needs_new_avet$fn__15584 = new index$needs_new_avet$fn__15584();
        IFn iFn4 = (IFn)const__2.getRawRoot();
        Object object = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), const__5);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = db2;
        db2 = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        return iFn.invoke(iFn2.invoke((Object)index$needs_new_avet$fn__15582, iFn3.invoke((Object)index$needs_new_avet$fn__15584, iFn4.invoke(object, object3))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$needs_new_avet.invokeStatic(object2);
    }
}

