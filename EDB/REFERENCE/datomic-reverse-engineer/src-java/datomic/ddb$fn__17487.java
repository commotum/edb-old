/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.services.dynamodbv2.model.AttributeValue
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

public final class ddb$fn__17487
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__2 = RT.keyword(null, (String)"bOOL");
    public static final Keyword const__3 = RT.keyword(null, (String)"nS");
    public static final Keyword const__4 = RT.keyword(null, (String)"n");
    public static final Keyword const__5 = RT.keyword(null, (String)"m");
    public static final Keyword const__6 = RT.keyword(null, (String)"s");
    public static final Keyword const__7 = RT.keyword(null, (String)"l");
    public static final Keyword const__8 = RT.keyword(null, (String)"nULL");
    public static final Keyword const__9 = RT.keyword(null, (String)"bS");
    public static final Keyword const__10 = RT.keyword(null, (String)"b");
    public static final Keyword const__11 = RT.keyword(null, (String)"sS");
    public static final AFn const__12 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"bOOL"), RT.keyword(null, (String)"nS"), RT.keyword(null, (String)"n"), RT.keyword(null, (String)"m"), RT.keyword(null, (String)"s"), RT.keyword(null, (String)"l"), RT.keyword(null, (String)"nULL"), RT.keyword(null, (String)"bS"), RT.keyword(null, (String)"b"), RT.keyword(null, (String)"sS")});
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__17 = RT.keyword(null, (String)"legal-keys");
    public static final AFn const__18 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"bOOL"), RT.keyword(null, (String)"nS"), RT.keyword(null, (String)"n"), RT.keyword(null, (String)"m"), RT.keyword(null, (String)"s"), RT.keyword(null, (String)"l"), RT.keyword(null, (String)"nULL"), RT.keyword(null, (String)"bS"), RT.keyword(null, (String)"b"), RT.keyword(null, (String)"sS")});
    public static final Keyword const__19 = RT.keyword(null, (String)"keys");
    public static final Keyword const__20 = RT.keyword(null, (String)"constructor");
    public static final Object const__21 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.AttributeValue");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__23 = RT.var((String)"datomic.datafy", (String)"property-to-object");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"class");
    public static final Object const__25 = RT.classForName((String)"java.lang.String");
    public static final Object const__26 = RT.classForName((String)"java.lang.Boolean");
    public static final Object const__27 = RT.classForName((String)"java.util.Collection");
    public static final Object const__28 = RT.classForName((String)"java.nio.ByteBuffer");
    public static final Object const__29 = RT.classForName((String)"java.util.Map");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"n"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"bOOL"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"nS"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"b"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"bS"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"nULL"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"m"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"l"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"sS"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"s"));
    static ILookupThunk __thunk__9__ = __site__9__;

    public static Object invokeStatic(Object m, Object _) {
        Object k;
        Object v;
        Object temp__5457__auto__17489;
        Object object = temp__5457__auto__17489 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__12, ((IFn)const__13.getRawRoot()).invoke(m)));
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__17489;
            temp__5457__auto__17489 = null;
            Object bad_ks = object2;
            Object object3 = ((IFn)const__15.getRawRoot()).invoke(const__16.getRawRoot(), (Object)"Unexpected keys ", bad_ks);
            Object[] objectArray = new Object[6];
            objectArray[0] = const__17;
            objectArray[1] = const__18;
            objectArray[2] = const__19;
            Object object4 = bad_ks;
            bad_ks = null;
            objectArray[3] = object4;
            objectArray[4] = const__20;
            objectArray[5] = const__21;
            throw (Throwable)((IFn)const__14.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        AttributeValue o = new AttributeValue();
        Object object5 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__4);
        if (object5 != null && object5 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = m;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            Object object8 = v = object7;
            v = null;
            Object object9 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__4, object8, const__25);
            k = null;
            o.setN((String)object9);
        }
        Object object10 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__2);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object11 = m;
            Object object12 = iLookupThunk.get(object11);
            if (iLookupThunk == object12) {
                __thunk__1__ = __site__1__.fault(object11);
                object12 = __thunk__1__.get(object11);
            }
            Object object13 = v = object12;
            v = null;
            Object object14 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__2, object13, const__26);
            k = null;
            o.setBOOL((Boolean)object14);
        }
        Object object15 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__3);
        if (object15 != null && object15 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__2__;
            Object object16 = m;
            Object object17 = iLookupThunk.get(object16);
            if (iLookupThunk == object17) {
                __thunk__2__ = __site__2__.fault(object16);
                object17 = __thunk__2__.get(object16);
            }
            Object object18 = v = object17;
            v = null;
            Object object19 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__3, object18, const__27);
            k = null;
            o.setNS((Collection)object19);
        }
        Object object20 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__10);
        if (object20 != null && object20 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__3__;
            Object object21 = m;
            Object object22 = iLookupThunk.get(object21);
            if (iLookupThunk == object22) {
                __thunk__3__ = __site__3__.fault(object21);
                object22 = __thunk__3__.get(object21);
            }
            Object object23 = v = object22;
            v = null;
            Object object24 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__10, object23, const__28);
            k = null;
            o.setB((ByteBuffer)object24);
        }
        Object object25 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__9);
        if (object25 != null && object25 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__4__;
            Object object26 = m;
            Object object27 = iLookupThunk.get(object26);
            if (iLookupThunk == object27) {
                __thunk__4__ = __site__4__.fault(object26);
                object27 = __thunk__4__.get(object26);
            }
            Object object28 = v = object27;
            v = null;
            Object object29 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__9, object28, const__27);
            k = null;
            o.setBS((Collection)object29);
        }
        Object object30 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__8);
        if (object30 != null && object30 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__5__;
            Object object31 = m;
            Object object32 = iLookupThunk.get(object31);
            if (iLookupThunk == object32) {
                __thunk__5__ = __site__5__.fault(object31);
                object32 = __thunk__5__.get(object31);
            }
            Object object33 = v = object32;
            v = null;
            Object object34 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__8, object33, const__26);
            k = null;
            o.setNULL((Boolean)object34);
        }
        Object object35 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__5);
        if (object35 != null && object35 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__6__;
            Object object36 = m;
            Object object37 = iLookupThunk.get(object36);
            if (iLookupThunk == object37) {
                __thunk__6__ = __site__6__.fault(object36);
                object37 = __thunk__6__.get(object36);
            }
            Object object38 = v = object37;
            v = null;
            Object object39 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__5, object38, const__29);
            k = null;
            o.setM((Map)object39);
        }
        Object object40 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__7);
        if (object40 != null && object40 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__7__;
            Object object41 = m;
            Object object42 = iLookupThunk.get(object41);
            if (iLookupThunk == object42) {
                __thunk__7__ = __site__7__.fault(object41);
                object42 = __thunk__7__.get(object41);
            }
            Object object43 = v = object42;
            v = null;
            Object object44 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__7, object43, const__27);
            k = null;
            o.setL((Collection)object44);
        }
        Object object45 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__11);
        if (object45 != null && object45 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__8__;
            Object object46 = m;
            Object object47 = iLookupThunk.get(object46);
            if (iLookupThunk == object47) {
                __thunk__8__ = __site__8__.fault(object46);
                object47 = __thunk__8__.get(object46);
            }
            Object object48 = v = object47;
            v = null;
            Object object49 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__11, object48, const__27);
            k = null;
            o.setSS((Collection)object49);
        }
        Object object50 = ((IFn)const__22.getRawRoot()).invoke(m, (Object)const__6);
        if (object50 != null && object50 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__9__;
            Object object51 = m;
            m = null;
            Object object52 = iLookupThunk.get(object51);
            if (iLookupThunk == object52) {
                __thunk__9__ = __site__9__.fault(object51);
                object52 = __thunk__9__.get(object51);
            }
            Object object53 = v = object52;
            v = null;
            Object object54 = k = ((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke((Object)o), (Object)const__6, object53, const__25);
            k = null;
            o.setS((String)object54);
        }
        Object var2_2 = null;
        return o;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb$fn__17487.invokeStatic(object3, object4);
    }
}

