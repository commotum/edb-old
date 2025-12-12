/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.fressian$read_batch$fn__12183;

public final class fressian$read_batch
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj");

    public static Object invokeStatic(Object fin) {
        Object sentinel = new Object();
        Object objects = PersistentVector.EMPTY;
        while (true) {
            Object obj;
            if (Util.equiv((Object)(obj = ((IFn)new fressian$read_batch$fn__12183(fin, sentinel)).invoke()), (Object)sentinel)) break;
            PersistentVector persistentVector = objects;
            objects = null;
            Object object = obj;
            obj = null;
            objects = ((IFn)const__1.getRawRoot()).invoke((Object)persistentVector, object);
        }
        PersistentVector persistentVector = objects;
        objects = null;
        return persistentVector;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$read_batch.invokeStatic(object2);
    }
}

