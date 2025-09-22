/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class callback$has_callback_signature_QMARK_$fn__10086
extends AFunction {
    Object mname;
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"java.lang.Object"));
    public static final AFn const__6 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"public"), RT.keyword(null, (String)"static")});
    public static final Var const__7 = RT.var((String)"clojure.set", (String)"intersection");
    public static final AFn const__8 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"public"), RT.keyword(null, (String)"static")});
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"name"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"parameter-types"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"flags"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public callback$has_callback_signature_QMARK_$fn__10086(Object object) {
        this.mname = object;
    }

    public Object invoke(Object member) {
        Boolean bl;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = member;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        boolean and__5236__auto__10089 = Util.equiv((Object)object2, (Object)this_.mname);
        if (and__5236__auto__10089) {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object3 = member;
            Object object4 = iLookupThunk2.get(object3);
            if (iLookupThunk2 == object4) {
                __thunk__1__ = __site__1__.fault(object3);
                object4 = __thunk__1__.get(object3);
            }
            boolean and__5236__auto__10088 = Util.equiv((Object)object4, (Object)const__3);
            if (and__5236__auto__10088) {
                IFn iFn = (IFn)const__7.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object5 = member;
                member = null;
                Object object6 = iLookupThunk3.get(object5);
                if (iLookupThunk3 == object6) {
                    __thunk__2__ = __site__2__.fault(object5);
                    object6 = __thunk__2__.get(object5);
                }
                callback$has_callback_signature_QMARK_$fn__10086 this_ = null;
                bl = Util.equiv((Object)const__6, (Object)iFn.invoke((Object)const__8, object6)) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                bl = and__5236__auto__10088 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__10089 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

