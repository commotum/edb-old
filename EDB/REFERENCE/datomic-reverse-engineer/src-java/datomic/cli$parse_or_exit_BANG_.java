/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cli$parse_or_exit_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cli", (String)"parse-or-exit!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"some");
    public static final AFn const__2 = (AFn)PersistentHashSet.create((Object[])new Object[]{"--help"});
    public static final Var const__3 = RT.var((String)"datomic.cli", (String)"print-help");
    public static final Var const__5 = RT.var((String)"datomic.cli", (String)"apply-defaults");
    public static final Var const__6 = RT.var((String)"datomic.cli", (String)"coerce-vals");
    public static final Var const__7 = RT.var((String)"datomic.cli", (String)"expand-short-names");
    public static final Var const__8 = RT.var((String)"datomic.cli", (String)"cli->map");
    public static final Var const__9 = RT.var((String)"datomic.cli", (String)"missing-values");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"println");

    public static Object invokeStatic(Object cmd, Object args, Object spec, Object positions, Object vararg) {
        Object object;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, args);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = cmd;
            cmd = null;
            Object object4 = spec;
            spec = null;
            Object object5 = positions;
            positions = null;
            ((IFn)const__3.getRawRoot()).invoke(object3, object4, object5);
            System.exit(RT.intCast((long)-1L));
            object = null;
        } else {
            Object object6 = args;
            args = null;
            Object object7 = vararg;
            vararg = null;
            Object m = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(object6, positions, object7), spec), spec), spec);
            Object missing = ((IFn)const__9.getRawRoot()).invoke(m, spec);
            Object object8 = ((IFn)const__10.getRawRoot()).invoke(missing);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object object9 = cmd;
                cmd = null;
                Object object10 = spec;
                spec = null;
                Object object11 = positions;
                positions = null;
                ((IFn)const__3.getRawRoot()).invoke(object9, object10, object11);
                Object object12 = missing;
                missing = null;
                ((IFn)const__11.getRawRoot()).invoke((Object)"\n**** missing required arguments", object12, (Object)"****");
                System.exit(RT.intCast((long)-1L));
                object = null;
            } else {
                object = m;
                m = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return cli$parse_or_exit_BANG_.invokeStatic(object6, object7, object8, object9, object10);
    }

    public static Object invokeStatic(Object cmd, Object arg2, Object spec, Object positions) {
        Object object = cmd;
        cmd = null;
        Object object2 = arg2;
        arg2 = null;
        Object object3 = spec;
        spec = null;
        Object object4 = positions;
        positions = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, object4, null);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return cli$parse_or_exit_BANG_.invokeStatic(object5, object6, object7, object8);
    }
}

