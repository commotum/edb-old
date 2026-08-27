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
import datomic.memory_size$handle_primitive_arrays$fn__400;

public final class memory_size$handle_primitive_arrays
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"do");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__6 = 2L;

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, ISeq typesizes) {
        ISeq iSeq = typesizes;
        typesizes = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), ((IFn)const__4.getRawRoot()).invoke((Object)new memory_size$handle_primitive_arrays$fn__400(), ((IFn)const__5.getRawRoot()).invoke(const__6, (Object)iSeq))));
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return memory_size$handle_primitive_arrays.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

