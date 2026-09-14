/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.query$group_rel$agg$reify__19455;

public final class query$group_rel$agg__19454
extends AFunction {
    Object srel;
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 639, RT.keyword(null, (String)"column"), 19});

    public query$group_rel$agg__19454(Object object) {
        this.srel = object;
    }

    public Object invoke(Object i, Object r, Object cnt) {
        Object object = cnt;
        cnt = null;
        Object object2 = i;
        i = null;
        Object object3 = r;
        r = null;
        return ((IObj)new query$group_rel$agg$reify__19455(null, object, object2, this.srel, object3)).withMeta((IPersistentMap)const__4);
    }
}

