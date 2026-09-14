/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.StringWriter;

public final class error$add_details_to_msg$fn__674
extends AFunction {
    Object details;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*out*");
    public static final Var const__3 = RT.var((String)"clojure.pprint", (String)"pprint");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public error$add_details_to_msg$fn__674(Object object) {
        this.details = object;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            StringWriter s__6071__auto__676 = new StringWriter();
            ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2, (Object)s__6071__auto__676));
            try {
                ((IFn)const__3.getRawRoot()).invoke(this.details);
                StringWriter stringWriter = s__6071__auto__676;
                s__6071__auto__676 = null;
                object2 = ((IFn)const__4.getRawRoot()).invoke((Object)stringWriter);
            }
            finally {
                ((IFn)const__5.getRawRoot()).invoke();
            }
            object = object2;
        }
        finally {
            ((IFn)const__5.getRawRoot()).invoke();
        }
        return object;
    }
}

