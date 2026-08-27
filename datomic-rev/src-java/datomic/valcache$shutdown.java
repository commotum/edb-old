/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class valcache$shutdown
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.valcache", (String)"valcache-ref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"compare-and-set!");

    public static Object invokeStatic() {
        Object object;
        Object temp__5457__auto__9781;
        Object object2 = temp__5457__auto__9781 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        if (object2 != null && object2 != Boolean.FALSE) {
            Object valcache2;
            Object object3 = temp__5457__auto__9781;
            temp__5457__auto__9781 = null;
            Object object4 = valcache2 = object3;
            valcache2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(const__1.getRawRoot(), object4, null);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke() {
        return valcache$shutdown.invokeStatic();
    }
}

