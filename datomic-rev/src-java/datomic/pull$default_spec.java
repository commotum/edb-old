/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class pull$default_spec
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"reverse-lookup?");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Keyword const__5 = RT.keyword(null, (String)"wildcard");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"class");
    public static final Keyword const__7 = RT.keyword(null, (String)"dbid");

    public static Object invokeStatic(Object attr, Object kw, Object db2) {
        IPersistentMap iPersistentMap;
        Object object;
        Object and__5236__auto__18998;
        Object object2 = and__5236__auto__18998 = attr;
        if (object2 != null && object2 != Boolean.FALSE) {
            object = Util.equiv((long)20L, (Object)((Attribute)attr).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__18998;
            and__5236__auto__18998 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            Object object3;
            Object and__5236__auto__18999;
            Object object4 = attr;
            attr = null;
            Object object5 = and__5236__auto__18999 = ((Attribute)object4).isComponent;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = db2;
                db2 = null;
                object3 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object6, ((IFn)const__4.getRawRoot()).invoke(kw)));
            } else {
                object3 = and__5236__auto__18999;
                Object var3_3 = null;
            }
            if (object3 != null && object3 != Boolean.FALSE) {
                Object[] objectArray = new Object[4];
                objectArray[0] = const__5;
                objectArray[1] = ((IFn)const__6.getRawRoot()).invoke(kw);
                objectArray[2] = const__7;
                Object object7 = kw;
                kw = null;
                objectArray[3] = ((IFn)const__6.getRawRoot()).invoke(object7);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__7;
                Object object8 = kw;
                kw = null;
                objectArray[1] = ((IFn)const__6.getRawRoot()).invoke(object8);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            }
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return pull$default_spec.invokeStatic(object4, object5, object6);
    }
}

