/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.process_monitor$convert_nanos_to_millis$fn__23490;
import datomic.process_monitor$convert_nanos_to_millis$round__23488;

public final class process_monitor$convert_nanos_to_millis
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce-kv");

    public static Object invokeStatic(Object snapshot) {
        process_monitor$convert_nanos_to_millis$round__23488 round2;
        process_monitor$convert_nanos_to_millis$round__23488 process_monitor$convert_nanos_to_millis$round__23488 = round2 = new process_monitor$convert_nanos_to_millis$round__23488();
        round2 = null;
        Object object = snapshot;
        snapshot = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new process_monitor$convert_nanos_to_millis$fn__23490((Object)process_monitor$convert_nanos_to_millis$round__23488), (Object)PersistentArrayMap.EMPTY, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return process_monitor$convert_nanos_to_millis.invokeStatic(object2);
    }
}

