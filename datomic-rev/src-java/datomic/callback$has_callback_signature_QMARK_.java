/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.callback$has_callback_signature_QMARK_$fn__10086;
import java.util.Arrays;

public final class callback$has_callback_signature_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"symbol?"), Symbol.intern(null, (String)"mname")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 10}));
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__8 = RT.var((String)"clojure.reflect", (String)"reflect");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"members"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cls, Object mname) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(mname);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        IFn iFn = (IFn)const__5.getRawRoot();
        IFn iFn2 = (IFn)const__6.getRawRoot();
        Object object2 = mname;
        mname = null;
        callback$has_callback_signature_QMARK_$fn__10086 callback$has_callback_signature_QMARK_$fn__10086 = new callback$has_callback_signature_QMARK_$fn__10086(object2);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = cls;
        cls = null;
        Object object4 = ((IFn)const__8.getRawRoot()).invoke(object3);
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        return RT.booleanCast((Object)iFn.invoke(iFn2.invoke((Object)callback$has_callback_signature_QMARK_$fn__10086, object5))) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return callback$has_callback_signature_QMARK_.invokeStatic(object3, object4);
    }
}

