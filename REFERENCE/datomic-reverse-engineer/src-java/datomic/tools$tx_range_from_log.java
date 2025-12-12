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
import datomic.tools$tx_range_from_log$fn__21774;
import java.util.Arrays;

public final class tools$tx_range_from_log
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.tools", (String)"cr?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"cr?"), Symbol.intern(null, (String)"cr")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 10}));
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__5 = RT.var((String)"datomic.log", (String)"seek-tx");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"find-log");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"take-while");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cluster"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object cr, Object start, Object end) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(cr);
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        IFn iFn = (IFn)const__4.getRawRoot();
        IFn iFn2 = (IFn)const__5.getRawRoot();
        IFn iFn3 = (IFn)const__6.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = cr;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object5 = cr;
        cr = null;
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        Object object7 = start;
        start = null;
        Object s = iFn.invoke(iFn2.invoke(iFn3.invoke(object4, object6), object7));
        Object object8 = end;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = end;
            end = null;
            Object object10 = s;
            s = null;
            object = ((IFn)const__9.getRawRoot()).invoke((Object)new tools$tx_range_from_log$fn__21774(object9), object10);
        } else {
            object = s;
            Object var3_3 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return tools$tx_range_from_log.invokeStatic(object4, object5, object6);
    }
}

