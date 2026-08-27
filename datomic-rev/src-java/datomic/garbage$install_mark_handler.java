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
import datomic.garbage$install_mark_handler$fn__19819;
import datomic.garbage$install_mark_handler$fn__19822;

public final class garbage$install_mark_handler
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"create-computing");
    public static final Object const__1 = 10L;
    public static final Var const__2 = RT.var((String)"datomic.process.events", (String)"subscribe");
    public static final Keyword const__3 = RT.keyword((String)"datomic.garbage", (String)"mark");

    public static Object invokeStatic() {
        Object olookups;
        Object object = olookups = ((IFn)const__0.getRawRoot()).invoke((Object)new garbage$install_mark_handler$fn__19819(), const__1);
        olookups = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__3, (Object)new garbage$install_mark_handler$fn__19822(object));
    }

    public Object invoke() {
        return garbage$install_mark_handler.invokeStatic();
    }
}

