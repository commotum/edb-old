/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.concurrent.TimeoutException;

public final class qtune$qtune$fn__23473
extends AFunction {
    Object oq;
    Object args;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"datomic.api", (String)"q");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__3 = RT.keyword(null, (String)"timeout");
    public static final AFn const__5 = (AFn)Tuple.create((Object)5000L);
    public static final Var const__8 = RT.var((String)"datomic.common", (String)"root-cause");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"println");

    public qtune$qtune$fn__23473(Object object, Object object2) {
        this.oq = object;
        this.args = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(this.oq, (Object)const__3, (Object)const__5), this.args);
        }
        catch (Exception ex2) {
            if (!(((IFn)const__8.getRawRoot()).invoke((Object)ex2) instanceof TimeoutException)) {
                Object ex2 = null;
                throw (Throwable)ex2;
            }
            object = ((IFn)const__9.getRawRoot()).invoke((Object)const__3);
        }
        return object;
    }
}

