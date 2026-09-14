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
import clojure.lang.Var;

public final class valcache$noop_quit
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"opcode");
    public static final Keyword const__4 = RT.keyword(null, (String)"key-length");
    public static final Keyword const__5 = RT.keyword(null, (String)"extras-length");
    public static final Keyword const__6 = RT.keyword(null, (String)"total-body-length");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__9 = RT.var((String)"datomic.valcache", (String)"reply-with-error");
    public static final Object const__10 = 4L;
    public static final Var const__11 = RT.var((String)"datomic.valcache", (String)"reply-empty-ok");

    public static Object invokeStatic(Object p__9654, Object sc) {
        Object object;
        Boolean bl;
        Object object2;
        Object object3 = p__9654;
        p__9654 = null;
        Object map__9655 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__9655);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__9655;
            map__9655 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__9655;
            map__9655 = null;
        }
        Object map__96552 = object2;
        Object opcode = RT.get((Object)map__96552, (Object)const__3);
        Object key_length = RT.get((Object)map__96552, (Object)const__4);
        Object extras_length = RT.get((Object)map__96552, (Object)const__5);
        Object object6 = map__96552;
        map__96552 = null;
        Object total_body_length = RT.get((Object)object6, (Object)const__6);
        IFn iFn = (IFn)const__7.getRawRoot();
        Object object7 = key_length;
        key_length = null;
        boolean and__5236__auto__9658 = Numbers.isZero((Object)object7);
        if (and__5236__auto__9658) {
            Object object8 = extras_length;
            extras_length = null;
            boolean and__5236__auto__9657 = Numbers.isZero((Object)object8);
            if (and__5236__auto__9657) {
                Object object9 = total_body_length;
                total_body_length = null;
                bl = Numbers.isZero((Object)object9) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                bl = and__5236__auto__9657 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__9658 ? Boolean.TRUE : Boolean.FALSE;
        }
        Object object10 = iFn.invoke((Object)bl);
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = opcode;
            opcode = null;
            Object object12 = sc;
            sc = null;
            object = ((IFn)const__9.getRawRoot()).invoke(object11, const__10, (Object)"Invalid args", object12);
        } else {
            Object object13 = opcode;
            opcode = null;
            Object object14 = sc;
            sc = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object13, object14);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$noop_quit.invokeStatic(object3, object4);
    }
}

