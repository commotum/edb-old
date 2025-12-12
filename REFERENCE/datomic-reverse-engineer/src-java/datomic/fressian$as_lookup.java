/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.fressian$as_lookup$reify__12164;

public final class fressian$as_lookup
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 32, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object o) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(o);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = o;
            o = null;
            object = ((IObj)new fressian$as_lookup$reify__12164(null, object3)).withMeta((IPersistentMap)const__5);
        } else {
            object = o;
            Object object4 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$as_lookup.invokeStatic(object2);
    }
}

