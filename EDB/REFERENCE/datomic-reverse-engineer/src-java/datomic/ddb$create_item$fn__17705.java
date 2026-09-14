/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb$create_item$fn__17705
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__4 = RT.var((String)"datomic.ddb", (String)"fullname");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"number?");
    public static final Keyword const__6 = RT.keyword(null, (String)"n");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__8 = RT.keyword(null, (String)"s");

    public Object invoke(Object m, Object p__17704) {
        IPersistentMap iPersistentMap;
        Object object = p__17704;
        p__17704 = null;
        Object vec__17706 = object;
        Object k = RT.nth((Object)vec__17706, (int)RT.intCast((long)0L), null);
        Object object2 = vec__17706;
        vec__17706 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        IFn iFn = (IFn)const__3.getRawRoot();
        Object object3 = m;
        m = null;
        Object object4 = k;
        k = null;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke(object4);
        Object object6 = ((IFn)const__5.getRawRoot()).invoke(v);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__6;
            Object object7 = v;
            v = null;
            objectArray[1] = ((IFn)const__7.getRawRoot()).invoke(object7);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__8;
            Object object8 = v;
            v = null;
            objectArray[1] = object8;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        ddb$create_item$fn__17705 this_ = null;
        return iFn.invoke(object3, object5, (Object)iPersistentMap);
    }
}

