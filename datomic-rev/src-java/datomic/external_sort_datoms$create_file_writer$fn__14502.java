/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.fressian.Writer
 */
package datomic;

import clojure.lang.AFunction;
import org.fressian.Writer;

public final class external_sort_datoms$create_file_writer$fn__14502
extends AFunction {
    Object writer;

    public external_sort_datoms$create_file_writer$fn__14502(Object object) {
        this.writer = object;
    }

    public Object invoke(Object o) {
        Object object = o;
        o = null;
        return ((Writer)this.writer).writeObject(object);
    }
}

