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
import datomic.iter$filter$advance__11765;
import datomic.iter$filter$reify__11767;

public final class iter$filter
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 142, RT.keyword(null, (String)"column"), 7});

    public static Object invokeStatic(Object p, Object iter2) {
        IObj iObj;
        Object temp__5457__auto__11771;
        iter$filter$advance__11765 advance = new iter$filter$advance__11765(p);
        Object object = iter2;
        iter2 = null;
        Object object2 = temp__5457__auto__11771 = ((IFn)advance).invoke(object);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5457__auto__11771;
            temp__5457__auto__11771 = null;
            Object iter3 = object3;
            Object object4 = p;
            p = null;
            Object object5 = iter3;
            iter3 = null;
            iter$filter$advance__11765 iter$filter$advance__11765 = advance;
            advance = null;
            iObj = ((IObj)new iter$filter$reify__11767(null, object4, object5, (Object)iter$filter$advance__11765)).withMeta((IPersistentMap)const__4);
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
        return iter$filter.invokeStatic(object3, object4);
    }
}

