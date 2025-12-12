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
import datomic.peer$seq_iterator$reify__21670;

public final class peer$seq_iterator
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 708, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object aseq) {
        Object a;
        Object object = aseq;
        aseq = null;
        Object object2 = a = ((IFn)const__0.getRawRoot()).invoke(object);
        a = null;
        return ((IObj)new peer$seq_iterator$reify__21670(null, object2)).withMeta((IPersistentMap)const__5);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$seq_iterator.invokeStatic(object2);
    }
}

