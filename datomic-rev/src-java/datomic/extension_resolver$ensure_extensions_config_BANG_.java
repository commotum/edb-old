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

public final class extension_resolver$ensure_extensions_config_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"xforms");
    public static final Var const__4 = RT.var((String)"datomic.extension-resolver", (String)"ensure-allow-list!");

    public static Object invokeStatic(Object p__14311) {
        Object object;
        Object xforms;
        Object map__14312;
        Object object2;
        Object object3 = p__14311;
        p__14311 = null;
        Object map__143122 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__143122);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__143122;
            map__143122 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__143122;
            map__143122 = null;
        }
        Object object6 = map__14312 = object2;
        map__14312 = null;
        Object object7 = xforms = RT.get((Object)object6, (Object)const__3);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = xforms;
            xforms = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object8);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$ensure_extensions_config_BANG_.invokeStatic(object2);
    }
}

