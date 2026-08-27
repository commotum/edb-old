/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class valcache$fn__9693
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"opcode");
    public static final Keyword const__4 = RT.keyword(null, (String)"key-length");
    public static final Keyword const__5 = RT.keyword(null, (String)"extras-length");
    public static final Keyword const__6 = RT.keyword(null, (String)"total-body-length");
    public static final Keyword const__7 = RT.keyword(null, (String)"root");
    public static final Keyword const__8 = RT.keyword(null, (String)"sasl");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__11 = RT.var((String)"datomic.valcache", (String)"reply-with-error");
    public static final Object const__12 = 4L;
    public static final Var const__13 = RT.var((String)"datomic.valcache", (String)"read-key");
    public static final Var const__15 = RT.var((String)"datomic.io", (String)"read-n-bytes");
    public static final Var const__18 = RT.var((String)"datomic.valcache", (String)"scan-strings");
    public static final Var const__24 = RT.var((String)"datomic.valcache", (String)"reply-empty-ok");
    public static final Object const__25 = 32L;
    public static final Object const__26 = 131L;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"username"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"password"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object p__9692, Object sc) {
        Object object;
        Object object2;
        Object object3 = p__9692;
        p__9692 = null;
        Object map__9694 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__9694);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__9694;
            map__9694 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__9694;
            map__9694 = null;
        }
        Object map__96942 = object2;
        Object opcode = RT.get((Object)map__96942, (Object)const__3);
        Object key_length = RT.get((Object)map__96942, (Object)const__4);
        Object extras_length = RT.get((Object)map__96942, (Object)const__5);
        Object total_body_length = RT.get((Object)map__96942, (Object)const__6);
        RT.get((Object)map__96942, (Object)const__7);
        Object object6 = map__96942;
        map__96942 = null;
        Object sasl = RT.get((Object)object6, (Object)const__8);
        Object object7 = ((IFn)const__9.getRawRoot()).invoke((Object)(Numbers.isPos((Object)key_length) ? Boolean.TRUE : Boolean.FALSE));
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = opcode;
            opcode = null;
            Object object9 = sc;
            sc = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object8, const__12, (Object)"Invalid args", object9);
        } else {
            Object k = ((IFn)const__13.getRawRoot()).invoke(key_length, sc);
            Object object10 = Numbers.isZero((Object)extras_length) ? null : ((IFn)const__13.getRawRoot()).invoke(extras_length, sc);
            Object object11 = total_body_length;
            total_body_length = null;
            Object object12 = key_length;
            key_length = null;
            Object object13 = extras_length;
            extras_length = null;
            Object bb = ((IFn)const__15.getRawRoot()).invoke((Object)Numbers.minus((Object)Numbers.minus((Object)object11, (Object)object12), (Object)object13), sc);
            Object object14 = k;
            k = null;
            if (Util.equiv((Object)object14, (Object)"PLAIN")) {
                boolean bl;
                Object object15 = bb;
                bb = null;
                Object vec__9695 = ((IFn)const__18.getRawRoot()).invoke(object15);
                Object username = RT.nth((Object)vec__9695, (int)RT.intCast((long)0L), null);
                Object object16 = vec__9695;
                vec__9695 = null;
                Object password = RT.nth((Object)object16, (int)RT.intCast((long)1L), null);
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object17 = sasl;
                Object object18 = iLookupThunk.get(object17);
                if (iLookupThunk == object18) {
                    __thunk__0__ = __site__0__.fault(object17);
                    object18 = __thunk__0__.get(object17);
                }
                Object object19 = username;
                username = null;
                boolean and__5236__auto__9699 = Util.equiv((Object)object18, (Object)object19);
                if (and__5236__auto__9699) {
                    ILookupThunk iLookupThunk2 = __thunk__1__;
                    Object object20 = sasl;
                    sasl = null;
                    Object object21 = iLookupThunk2.get(object20);
                    if (iLookupThunk2 == object21) {
                        __thunk__1__ = __site__1__.fault(object20);
                        object21 = __thunk__1__.get(object20);
                    }
                    Object object22 = password;
                    password = null;
                    bl = Util.equiv((Object)object21, (Object)object22);
                } else {
                    bl = and__5236__auto__9699;
                }
                if (bl) {
                    Object object23 = opcode;
                    opcode = null;
                    Object object24 = sc;
                    sc = null;
                    ((IFn)const__24.getRawRoot()).invoke(object23, object24);
                    object = Boolean.TRUE;
                } else {
                    Object object25 = sc;
                    sc = null;
                    ((IFn)const__24.getRawRoot()).invoke(const__25, object25);
                    object = Boolean.FALSE;
                }
            } else {
                Object object26 = opcode;
                opcode = null;
                Object object27 = sc;
                sc = null;
                ((IFn)const__11.getRawRoot()).invoke(object26, const__26, (Object)"Not supported", object27);
                object = Boolean.FALSE;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$fn__9693.invokeStatic(object3, object4);
    }
}

