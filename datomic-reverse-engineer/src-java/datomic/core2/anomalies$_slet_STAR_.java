/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class anomalies$_slet_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__9 = (AFn)Symbol.intern((String)"datomic.core2.anomalies", (String)"anom");
    public static final Var const__10 = RT.var((String)"datomic.core2.anomalies", (String)"-slet*");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"do");

    public static Object invokeStatic(Object bindings, Object body) {
        Object object;
        Object vec__19461;
        Object object2 = bindings;
        bindings = null;
        Object object3 = vec__19461 = object2;
        vec__19461 = null;
        Object seq__19462 = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object first__19463 = ((IFn)const__1.getRawRoot()).invoke(seq__19462);
        Object object4 = seq__19462;
        seq__19462 = null;
        Object seq__194622 = ((IFn)const__2.getRawRoot()).invoke(object4);
        Object object5 = first__19463;
        first__19463 = null;
        Object s = object5;
        Object first__194632 = ((IFn)const__1.getRawRoot()).invoke(seq__194622);
        Object object6 = seq__194622;
        seq__194622 = null;
        Object seq__194623 = ((IFn)const__2.getRawRoot()).invoke(object6);
        Object object7 = first__194632;
        first__194632 = null;
        Object v = object7;
        Object object8 = seq__194623;
        seq__194623 = null;
        Object more = object8;
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__3.getRawRoot();
        Object object9 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5);
        Object object10 = v;
        v = null;
        Object object11 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(s), ((IFn)const__4.getRawRoot()).invoke(object10)))));
        IFn iFn3 = (IFn)const__4.getRawRoot();
        IFn iFn4 = (IFn)const__0.getRawRoot();
        IFn iFn5 = (IFn)const__3.getRawRoot();
        Object object12 = ((IFn)const__4.getRawRoot()).invoke((Object)const__8);
        Object object13 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__9), ((IFn)const__4.getRawRoot()).invoke(s))));
        Object object14 = s;
        s = null;
        Object object15 = ((IFn)const__4.getRawRoot()).invoke(object14);
        IFn iFn6 = (IFn)const__4.getRawRoot();
        Object object16 = ((IFn)const__0.getRawRoot()).invoke(more);
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = more;
            more = null;
            Object object18 = body;
            body = null;
            object = ((IFn)const__10.getRawRoot()).invoke(object17, object18);
        } else {
            Object object19 = body;
            body = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__11), object19));
        }
        return iFn.invoke(iFn2.invoke(object9, object11, iFn3.invoke(iFn4.invoke(iFn5.invoke(object12, object13, object15, iFn6.invoke(object))))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return anomalies$_slet_STAR_.invokeStatic(object3, object4);
    }
}

