/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.fressian.FressianReader
 */
package datomic;

import clojure.lang.AFunction;
import org.fressian.FressianReader;

public final class fressian$fn__12181
extends AFunction {
    public static Object invokeStatic(Object reader2) {
        Object object = reader2;
        reader2 = null;
        return ((FressianReader)object).readObject();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$fn__12181.invokeStatic(object2);
    }
}

