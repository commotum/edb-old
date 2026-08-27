/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;
import datomic.log.LogSeek;
import org.slf4j.LoggerFactory;

public final class log$excise_root$fn__16449
extends AFunction {
    Object replacements;
    Object patch_dir;
    Object log;
    Object dir_map;
    Object lookup;
    Object cs;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public log$excise_root$fn__16449(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.replacements = object;
        this.patch_dir = object2;
        this.log = object3;
        this.dir_map = object4;
        this.lookup = object5;
        this.cs = object6;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object m, Object did) {
        v0 = did;
        did = null;
        segids = ((IFn)this.dir_map).invoke(v0);
        ((IFn)log$excise_root$fn__16449.const__0.getRawRoot()).invoke();
        v1 = this.log;
        if (Util.classOf((Object)v1) == log$excise_root$fn__16449.__cached_class__0) ** GOTO lbl11
        if (!(v1 instanceof LogSeek)) {
            v1 = v1;
            log$excise_root$fn__16449.__cached_class__0 = Util.classOf((Object)v1);
lbl11:
            // 2 sources

            v2 = (IFn)log$excise_root$fn__16449.const__2.getRawRoot();
            v3 = log$excise_root$fn__16449.__thunk__0__;
            v4 = segids;
            segids = null;
            v5 = ((IFn)log$excise_root$fn__16449.const__2.getRawRoot()).invoke(((IFn)log$excise_root$fn__16449.const__4.getRawRoot()).invoke(this.lookup, ((IFn)this.replacements).invoke(((IFn)log$excise_root$fn__16449.const__2.getRawRoot()).invoke(v4))));
            v6 = v3.get(v5);
            if (v3 == v6) {
                log$excise_root$fn__16449.__thunk__0__ = log$excise_root$fn__16449.__site__0__.fault(v5);
                v6 = log$excise_root$fn__16449.__thunk__0__.get(v5);
            }
            v7 = log$excise_root$fn__16449.const__1.getRawRoot().invoke(v1, (Object)Numbers.num((long)((IDatum)v2.invoke(v6)).getT()));
        } else {
            v8 = (LogSeek)v1;
            v9 = (IFn)log$excise_root$fn__16449.const__2.getRawRoot();
            v10 = log$excise_root$fn__16449.__thunk__0__;
            v11 = segids;
            segids = null;
            v12 = ((IFn)log$excise_root$fn__16449.const__2.getRawRoot()).invoke(((IFn)log$excise_root$fn__16449.const__4.getRawRoot()).invoke(this.lookup, ((IFn)this.replacements).invoke(((IFn)log$excise_root$fn__16449.const__2.getRawRoot()).invoke(v11))));
            v13 = v10.get(v12);
            if (v10 == v13) {
                log$excise_root$fn__16449.__thunk__0__ = log$excise_root$fn__16449.__site__0__.fault(v12);
                v13 = log$excise_root$fn__16449.__thunk__0__.get(v12);
            }
            v7 = v8.seek_seg_path(Numbers.num((long)((IDatum)v9.invoke(v13)).getT()));
        }
        vec__16450 = v7;
        dirid = RT.nth((Object)vec__16450, (int)RT.intCast((long)0L), null);
        v14 = vec__16450;
        vec__16450 = null;
        RT.nth((Object)v14, (int)RT.intCast((long)1L), null);
        dir = ((IFn)log$excise_root$fn__16449.const__4.getRawRoot()).invoke(this.lookup, dirid);
        newdid = ((IFn)log$excise_root$fn__16449.const__0.getRawRoot()).invoke();
        v15 = dir;
        dir = null;
        newdir = ((IFn)this.patch_dir).invoke(this.replacements, v15);
        logger = LoggerFactory.getLogger((String)"datomic.log");
        if (logger.isDebugEnabled()) {
            v16 = logger;
            logger = null;
            v16.debug((String)((IFn)log$excise_root$fn__16449.const__8.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{log$excise_root$fn__16449.const__9, log$excise_root$fn__16449.const__10, log$excise_root$fn__16449.const__11, dirid, log$excise_root$fn__16449.const__12, newdid})));
        }
        v17 = newdir;
        newdir = null;
        ((IFn)log$excise_root$fn__16449.const__13.getRawRoot()).invoke(this.cs, newdid, ((IFn)log$excise_root$fn__16449.const__14.getRawRoot()).invoke(v17));
        v18 = m;
        m = null;
        v19 = dirid;
        dirid = null;
        v20 = newdid;
        newdid = null;
        this = null;
        return ((IFn)log$excise_root$fn__16449.const__15.getRawRoot()).invoke(v18, v19, v20);
    }

    static {
        const__0 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__1 = RT.var((String)"datomic.log", (String)"seek-seg-path");
        const__2 = RT.var((String)"clojure.core", (String)"first");
        const__4 = RT.var((String)"datomic.common", (String)"getx");
        const__8 = RT.var((String)"datomic.slf4j", (String)"process");
        const__9 = RT.keyword(null, (String)"event");
        const__10 = RT.keyword((String)"log", (String)"excise-replace-rightmost");
        const__11 = RT.keyword(null, (String)"oldid");
        const__12 = RT.keyword(null, (String)"newid");
        const__13 = RT.var((String)"datomic.log", (String)"write-excise-val");
        const__14 = RT.var((String)"datomic.log", (String)"fressianed-dir");
        const__15 = RT.var((String)"clojure.core", (String)"assoc");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
        __thunk__0__ = __site__0__;
    }
}

