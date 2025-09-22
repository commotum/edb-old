/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.fressian.Reader
 */
package datomic;

import clojure.lang.AFunction;
import java.io.EOFException;
import org.fressian.Reader;

public final class fressian$read_batch$fn__12183
extends AFunction {
    Object fin;
    Object sentinel;

    public fressian$read_batch$fn__12183(Object object, Object object2) {
        this.fin = object;
        this.sentinel = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((Reader)this.fin).readObject();
        }
        catch (EOFException e) {
            object = this.sentinel;
        }
        return object;
    }
}

