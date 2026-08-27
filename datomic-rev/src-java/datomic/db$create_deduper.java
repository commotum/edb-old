/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db$create_deduper$fn__13270;
import java.util.HashSet;

public final class db$create_deduper
extends AFunction {
    public static Object invokeStatic() {
        HashSet dset;
        HashSet hashSet = dset = new HashSet();
        dset = null;
        return new db$create_deduper$fn__13270(hashSet);
    }

    public Object invoke() {
        return db$create_deduper.invokeStatic();
    }
}

