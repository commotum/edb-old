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
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$describe_bbuf
extends AFunction {
    public static final Keyword const__2 = RT.keyword(null, (String)"crc32");
    public static final Var const__3 = RT.var((String)"datomic.io", (String)"crc32");
    public static final Keyword const__4 = RT.keyword(null, (String)"size");

    public static Object invokeStatic(Object bbuf) {
        IPersistentMap iPersistentMap;
        if (bbuf instanceof ByteBuffer) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__2;
            objectArray[1] = ((IFn)const__3.getRawRoot()).invoke(bbuf);
            objectArray[2] = const__4;
            Object object = bbuf;
            bbuf = null;
            objectArray[3] = ((Buffer)object).remaining();
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$describe_bbuf.invokeStatic(object2);
    }
}

