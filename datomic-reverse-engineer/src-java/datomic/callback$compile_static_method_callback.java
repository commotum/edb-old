/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.callback$compile_static_method_callback$fn__10095;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class callback$compile_static_method_callback
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"re-matches");
    public static final Object const__1 = Pattern.compile("(.*)\\.(.*)");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"datomic.callback", (String)"has-callback-signature?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"eval");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"x__10091__auto__");
    public static final Var const__17 = RT.var((String)"datomic.slf4j", (String)"process");

    public static Object invokeStatic(Object sym) {
        Object object;
        Object temp__5455__auto__10099;
        Object object2 = temp__5455__auto__10099 = ((IFn)const__0.getRawRoot()).invoke(const__1, ((IFn)const__2.getRawRoot()).invoke(sym));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object temp__5455__auto__10098;
            Object object3 = temp__5455__auto__10099;
            temp__5455__auto__10099 = null;
            Object vec__10092 = object3;
            RT.nth((Object)vec__10092, (int)RT.intCast((long)0L), null);
            Object cname = RT.nth((Object)vec__10092, (int)RT.intCast((long)1L), null);
            Object object4 = vec__10092;
            vec__10092 = null;
            Object mname = RT.nth((Object)object4, (int)RT.intCast((long)2L), null);
            Object object5 = temp__5455__auto__10098 = ((IFn)new callback$compile_static_method_callback$fn__10095(cname)).invoke();
            if (object5 != null && object5 != Boolean.FALSE) {
                Object cls;
                Object object6 = temp__5455__auto__10098;
                temp__5455__auto__10098 = null;
                Object object7 = cls = object6;
                cls = null;
                Object object8 = ((IFn)const__7.getRawRoot()).invoke(object7, ((IFn)const__8.getRawRoot()).invoke(mname));
                if (object8 != null && object8 != Boolean.FALSE) {
                    Object object9 = cname;
                    cname = null;
                    Object object10 = mname;
                    mname = null;
                    object = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke((Object)const__13), ((IFn)const__12.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(const__15.getRawRoot(), ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke((Object)const__16))))), ((IFn)const__12.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(object9, object10)), ((IFn)const__12.getRawRoot()).invoke((Object)const__16)))))));
                } else {
                    Logger logger = LoggerFactory.getLogger((String)"datomic.callback");
                    if (logger.isWarnEnabled()) {
                        Logger logger2 = logger;
                        logger = null;
                        Object object11 = sym;
                        sym = null;
                        logger2.warn((String)((IFn)const__17.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)"Callback ", object11, (Object)"does not have required signature")));
                    }
                    object = null;
                }
            } else {
                Logger logger = LoggerFactory.getLogger((String)"datomic.callback");
                if (logger.isWarnEnabled()) {
                    Logger logger3 = logger;
                    logger = null;
                    Object object12 = cname;
                    cname = null;
                    logger3.warn((String)((IFn)const__17.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)"Callback class ", object12, (Object)" could not be loaded")));
                }
                object = null;
            }
        } else {
            Logger logger = LoggerFactory.getLogger((String)"datomic.callback");
            if (logger.isWarnEnabled()) {
                Logger logger4 = logger;
                logger = null;
                Object object13 = sym;
                sym = null;
                logger4.warn((String)((IFn)const__17.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)"Callback symbol ", object13, (Object)" does not have the required format")));
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return callback$compile_static_method_callback.invokeStatic(object2);
    }
}

