/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.net.URI;
import java.util.Map;

public final class uri$storage_protocol
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Keyword const__4 = RT.keyword(null, (String)"protocol");

    public static Object invokeStatic(Object uri2) {
        Object object;
        if (uri2 instanceof Map) {
            Object object2;
            Object or__5238__auto__16834;
            IFn iFn = (IFn)const__2.getRawRoot();
            Object object3 = or__5238__auto__16834 = RT.get((Object)uri2, (Object)const__4);
            if (object3 != null && object3 != Boolean.FALSE) {
                object2 = or__5238__auto__16834;
                or__5238__auto__16834 = null;
            } else {
                Object object4 = uri2;
                uri2 = null;
                object2 = RT.get((Object)object4, (Object)"protocol");
            }
            object = iFn.invoke(object2);
        } else {
            Object object5 = uri2;
            uri2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(RT.nth((Object)new URI((String)object5).getSchemeSpecificPart().split(":"), (int)RT.intCast((long)0L)));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$storage_protocol.invokeStatic(object2);
    }
}

