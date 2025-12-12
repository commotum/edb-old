/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.pull$attr_spec__GT_fn$fn__18934;
import java.util.List;

public final class pull$attr_spec__GT_fn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__4 = RT.var((String)"datomic.pull", (String)"default-limit");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Keyword const__11 = RT.keyword(null, (String)"default");
    public static final Keyword const__12 = RT.keyword(null, (String)"limit");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Var const__15 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__16 = RT.keyword((String)"db.error", (String)"invalid-limit");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__18 = RT.keyword((String)"db.error", (String)"invalid-attr-spec");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"class");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object attr_spec) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)(attr_spec instanceof List ? Boolean.TRUE : Boolean.FALSE));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = Tuple.create((Object)((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot()), (Object)const__5.getRawRoot());
            return object;
        }
        Object vec__18930 = attr_spec;
        Object sym = RT.nth((Object)vec__18930, (int)RT.intCast((long)0L), null);
        RT.nth((Object)vec__18930, (int)RT.intCast((long)1L), null);
        Object object3 = vec__18930;
        vec__18930 = null;
        Object arg2 = RT.nth((Object)object3, (int)RT.intCast((long)2L), null);
        Object object4 = sym;
        sym = null;
        Object G__18933 = ((IFn)const__10.getRawRoot()).invoke(object4);
        switch (Util.hash((Object)G__18933) >> 2 & 1) {
            case 0: {
                if (G__18933 != const__11) break;
                Object object5 = arg2;
                arg2 = null;
                object = Tuple.create((Object)((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot()), (Object)((Object)new pull$attr_spec__GT_fn$fn__18934(object5)));
                return object;
            }
            case 1: {
                Object object6;
                Object or__5238__auto__18938;
                if (G__18933 != const__12) break;
                Object object7 = or__5238__auto__18938 = ((IFn)const__13.getRawRoot()).invoke(arg2);
                if (object7 != null && object7 != Boolean.FALSE) {
                    object6 = or__5238__auto__18938;
                    or__5238__auto__18938 = null;
                } else {
                    object6 = Util.identical((Object)arg2, null) ? Boolean.TRUE : Boolean.FALSE;
                }
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object8 = arg2;
                    arg2 = null;
                    object = Tuple.create((Object)object8, (Object)const__5.getRawRoot());
                    return object;
                }
                Object object9 = arg2;
                arg2 = null;
                Object object10 = attr_spec;
                attr_spec = null;
                object = ((IFn)const__15.getRawRoot()).invoke((Object)const__16, ((IFn)const__17.getRawRoot()).invoke((Object)"'", object9, (Object)"' is not a valid limit in '", object10, (Object)"'"));
                return object;
            }
        }
        Object object12 = attr_spec;
        object12 = attr_spec;
        attr_spec = null;
        object = ((IFn)const__15.getRawRoot()).invoke((Object)const__18, ((IFn)const__17.getRawRoot()).invoke((Object)"Cannot interpret as an attribute spec: ", object11, (Object)" of class: ", ((IFn)const__19.getRawRoot()).invoke(object12)));
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$attr_spec__GT_fn.invokeStatic(object2);
    }
}

