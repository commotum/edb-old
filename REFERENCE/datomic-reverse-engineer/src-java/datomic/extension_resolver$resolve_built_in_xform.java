/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class extension_resolver$resolve_built_in_xform
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final AFn const__1 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"namespace"), Symbol.intern((String)"clojure.edn", (String)"read-string"), Symbol.intern(null, (String)"symbol"), Symbol.intern(null, (String)"name"), Symbol.intern(null, (String)"keyword"), Symbol.intern(null, (String)"str")});
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"resolve");

    public static Object invokeStatic(Object sym) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)const__1, sym);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = sym;
            sym = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object3);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$resolve_built_in_xform.invokeStatic(object2);
    }
}

