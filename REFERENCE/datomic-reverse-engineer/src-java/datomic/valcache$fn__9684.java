/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.io.File;
import java.nio.file.Path;

public final class valcache$fn__9684
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"opcode");
    public static final Keyword const__4 = RT.keyword(null, (String)"key-length");
    public static final Keyword const__5 = RT.keyword(null, (String)"extras-length");
    public static final Keyword const__6 = RT.keyword(null, (String)"total-body-length");
    public static final Keyword const__7 = RT.keyword(null, (String)"root");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__12 = RT.var((String)"datomic.valcache", (String)"reply-with-error");
    public static final Object const__13 = 4L;
    public static final Var const__14 = RT.var((String)"datomic.valcache", (String)"read-key");
    public static final Var const__15 = RT.var((String)"datomic.valcache", (String)"full-path");
    public static final Var const__17 = RT.var((String)"datomic.valcache", (String)"reply-empty-ok");

    public static Object invokeStatic(Object p__9683, Object sc) {
        Object object;
        Boolean bl;
        Object object2;
        Object object3 = p__9683;
        p__9683 = null;
        Object map__9685 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__9685);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__9685;
            map__9685 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__9685;
            map__9685 = null;
        }
        Object map__96852 = object2;
        Object opcode = RT.get((Object)map__96852, (Object)const__3);
        Object key_length = RT.get((Object)map__96852, (Object)const__4);
        Object extras_length = RT.get((Object)map__96852, (Object)const__5);
        Object total_body_length = RT.get((Object)map__96852, (Object)const__6);
        Object object6 = map__96852;
        map__96852 = null;
        Object root = RT.get((Object)object6, (Object)const__7);
        IFn iFn = (IFn)const__8.getRawRoot();
        boolean and__5236__auto__9689 = Numbers.isPos((Object)key_length);
        if (and__5236__auto__9689) {
            Object object7 = extras_length;
            extras_length = null;
            boolean and__5236__auto__9688 = Numbers.isZero((Object)object7);
            if (and__5236__auto__9688) {
                Object object8 = total_body_length;
                total_body_length = null;
                bl = Util.equiv((Object)object8, (Object)key_length) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                bl = and__5236__auto__9688 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__9689 ? Boolean.TRUE : Boolean.FALSE;
        }
        Object object9 = iFn.invoke((Object)bl);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = opcode;
            opcode = null;
            Object object11 = sc;
            sc = null;
            object = ((IFn)const__12.getRawRoot()).invoke(object10, const__13, (Object)"Invalid args", object11);
        } else {
            Comparable<File> comparable;
            File file;
            File and__5236__auto__9690;
            File file2;
            Object path2;
            Object object12 = key_length;
            key_length = null;
            Object k = ((IFn)const__14.getRawRoot()).invoke(object12, sc);
            Object object13 = root;
            root = null;
            Object object14 = k;
            k = null;
            Object object15 = path2 = ((IFn)const__15.getRawRoot()).invoke(object13, object14);
            path2 = null;
            Object G__9686 = object15;
            if (Util.identical((Object)G__9686, null)) {
                file2 = null;
            } else {
                Object object16 = G__9686;
                G__9686 = null;
                file2 = ((Path)object16).toFile();
            }
            File file3 = and__5236__auto__9690 = (file = file2);
            if (file3 != null && file3 != Boolean.FALSE) {
                comparable = file.exists() ? Boolean.TRUE : Boolean.FALSE;
            } else {
                comparable = and__5236__auto__9690;
                and__5236__auto__9690 = null;
            }
            if (comparable != null && comparable != Boolean.FALSE) {
                File file4 = file;
                file = null;
                Boolean bl2 = file4.delete() ? Boolean.TRUE : Boolean.FALSE;
            }
            Object object17 = opcode;
            opcode = null;
            Object object18 = sc;
            sc = null;
            object = ((IFn)const__17.getRawRoot()).invoke(object17, object18);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$fn__9684.invokeStatic(object3, object4);
    }
}

