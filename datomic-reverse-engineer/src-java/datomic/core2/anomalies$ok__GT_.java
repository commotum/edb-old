/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.core2.anomalies$ok__GT_$fn__19452;

public final class anomalies$ok__GT_
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"interleave");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"repeat");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"butlast");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"last");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object expr, ISeq forms) {
        Object object;
        Object g = ((IFn)const__0.getRawRoot()).invoke();
        ISeq iSeq = forms;
        forms = null;
        Object steps = ((IFn)const__1.getRawRoot()).invoke((Object)new anomalies$ok__GT_$fn__19452(g), (Object)iSeq);
        IFn iFn = (IFn)const__2.getRawRoot();
        IFn iFn2 = (IFn)const__3.getRawRoot();
        Object object2 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5);
        Object object3 = expr;
        expr = null;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(g), ((IFn)const__4.getRawRoot()).invoke(object3), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(g), ((IFn)const__10.getRawRoot()).invoke(steps))))));
        IFn iFn3 = (IFn)const__4.getRawRoot();
        Object object5 = ((IFn)const__11.getRawRoot()).invoke(steps);
        if (object5 != null && object5 != Boolean.FALSE) {
            object = g;
            g = null;
        } else {
            Object object6 = steps;
            steps = null;
            object = ((IFn)const__12.getRawRoot()).invoke(object6);
        }
        return iFn.invoke(iFn2.invoke(object2, object4, iFn3.invoke(object)));
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return anomalies$ok__GT_.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

