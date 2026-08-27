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
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class artemis_client$create_rpc_server$fn__20943$fn__20945
extends AFunction {
    Object code;
    Object handler;
    Object id;
    public static final Keyword const__0 = RT.keyword(null, (String)"failed");
    public static final Keyword const__1 = RT.keyword(null, (String)"id");
    public static final Keyword const__2 = RT.keyword(null, (String)"value");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"failed"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public artemis_client$create_rpc_server$fn__20943$fn__20945(Object object, Object object2, Object object3) {
        this.code = object;
        this.handler = object2;
        this.id = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            IPersistentMap iPersistentMap2;
            Object temp__5455__auto__20947;
            this.handler = null;
            this.code = null;
            Object handler_result = ((IFn)this.handler).invoke(this.code);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object = handler_result;
            Object object2 = iLookupThunk.get(object);
            if (iLookupThunk == object2) {
                __thunk__0__ = __site__0__.fault(object);
                object2 = __thunk__0__.get(object);
            }
            Object object3 = temp__5455__auto__20947 = object2;
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = temp__5455__auto__20947;
                temp__5455__auto__20947 = null;
                Object failed = object4;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__1;
                objectArray[1] = this.id;
                objectArray[2] = const__0;
                Object object5 = failed;
                failed = null;
                objectArray[3] = object5;
                iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                Object[] objectArray = new Object[4];
                objectArray[0] = const__1;
                objectArray[1] = this.id;
                objectArray[2] = const__2;
                Object object6 = handler_result;
                handler_result = null;
                objectArray[3] = object6;
                iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray);
            }
            iPersistentMap = iPersistentMap2;
        }
        catch (Throwable e2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.artemis-client");
            Throwable ex = e2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__3.getRawRoot()).invoke((Object)"command failed"), ex);
                Logger logger2 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__4.getRawRoot()).invoke((Object)logger2, (Object)throwable);
            }
            Object[] objectArray = new Object[4];
            objectArray[0] = const__1;
            objectArray[1] = this.id = null;
            objectArray[2] = const__0;
            Object e2 = null;
            objectArray[3] = e2.getMessage();
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

