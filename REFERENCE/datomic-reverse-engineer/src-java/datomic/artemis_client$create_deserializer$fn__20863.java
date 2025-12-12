/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.fressian.Reader;

public final class artemis_client$create_deserializer$fn__20863
extends AFunction {
    Object read_handlers;
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"create-reader");
    public static final Var const__1 = RT.var((String)"datomic.artemis-client", (String)"input-stream");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__3 = RT.var((String)"datomic.fressian", (String)"clojure-read-handlers");

    public artemis_client$create_deserializer$fn__20863(Object object) {
        this.read_handlers = object;
    }

    public Object invoke(Object msg) {
        Object fin;
        Object object = msg;
        msg = null;
        Object object2 = fin = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object), ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), this.read_handlers));
        fin = null;
        return ((Reader)object2).readObject();
    }
}

