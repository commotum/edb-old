/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.process_monitor$convert_nanos_to_millis$fn__23490$fn__23491;

public final class process_monitor$convert_nanos_to_millis$fn__23490
extends AFunction {
    Object round;
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"ends-with?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__4 = RT.var((String)"clojure.string", (String)"replace");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"reduce");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"lo"), (Object)RT.keyword(null, (String)"hi"), (Object)RT.keyword(null, (String)"sum"));

    public process_monitor$convert_nanos_to_millis$fn__23490(Object object) {
        this.round = object;
    }

    public Object invoke(Object m, Object k, Object v) {
        Object object;
        process_monitor$convert_nanos_to_millis$fn__23490 this_;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(k), (Object)"Nsec");
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = m;
            m = null;
            Object object4 = k;
            k = null;
            Object object5 = v;
            v = null;
            this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object3, ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object4), (Object)"Nsec", (Object)"Msec")), ((IFn)const__5.getRawRoot()).invoke((Object)new process_monitor$convert_nanos_to_millis$fn__23490$fn__23491(this_.round), object5, (Object)const__9));
        } else {
            Object object6 = m;
            m = null;
            Object object7 = k;
            k = null;
            Object object8 = v;
            v = null;
            this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object6, object7, object8);
        }
        return object;
    }
}

