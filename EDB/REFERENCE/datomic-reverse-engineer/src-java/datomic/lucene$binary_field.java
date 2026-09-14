/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOLLO
 *  clojure.lang.RT
 *  com.datomic.lucene.document.Field
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import com.datomic.lucene.document.Field;

public final class lucene$binary_field
extends AFunction
implements IFn.OOLLO {
    public static Object invokeStatic(Object name, Object value, long offset, long l) {
        Object object = name;
        name = null;
        Object object2 = value;
        value = null;
        return new Field((String)object, (byte[])object2, RT.intCast((long)offset), RT.intCast((long)l));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        return lucene$binary_field.invokeStatic(object5, object6, RT.longCast((Object)((Number)object3)), RT.longCast((Object)((Number)object4)));
    }

    public final Object invokePrim(Object object, Object object2, long l, long l2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return lucene$binary_field.invokeStatic(object3, object4, l, l2);
    }
}

