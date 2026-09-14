/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.handlers.ILookup
 *  org.fressian.handlers.WriteHandlerLookup
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.fressian.handlers.ILookup;
import org.fressian.handlers.WriteHandlerLookup;

public final class fressian$write_handler_lookup
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"as-lookup");

    public static Object invokeStatic(Object custom_lookup) {
        Object object = custom_lookup;
        custom_lookup = null;
        return WriteHandlerLookup.createLookupChain((ILookup)((ILookup)((IFn)const__0.getRawRoot()).invoke(object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$write_handler_lookup.invokeStatic(object2);
    }
}

