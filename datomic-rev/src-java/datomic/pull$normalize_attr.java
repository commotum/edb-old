/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class pull$normalize_attr
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"attr-with-opts?");
    public static final Var const__1 = RT.var((String)"datomic.pull", (String)"attr-with-opts->attr-tuple");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__4 = RT.var((String)"datomic.pull", (String)"attr-spec->attr");
    public static final Var const__5 = RT.var((String)"datomic.pull", (String)"attr-spec->fn");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"identity");

    public static Object invokeStatic(Object attr_spec) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(attr_spec);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = attr_spec;
            attr_spec = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3);
        } else {
            IPersistentVector iPersistentVector = Tuple.create((Object)((IFn)const__4.getRawRoot()).invoke(attr_spec));
            Object object4 = attr_spec;
            attr_spec = null;
            object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)iPersistentVector, ((IFn)const__5.getRawRoot()).invoke(object4)), const__6.getRawRoot());
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$normalize_attr.invokeStatic(object2);
    }
}

