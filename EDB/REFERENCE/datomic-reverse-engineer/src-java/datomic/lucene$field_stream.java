/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.document.AbstractField
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.document.AbstractField;
import java.io.ByteArrayInputStream;

public final class lucene$field_stream
extends AFunction {
    public static Object invokeStatic(Object field) {
        int n = ((AbstractField)field).getBinaryOffset();
        Object object = field;
        field = null;
        return new ByteArrayInputStream(((AbstractField)field).getBinaryValue(), n, ((AbstractField)object).getBinaryLength());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$field_stream.invokeStatic(object2);
    }
}

