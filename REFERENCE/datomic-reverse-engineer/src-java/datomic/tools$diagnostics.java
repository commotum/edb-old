/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.tools$diagnostics$fn__21815;
import datomic.tools$diagnostics$fn__21817;
import datomic.tools$diagnostics$fn__21819;
import datomic.tools$diagnostics$fn__21821;

public final class tools$diagnostics
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__1 = RT.keyword(null, (String)"db");
    public static final Keyword const__2 = RT.keyword(null, (String)"catalog");
    public static final Keyword const__3 = RT.keyword(null, (String)"db-storage");
    public static final Keyword const__4 = RT.keyword(null, (String)"peer");

    public static Object invokeStatic(Object uri2) {
        Object[] objectArray = new Object[]{const__1, ((IFn)new tools$diagnostics$fn__21815(uri2)).invoke()};
        Object[] objectArray2 = new Object[]{const__2, ((IFn)new tools$diagnostics$fn__21817(uri2)).invoke()};
        Object[] objectArray3 = new Object[2];
        objectArray3[0] = const__3;
        Object object = uri2;
        uri2 = null;
        objectArray3[1] = ((IFn)new tools$diagnostics$fn__21819(object)).invoke();
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), (Object)RT.mapUniqueKeys((Object[])objectArray2), (Object)RT.mapUniqueKeys((Object[])objectArray3), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__4, ((IFn)new tools$diagnostics$fn__21821()).invoke()}));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$diagnostics.invokeStatic(object2);
    }
}

