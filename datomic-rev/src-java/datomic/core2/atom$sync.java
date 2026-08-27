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
import datomic.core2.atom.spi.DurableAtom;

public final class atom$sync
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object a) {
        Object object;
        Object ch = ((IFn)const__0.getRawRoot()).invoke();
        Object object2 = a;
        a = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof DurableAtom) {
                Object object4 = ch;
                ch = null;
                object = ((DurableAtom)object3)._sync(object4);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object5 = ch;
        ch = null;
        object = const__1.getRawRoot().invoke(object3, object5);
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return atom$sync.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core.async", (String)"promise-chan");
        const__1 = RT.var((String)"datomic.core2.atom.spi", (String)"-sync");
    }
}

