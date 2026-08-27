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

public final class valcache$supported_or_drain
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"opcode");
    public static final Keyword const__4 = RT.keyword(null, (String)"data-type");
    public static final Keyword const__5 = RT.keyword(null, (String)"vbucket-id");
    public static final Keyword const__6 = RT.keyword(null, (String)"cas");
    public static final Keyword const__7 = RT.keyword(null, (String)"total-body-length");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"methods");
    public static final Var const__12 = RT.var((String)"datomic.io", (String)"read-n-bytes");
    public static final Var const__13 = RT.var((String)"datomic.valcache", (String)"reply-with-error");
    public static final Object const__14 = 131L;

    public static Object invokeStatic(Object p__9647, Object sc, Object mmeth) {
        Object object;
        Object object2;
        Object map__9648;
        Object object3;
        Object object4 = p__9647;
        p__9647 = null;
        Object map__96482 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__96482);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__96482;
            map__96482 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__96482;
            map__96482 = null;
        }
        Object header = map__9648 = object3;
        Object opcode = RT.get((Object)map__9648, (Object)const__3);
        Object data_type = RT.get((Object)map__9648, (Object)const__4);
        Object vbucket_id = RT.get((Object)map__9648, (Object)const__5);
        Object cas = RT.get((Object)map__9648, (Object)const__6);
        Object object7 = map__9648;
        map__9648 = null;
        Object total_body_length = RT.get((Object)object7, (Object)const__7);
        IFn iFn = (IFn)const__8.getRawRoot();
        Object object8 = data_type;
        data_type = null;
        boolean and__5236__auto__9652 = Numbers.isZero((Object)object8);
        if (and__5236__auto__9652) {
            Object object9 = vbucket_id;
            vbucket_id = null;
            boolean and__5236__auto__9651 = Numbers.isZero((Object)object9);
            if (and__5236__auto__9651) {
                Object object10 = cas;
                cas = null;
                boolean and__5236__auto__9650 = Numbers.isZero((Object)object10);
                if (and__5236__auto__9650) {
                    Object object11 = mmeth;
                    mmeth = null;
                    object2 = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object11), opcode);
                } else {
                    object2 = and__5236__auto__9650 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object2 = and__5236__auto__9651 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object2 = and__5236__auto__9652 ? Boolean.TRUE : Boolean.FALSE;
        }
        Object object12 = iFn.invoke(object2);
        if (object12 != null && object12 != Boolean.FALSE) {
            Object object13 = total_body_length;
            total_body_length = null;
            ((IFn)const__12.getRawRoot()).invoke(object13, sc);
            Object object14 = opcode;
            opcode = null;
            Object object15 = sc;
            sc = null;
            object = ((IFn)const__13.getRawRoot()).invoke(object14, const__14, (Object)"Not supported", object15);
        } else {
            object = header;
            header = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return valcache$supported_or_drain.invokeStatic(object4, object5, object6);
    }
}

