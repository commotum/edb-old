/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.FressianWriter
 *  org.fressian.handlers.ILookup
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.OutputStream;
import org.fressian.FressianWriter;
import org.fressian.handlers.ILookup;

public final class fressian$create_writer
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"create-writer");
    public static final Var const__1 = RT.var((String)"datomic.fressian", (String)"as-lookup");

    public static Object invokeStatic(Object out, Object lookup) {
        Object object = out;
        out = null;
        Object object2 = lookup;
        lookup = null;
        return new FressianWriter((OutputStream)object, (ILookup)((IFn)const__1.getRawRoot()).invoke(object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fressian$create_writer.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object out) {
        Object object = out;
        out = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$create_writer.invokeStatic(object2);
    }
}

