/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OLO
 *  clojure.lang.RT
 *  com.datomic.lucene.document.Field$Store
 *  com.datomic.lucene.document.NumericField
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import com.datomic.lucene.document.Field;
import com.datomic.lucene.document.NumericField;

public final class lucene$long_field
extends AFunction
implements IFn.OLO {
    public static Object invokeStatic(Object name, long value) {
        Object object = name;
        name = null;
        NumericField G__12248 = new NumericField((String)object, Field.Store.YES, Boolean.TRUE.booleanValue());
        G__12248.setLongValue(value);
        Object var3_2 = null;
        return G__12248;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        return lucene$long_field.invokeStatic(object3, RT.longCast((Object)((Number)object2)));
    }

    public final Object invokePrim(Object object, long l) {
        Object object2 = object;
        object = null;
        return lucene$long_field.invokeStatic(object2, l);
    }
}

