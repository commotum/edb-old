/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.datalog$eval_not_join$fn__18779;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;

public final class datalog$eval_not_join
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"used-srcs");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"meta");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"$");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"list");
    public static final Keyword const__16 = RT.keyword(null, (String)"find");
    public static final Keyword const__17 = RT.keyword(null, (String)"in");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"keys");
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"%");
    public static final Keyword const__21 = RT.keyword(null, (String)"where");
    public static final Var const__22 = RT.var((String)"datomic.datalog", (String)"q");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"vals");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tag"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object srcs, Object prog, Object inrel, Object inbinds, Object p__18774) {
        Object uvs;
        Object object;
        Object used;
        Object object2 = p__18774;
        p__18774 = null;
        Object vec__18775 = object2;
        Object seq__18776 = ((IFn)const__0.getRawRoot()).invoke(vec__18775);
        Object first__18777 = ((IFn)const__1.getRawRoot()).invoke(seq__18776);
        Object object3 = seq__18776;
        seq__18776 = null;
        Object seq__187762 = ((IFn)const__2.getRawRoot()).invoke(object3);
        first__18777 = null;
        Object first__187772 = ((IFn)const__1.getRawRoot()).invoke(seq__187762);
        Object object4 = seq__187762;
        seq__187762 = null;
        Object seq__187763 = ((IFn)const__2.getRawRoot()).invoke(object4);
        Object object5 = first__187772;
        first__187772 = null;
        Object vars = object5;
        Object object6 = seq__187763;
        seq__187763 = null;
        Object cs = object6;
        Object object7 = vec__18775;
        vec__18775 = null;
        Object not_join_clause = object7;
        Object object8 = used = ((IFn)const__3.getRawRoot()).invoke(srcs, not_join_clause);
        used = null;
        Object dbs = ((IFn)const__4.getRawRoot()).invoke(srcs, object8);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object9 = not_join_clause;
        not_join_clause = null;
        Object object10 = ((IFn)const__6.getRawRoot()).invoke(object9);
        Object object11 = iLookupThunk.get(object10);
        if (iLookupThunk == object11) {
            __thunk__0__ = __site__0__.fault(object10);
            object11 = __thunk__0__.get(object10);
        }
        Object csrc = object11;
        Object object12 = dbs;
        dbs = null;
        Object G__18778 = object12;
        Object object13 = csrc;
        if (object13 != null && object13 != Boolean.FALSE) {
            Object object14 = G__18778;
            G__18778 = null;
            Object object15 = srcs;
            srcs = null;
            Object object16 = csrc;
            csrc = null;
            object = ((IFn)const__7.getRawRoot()).invoke(object14, (Object)const__8, RT.get((Object)object15, (Object)object16));
        } else {
            object = G__18778;
            G__18778 = null;
        }
        Object dbs2 = object;
        Object object17 = vars;
        vars = null;
        Object object18 = uvs = ((IFn)const__10.getRawRoot()).invoke(object17);
        uvs = null;
        Object object19 = inbinds;
        inbinds = null;
        Object binds = ((IFn)const__11.getRawRoot()).invoke((Object)new datalog$eval_not_join$fn__18779(object18), object19);
        Object object20 = ((IFn)const__15.getRawRoot()).invoke(binds);
        Object object21 = binds;
        binds = null;
        Object object22 = cs;
        cs = null;
        Object query2 = ((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke((Object)const__16), object20, ((IFn)const__15.getRawRoot()).invoke((Object)const__17), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__18.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke(dbs2), ((IFn)const__15.getRawRoot()).invoke((Object)const__20), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__18.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__18.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object21)))))))))))), ((IFn)const__15.getRawRoot()).invoke((Object)const__21), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__18.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object22)))))));
        HashSet G__18782 = new HashSet((Collection)inrel);
        Object object23 = query2;
        query2 = null;
        Object object24 = dbs2;
        dbs2 = null;
        Object object25 = prog;
        prog = null;
        Object object26 = inrel;
        inrel = null;
        Boolean bl = ((AbstractSet)G__18782).removeAll((Collection)((IFn)const__22.getRawRoot()).invoke(object23, ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke(((IFn)const__25.getRawRoot()).invoke(object24)), object25, object26))) ? Boolean.TRUE : Boolean.FALSE;
        HashSet hashSet = G__18782;
        G__18782 = null;
        return hashSet;
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
        return datalog$eval_not_join.invokeStatic(object6, object7, object8, object9, object10);
    }
}

