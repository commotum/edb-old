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
 *  org.h2.tools.Server
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import org.h2.tools.Server;

public final class h2$shutdown
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"server");

    public static Object invokeStatic(Object p__11644) {
        Object server;
        Object map__11645;
        Object object;
        Object object2 = p__11644;
        p__11644 = null;
        Object map__116452 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__116452);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__116452;
            map__116452 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__116452;
            map__116452 = null;
        }
        Object object5 = map__11645 = object;
        map__11645 = null;
        Object object6 = server = RT.get((Object)object5, (Object)const__3);
        server = null;
        ((Server)object6).stop();
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$shutdown.invokeStatic(object2);
    }
}

