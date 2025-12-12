/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db$create_card_one_validator$fn__13305;
import java.util.HashMap;

public final class db$create_card_one_validator
extends AFunction {
    public static Object invokeStatic(Object db2) {
        HashMap eaomap = new HashMap();
        Object object = db2;
        db2 = null;
        HashMap hashMap = eaomap;
        eaomap = null;
        return new db$create_card_one_validator$fn__13305(object, hashMap);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$create_card_one_validator.invokeStatic(object2);
    }
}

