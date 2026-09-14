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
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.iter$take_while$reify__11772;
import datomic.iter.Iter;

public final class iter$take_while
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 152, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object p, Object iter2) {
        IObj iObj;
        Object object;
        Object and__5236__auto__11776;
        Object object2 = and__5236__auto__11776 = iter2;
        if (object2 != null && object2 != Boolean.FALSE) {
            object = ((IFn)p).invoke(((Iter)iter2).get());
        } else {
            object = and__5236__auto__11776;
            Object var2_2 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            Object object3 = p;
            p = null;
            Object object4 = iter2;
            iter2 = null;
            iObj = ((IObj)new iter$take_while$reify__11772(null, object3, object4)).withMeta((IPersistentMap)const__4);
        } else {
            iObj = null;
        }
        return iObj;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return iter$take_while.invokeStatic(object3, object4);
    }
}

