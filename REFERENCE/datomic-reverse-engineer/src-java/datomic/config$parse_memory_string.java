/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.regex.Pattern;

public final class config$parse_memory_string
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"re-matches");
    public static final Object const__1 = Pattern.compile("(?i)(\\d+)([gmk])");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"*");
    public static final Var const__7 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final String const__8 = "g";
    public static final String const__12 = "k";
    public static final Object const__13 = 1024L;
    public static final String const__14 = "m";
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"integer?");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object s) {
        Object object;
        block7: {
            block5: {
                Object object2;
                Object object3;
                block6: {
                    Object temp__5455__auto__762;
                    Object object4 = s;
                    if (object4 == null) return null;
                    if (object4 == Boolean.FALSE) return null;
                    Object object5 = temp__5455__auto__762 = ((IFn)const__0.getRawRoot()).invoke(const__1, s);
                    if (object5 == null || object5 == Boolean.FALSE) break block5;
                    Object object6 = temp__5455__auto__762;
                    temp__5455__auto__762 = null;
                    Object vec__757 = object6;
                    RT.nth((Object)vec__757, (int)RT.intCast((long)0L), null);
                    Object mult = RT.nth((Object)vec__757, (int)RT.intCast((long)1L), null);
                    Object object7 = vec__757;
                    vec__757 = null;
                    Object abbr = RT.nth((Object)object7, (int)RT.intCast((long)2L), null);
                    Object object8 = mult;
                    mult = null;
                    object3 = ((IFn)const__7.getRawRoot()).invoke(object8);
                    Object object9 = abbr;
                    abbr = null;
                    String G__760 = ((String)object9).toLowerCase();
                    switch (Util.hash((Object)G__760)) {
                        case 103: {
                            if (!Util.equiv((Object)G__760, (Object)const__8)) break;
                            object2 = Numbers.num((long)Numbers.multiply((long)Numbers.multiply((long)1024L, (long)1024L), (long)1024L));
                            break block6;
                        }
                        case 107: {
                            if (!Util.equiv((Object)G__760, (Object)const__12)) break;
                            object2 = ((IFn)const__6.getRawRoot()).invoke(const__13);
                            break block6;
                        }
                        case 109: {
                            if (!Util.equiv((Object)G__760, (Object)const__14)) break;
                            object2 = Numbers.num((long)Numbers.multiply((long)1024L, (long)1024L));
                            break block6;
                        }
                    }
                    String string = G__760;
                    G__760 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__17.getRawRoot()).invoke((Object)"No matching clause: ", (Object)string));
                }
                object = Numbers.multiply((Object)object3, (Object)object2);
                break block7;
            }
            Object object10 = s;
            s = null;
            object = ((IFn)const__7.getRawRoot()).invoke(object10);
        }
        Object parsed = object;
        Object object11 = ((IFn)const__18.getRawRoot()).invoke(parsed);
        if (object11 == null) return null;
        if (object11 == Boolean.FALSE) return null;
        Object object12 = parsed;
        return object12;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$parse_memory_string.invokeStatic(object2);
    }
}

