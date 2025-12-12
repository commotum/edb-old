/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Writer
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.fressian.Writer;

public final class fressian$write_named
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object tag, Object w, Object s) {
        Object object = tag;
        tag = null;
        ((Writer)w).writeTag(object, RT.intCast((long)2L));
        ((Writer)w).writeObject(((IFn)const__1.getRawRoot()).invoke(s), Boolean.TRUE.booleanValue());
        Object object2 = w;
        w = null;
        Object object3 = s;
        s = null;
        return ((Writer)object2).writeObject(((IFn)const__2.getRawRoot()).invoke(object3), Boolean.TRUE.booleanValue());
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fressian$write_named.invokeStatic(object4, object5, object6);
    }
}

