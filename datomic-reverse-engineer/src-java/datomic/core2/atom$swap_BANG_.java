/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.atom$swap_BANG_$fn__19671;
import datomic.core2.atom$swap_BANG_$fn__19674;
import datomic.core2.atom.spi.DurableAtom;

public final class atom$swap_BANG_
extends RestFn {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object a, Object f, ISeq args) {
        Object object;
        Object ch = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new atom$swap_BANG_$fn__19671()));
        Object object2 = a;
        a = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof DurableAtom) {
                ISeq iSeq = args;
                args = null;
                Object object4 = f;
                f = null;
                Object object5 = ch;
                ch = null;
                object = ((DurableAtom)object3)._swap_vals_BANG_((Object)new atom$swap_BANG_$fn__19674(iSeq, object4), object5);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        ISeq iSeq = args;
        args = null;
        Object object6 = f;
        f = null;
        Object object7 = ch;
        ch = null;
        object = const__2.getRawRoot().invoke(object3, (Object)new atom$swap_BANG_$fn__19674(iSeq, object6), object7);
        return object;
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return atom$swap_BANG_.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }

    static {
        const__0 = RT.var((String)"clojure.core.async", (String)"promise-chan");
        const__1 = RT.var((String)"clojure.core", (String)"map");
        const__2 = RT.var((String)"datomic.core2.atom.spi", (String)"-swap-vals!");
    }
}

