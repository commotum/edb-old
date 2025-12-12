/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.atom;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.atom.spi.DurableAtom;

public final class logged$load
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final AFn const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Object const__9;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object args) {
        v0 = args;
        args = null;
        G__20386 = ((IFn)logged$load.const__0.getRawRoot()).invoke(v0, (Object)logged$load.const__6);
        v1 = G__20386;
        if (Util.classOf((Object)v1) == logged$load.__cached_class__0) ** GOTO lbl9
        if (!(v1 instanceof DurableAtom)) {
            v1 = v1;
            logged$load.__cached_class__0 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = logged$load.const__7.getRawRoot().invoke(v1, ((IFn)logged$load.const__8.getRawRoot()).invoke(logged$load.const__9));
        } else {
            v2 = ((DurableAtom)v1)._sync(((IFn)logged$load.const__8.getRawRoot()).invoke(logged$load.const__9));
        }
        var1_1 = null;
        return G__20386;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return logged$load.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.core2.atom.logged", (String)"create*");
        const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"anom"), RT.map((Object[])new Object[]{RT.keyword((String)"cognitect.anomalies", (String)"category"), RT.keyword((String)"cognitect.anomalies", (String)"unavailable"), RT.keyword((String)"cognitect.anomalies", (String)"message"), "Initializing"})});
        const__7 = RT.var((String)"datomic.core2.atom.spi", (String)"-sync");
        const__8 = RT.var((String)"clojure.core.async", (String)"chan");
        const__9 = 1L;
    }
}

