/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.List;

public final class pull$attr_spec__GT_attr
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.pull", (String)"attr-spec->attr");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Object const__11 = Character.valueOf(':');
    public static final Var const__12 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__13 = RT.keyword((String)"db.error", (String)"invalid-attr-spec");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"class");

    public static Object invokeStatic(Object attr_spec) {
        Object object;
        if (attr_spec instanceof List) {
            Object object2 = attr_spec;
            attr_spec = null;
            object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object2));
        } else {
            Object object3 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(attr_spec));
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4;
                Object s = ((IFn)const__6.getRawRoot()).invoke(attr_spec);
                boolean or__5238__auto__18940 = Util.equiv((Object)s, (Object)"*");
                if (or__5238__auto__18940 ? or__5238__auto__18940 : Util.equiv((Object)s, (Object)":*")) {
                    object4 = "*";
                } else {
                    object4 = s;
                    s = null;
                }
                Object s2 = object4;
                if (Util.equiv((Object)s2, (Object)"*")) {
                    Object object5 = ((IFn)const__8.getRawRoot()).invoke(attr_spec);
                    if (object5 != null && object5 != Boolean.FALSE) {
                        object = s2;
                        s2 = null;
                    } else {
                        Object object6 = attr_spec;
                        attr_spec = null;
                        object = ((IFn)const__9.getRawRoot()).invoke(object6);
                    }
                } else if (Util.equiv((char)((String)s2).charAt(RT.intCast((long)0L)), (char)((Character)const__11).charValue())) {
                    object = s2;
                    s2 = null;
                } else {
                    Object object7 = s2;
                    Object object8 = s2;
                    s2 = null;
                    object = ((IFn)const__12.getRawRoot()).invoke((Object)const__13, ((IFn)const__6.getRawRoot()).invoke((Object)"Attribute identifier ", object7, (Object)" of class: ", ((IFn)const__14.getRawRoot()).invoke(object8), (Object)" does not start with a colon"));
                }
            } else {
                object = attr_spec;
                Object object9 = null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$attr_spec__GT_attr.invokeStatic(object2);
    }
}

