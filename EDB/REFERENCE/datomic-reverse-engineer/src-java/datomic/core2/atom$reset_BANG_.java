/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.atom$reset_BANG_$fn__19663;
import datomic.core2.atom.spi.DurableAtom;

public final class atom$reset_BANG_
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object a, Object v) {
        Object object;
        Object ch = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new atom$reset_BANG_$fn__19663()));
        Object object2 = a;
        a = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof DurableAtom) {
                Object object4 = v;
                v = null;
                Object object5 = ch;
                ch = null;
                object = ((DurableAtom)object3)._swap_vals_BANG_(((IFn)const__3.getRawRoot()).invoke(object4), object5);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object6 = v;
        v = null;
        Object object7 = ch;
        ch = null;
        object = const__2.getRawRoot().invoke(object3, ((IFn)const__3.getRawRoot()).invoke(object6), object7);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return atom$reset_BANG_.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.core.async", (String)"promise-chan");
        const__1 = RT.var((String)"clojure.core", (String)"map");
        const__2 = RT.var((String)"datomic.core2.atom.spi", (String)"-swap-vals!");
        const__3 = RT.var((String)"clojure.core", (String)"constantly");
    }
}

