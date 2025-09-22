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
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.datafy$define_object_to_functions_for$fn__17320;
import datomic.datafy$define_object_to_functions_for$fn__17329;

public final class datafy$define_object_to_functions_for
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"flush");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"do");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object clsname, ISeq specs) {
        Object cls;
        Object object = clsname;
        clsname = null;
        Object object2 = cls = ((IFn)const__0.getRawRoot()).invoke(object);
        cls = null;
        ISeq iSeq = specs;
        specs = null;
        Object arglist = ((IFn)const__1.getRawRoot()).invoke((Object)new datafy$define_object_to_functions_for$fn__17320(object2), (Object)iSeq);
        ((IFn)const__2.getRawRoot()).invoke();
        Object object3 = arglist;
        arglist = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__6), ((IFn)const__1.getRawRoot()).invoke((Object)new datafy$define_object_to_functions_for$fn__17329(), object3)));
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
        return datafy$define_object_to_functions_for.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

