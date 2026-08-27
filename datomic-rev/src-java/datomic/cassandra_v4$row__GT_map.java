/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  com.datastax.oss.driver.api.core.data.GettableByIndex
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import com.datastax.oss.driver.api.core.data.GettableByIndex;

public final class cassandra_v4$row__GT_map
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"id");
    public static final Keyword const__2 = RT.keyword(null, (String)"rev");
    public static final Keyword const__3 = RT.keyword(null, (String)"map");
    public static final Keyword const__4 = RT.keyword(null, (String)"val");
    public static final AFn const__5 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id"), (Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"map"), (Object)RT.keyword(null, (String)"val"));
    public static final Keyword const__11 = RT.keyword(null, (String)"chunks");
    public static final AFn const__12 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id2"), (Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"map"), (Object)RT.keyword(null, (String)"val"), (Object)RT.keyword(null, (String)"chunks"));
    public static final Keyword const__14 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object row, Object ks) {
        IPersistentMap iPersistentMap;
        if (Util.equiv((Object)ks, (Object)const__5)) {
            Object[] objectArray = new Object[8];
            objectArray[0] = const__1;
            objectArray[1] = ((GettableByIndex)row).getString(RT.intCast((long)0L));
            objectArray[2] = const__2;
            objectArray[3] = Numbers.num((long)((GettableByIndex)row).getLong(RT.intCast((long)1L)));
            objectArray[4] = const__3;
            objectArray[5] = ((GettableByIndex)row).getString(RT.intCast((long)2L));
            objectArray[6] = const__4;
            Object object = row;
            row = null;
            objectArray[7] = ((GettableByIndex)object).getByteBuffer(RT.intCast((long)3L));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            Object object = ks;
            ks = null;
            if (Util.equiv((Object)object, (Object)const__12)) {
                Object[] objectArray = new Object[10];
                objectArray[0] = const__1;
                objectArray[1] = ((GettableByIndex)row).getString(RT.intCast((long)0L));
                objectArray[2] = const__2;
                objectArray[3] = Numbers.num((long)((GettableByIndex)row).getLong(RT.intCast((long)1L)));
                objectArray[4] = const__3;
                objectArray[5] = ((GettableByIndex)row).getString(RT.intCast((long)2L));
                objectArray[6] = const__4;
                objectArray[7] = ((GettableByIndex)row).getByteBuffer(RT.intCast((long)3L));
                objectArray[8] = const__11;
                Object object2 = row;
                row = null;
                objectArray[9] = ((GettableByIndex)object2).getInt(RT.intCast((long)4L));
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                Keyword keyword = const__14;
                if (keyword != null && keyword != Boolean.FALSE) {
                    throw (Throwable)new RuntimeException("Invalid select.");
                }
                iPersistentMap = null;
            }
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cassandra_v4$row__GT_map.invokeStatic(object3, object4);
    }
}

