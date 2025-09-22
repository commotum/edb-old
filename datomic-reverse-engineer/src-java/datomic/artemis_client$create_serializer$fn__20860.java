/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Writer
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.fressian.Writer;

public final class artemis_client$create_serializer$fn__20860
extends AFunction {
    Object write_handlers;
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"create-writer");
    public static final Var const__1 = RT.var((String)"datomic.artemis-client", (String)"output-stream");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__3 = RT.var((String)"datomic.fressian", (String)"clojure-write-handlers");

    public artemis_client$create_serializer$fn__20860(Object object) {
        this.write_handlers = object;
    }

    public Object invoke(Object obj, Object msg) {
        Object fout;
        Object object = msg;
        msg = null;
        Object object2 = fout = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object), ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), this.write_handlers));
        fout = null;
        Object object3 = obj;
        obj = null;
        return ((Writer)object2).writeObject(object3);
    }
}

