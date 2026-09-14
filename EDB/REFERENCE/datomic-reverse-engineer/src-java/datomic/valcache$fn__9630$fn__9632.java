/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class valcache$fn__9630$fn__9632
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"opcode");

    public Object invoke(Object p__9631, Object sc) {
        Object opcode;
        Object object;
        Object object2 = p__9631;
        p__9631 = null;
        Object map__9633 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__9633);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__9633;
            map__9633 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__9633;
            map__9633 = null;
        }
        Object map__96332 = object;
        Object object5 = map__96332;
        map__96332 = null;
        Object object6 = opcode = RT.get((Object)object5, (Object)const__3);
        opcode = null;
        return object6;
    }
}

