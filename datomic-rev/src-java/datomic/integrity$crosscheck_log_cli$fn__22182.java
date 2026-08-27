/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class integrity$crosscheck_log_cli$fn__22182
extends AFunction {
    Object current;
    Object nohistory;
    Object history;
    public static final Keyword const__0 = RT.keyword(null, (String)"history");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"mod");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"inc");
    public static final Object const__5 = 10000L;
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"flush");
    public static final Keyword const__8 = RT.keyword(null, (String)"current");
    public static final Object const__9 = 10000L;
    public static final Keyword const__10 = RT.keyword(null, (String)"nohistory");
    public static final Object const__11 = 10000L;
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"str");

    public integrity$crosscheck_log_cli$fn__22182(Object object, Object object2, Object object3) {
        this.current = object;
        this.nohistory = object2;
        this.history = object3;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object result2) {
        Object object = result2;
        result2 = null;
        Object G__22183 = object;
        switch (Util.hash((Object)G__22183) >> 2 & 3) {
            case 0: {
                if (G__22183 != const__0) break;
                if (!Numbers.isZero((Object)((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(this_.history, const__4.getRawRoot()), const__5))) return null;
                ((IFn)const__6.getRawRoot()).invoke((Object)"-");
                integrity$crosscheck_log_cli$fn__22182 this_ = null;
                Object object2 = ((IFn)const__7.getRawRoot()).invoke();
                return object2;
            }
            case 1: {
                if (G__22183 != const__8) break;
                if (!Numbers.isZero((Object)((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(this_.current, const__4.getRawRoot()), const__9))) return null;
                ((IFn)const__6.getRawRoot()).invoke((Object)".");
                integrity$crosscheck_log_cli$fn__22182 this_ = null;
                Object object2 = ((IFn)const__7.getRawRoot()).invoke();
                return object2;
            }
            case 3: {
                if (G__22183 != const__10) break;
                if (!Numbers.isZero((Object)((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(this_.nohistory, const__4.getRawRoot()), const__11))) return null;
                ((IFn)const__6.getRawRoot()).invoke((Object)"~");
                integrity$crosscheck_log_cli$fn__22182 this_ = null;
                Object object2 = ((IFn)const__7.getRawRoot()).invoke();
                return object2;
            }
        }
        Object object3 = G__22183;
        G__22183 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__12.getRawRoot()).invoke((Object)"No matching clause: ", object3));
    }
}

