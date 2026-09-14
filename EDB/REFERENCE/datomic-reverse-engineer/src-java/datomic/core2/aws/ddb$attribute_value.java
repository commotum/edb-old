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
package datomic.core2.aws;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.ByteBuffer;

public final class ddb$attribute_value
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Keyword const__1 = RT.keyword(null, (String)"S");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Keyword const__3 = RT.keyword(null, (String)"N");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__7 = RT.keyword(null, (String)"B");
    public static final Keyword const__8 = RT.keyword(null, (String)"default");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__10 = RT.keyword((String)"datomic.core2.aws.ddb", (String)"value");

    public static Object invokeStatic(Object v) {
        IPersistentMap iPersistentMap;
        Object object = ((IFn)const__0.getRawRoot()).invoke(v);
        if (object != null && object != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__1;
            Object object2 = v;
            v = null;
            objectArray[1] = object2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            Object object3 = ((IFn)const__2.getRawRoot()).invoke(v);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__3;
                Object object4 = v;
                v = null;
                objectArray[1] = ((IFn)const__4.getRawRoot()).invoke(object4);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else if (v instanceof ByteBuffer) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__7;
                Object object5 = v;
                v = null;
                objectArray[1] = ((ByteBuffer)object5).duplicate();
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                Keyword keyword = const__8;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object[] objectArray = new Object[2];
                    objectArray[0] = const__10;
                    Object object6 = v;
                    v = null;
                    objectArray[1] = object6;
                    throw (Throwable)((IFn)const__9.getRawRoot()).invoke((Object)"No type defined", (Object)RT.mapUniqueKeys((Object[])objectArray));
                }
                iPersistentMap = null;
            }
        }
        return iPersistentMap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$attribute_value.invokeStatic(object2);
    }
}

