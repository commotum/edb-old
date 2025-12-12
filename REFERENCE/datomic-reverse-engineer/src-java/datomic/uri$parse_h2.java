/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.net.URI;
import java.util.regex.Pattern;

public final class uri$parse_h2
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"split");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__2 = Pattern.compile("^datomic:[^:]+://");
    public static final Object const__3 = Pattern.compile("\\?.*");
    public static final Object const__4 = Pattern.compile("/");
    public static final Object const__8 = Pattern.compile(":");
    public static final Var const__9 = RT.var((String)"datomic.uri", (String)"param-map");
    public static final Var const__10 = RT.var((String)"datomic.uri", (String)"read-port");
    public static final Keyword const__11 = RT.keyword(null, (String)"protocol");
    public static final Var const__12 = RT.var((String)"datomic.uri", (String)"storage-protocol");
    public static final Keyword const__13 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__14 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__15 = RT.keyword(null, (String)"host");
    public static final Keyword const__16 = RT.keyword(null, (String)"port");
    public static final Keyword const__17 = RT.keyword(null, (String)"password");
    public static final Keyword const__18 = RT.keyword(null, (String)"h2-port");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"password"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"h2-port"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object uri2) {
        IPersistentMap iPersistentMap;
        Object temp__5457__auto__16926;
        Object vec__16917 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(uri2, const__2, (Object)""), const__3, (Object)""), const__4);
        Object hp = RT.nth((Object)vec__16917, (int)RT.intCast((long)0L), null);
        Object object = vec__16917;
        vec__16917 = null;
        Object db_name = RT.nth((Object)object, (int)RT.intCast((long)1L), null);
        Object vec__16920 = ((IFn)const__0.getRawRoot()).invoke(hp, const__8);
        Object host = RT.nth((Object)vec__16920, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16920;
        vec__16920 = null;
        Object portstr = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object params = ((IFn)const__9.getRawRoot()).invoke((Object)new URI(new URI((String)uri2).getSchemeSpecificPart()).getQuery());
        Object object3 = portstr;
        portstr = null;
        Object object4 = temp__5457__auto__16926 = ((IFn)const__10.getRawRoot()).invoke(object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5;
            Object or__5238__auto__16925;
            Object object6;
            Object or__5238__auto__16924;
            Object object7 = temp__5457__auto__16926;
            temp__5457__auto__16926 = null;
            Object port = object7;
            Object[] objectArray = new Object[14];
            objectArray[0] = const__11;
            Object object8 = uri2;
            uri2 = null;
            objectArray[1] = ((IFn)const__12.getRawRoot()).invoke(object8);
            objectArray[2] = const__13;
            Object object9 = hp;
            hp = null;
            objectArray[3] = object9;
            objectArray[4] = const__14;
            Object object10 = db_name;
            db_name = null;
            objectArray[5] = object10;
            objectArray[6] = const__15;
            Object object11 = host;
            host = null;
            objectArray[7] = object11;
            objectArray[8] = const__16;
            objectArray[9] = port;
            objectArray[10] = const__17;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object12 = params;
            Object object13 = iLookupThunk.get(object12);
            if (iLookupThunk == object13) {
                __thunk__0__ = __site__0__.fault(object12);
                object13 = __thunk__0__.get(object12);
            }
            Object object14 = or__5238__auto__16924 = object13;
            if (object14 != null && object14 != Boolean.FALSE) {
                object6 = or__5238__auto__16924;
                or__5238__auto__16924 = null;
            } else {
                object6 = "datomic";
            }
            objectArray[11] = object6;
            objectArray[12] = const__18;
            IFn iFn = (IFn)const__10.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object15 = params;
            params = null;
            Object object16 = iLookupThunk2.get(object15);
            if (iLookupThunk2 == object16) {
                __thunk__1__ = __site__1__.fault(object15);
                object16 = __thunk__1__.get(object15);
            }
            Object object17 = or__5238__auto__16925 = iFn.invoke(object16);
            if (object17 != null && object17 != Boolean.FALSE) {
                object5 = or__5238__auto__16925;
                or__5238__auto__16925 = null;
            } else {
                Object object18 = port;
                port = null;
                object5 = Numbers.inc((Object)object18);
            }
            objectArray[13] = object5;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$parse_h2.invokeStatic(object2);
    }
}

