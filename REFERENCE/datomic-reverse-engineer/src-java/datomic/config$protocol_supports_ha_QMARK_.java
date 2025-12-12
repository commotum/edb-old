/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class config$protocol_supports_ha_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"contains?");
    public static final AFn const__4 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"limited-edition"), RT.keyword(null, (String)"dev")});

    public static Object invokeStatic(Object protocol) {
        Object object = protocol;
        protocol = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__4, object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$protocol_supports_ha_QMARK_.invokeStatic(object2);
    }
}

