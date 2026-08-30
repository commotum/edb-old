(do
  (clojure.core/in-ns 'datomic.rest)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.edn :as 'edn]
        ['clojure.tools.cli :as 'cli]
        ['clojure.string :as 'str]
        ['datomic.jetty :as 'jetty]
        ['datomic.peer :as 'peer]
        ['datomic.db :as 'db]
        ['datomic.query :as 'dq]
        ['datomic.error :as 'error]
        ['datomic.common :as 'common]
        ['ring.util.servlet :as 'servlet]
        ['ring.middleware.params :as 'ringp]
        ['ring.middleware.keyword-params :as 'ringkw]
        ['ring.util.response :as 'ring]
        ['net.cgrand.moustache :as 'm]
        ['liberator.core :as 'lib :refer ['defresource 'resource]]
        ['hiccup.core :as 'h :refer ['html]]
        ['hiccup.page :as 'hp]
        ['hiccup.form :as 'hf]
        ['datomic.api :as 'd])
      (clojure.core/import 'datomic.db.IDbImpl)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'javax.servlet.http.HttpServlet)
      (clojure.core/import 'javax.servlet.http.HttpServletRequest)
      (clojure.core/import 'javax.servlet.http.HttpServletResponse)
      (clojure.core/import 'org.eclipse.jetty.server.Server)
      (clojure.core/import 'org.eclipse.jetty.server.Request)
      (clojure.core/import 'org.eclipse.jetty.server.Response)
      (clojure.core/import 'org.eclipse.jetty.servlet.ServletHolder)
      (clojure.core/import 'org.eclipse.jetty.servlet.ServletContextHandler)
      (clojure.core/import 'org.eclipse.jetty.servlets.EventSourceServlet)
      (clojure.core/import 'org.eclipse.jetty.servlets.EventSource)
      (clojure.core/import 'org.eclipse.jetty.servlets.EventSource$Emitter)
      (clojure.core/import 'java.util.concurrent.BlockingQueue)
      (clojure.core/import 'java.io.InputStream)
      (clojure.core/import 'java.io.InputStreamReader)
      (clojure.core/import 'java.io.PushbackReader)))
  (when-not (.equals 'datomic.rest 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.rest))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.edn :as 'edn]
          ['clojure.tools.cli :as 'cli]
          ['clojure.string :as 'str]
          ['datomic.jetty :as 'jetty]
          ['datomic.peer :as 'peer]
          ['datomic.db :as 'db]
          ['datomic.query :as 'dq]
          ['datomic.error :as 'error]
          ['datomic.common :as 'common]
          ['ring.util.servlet :as 'servlet]
          ['ring.middleware.params :as 'ringp]
          ['ring.middleware.keyword-params :as 'ringkw]
          ['ring.util.response :as 'ring]
          ['net.cgrand.moustache :as 'm]
          ['liberator.core :as 'lib :refer ['defresource 'resource]]
          ['hiccup.core :as 'h :refer ['html]]
          ['hiccup.page :as 'hp]
          ['hiccup.form :as 'hf]
          ['datomic.api :as 'd])
        (clojure.core/import 'datomic.db.IDbImpl)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'javax.servlet.http.HttpServlet)
        (clojure.core/import 'javax.servlet.http.HttpServletRequest)
        (clojure.core/import 'javax.servlet.http.HttpServletResponse)
        (clojure.core/import 'org.eclipse.jetty.server.Server)
        (clojure.core/import 'org.eclipse.jetty.server.Request)
        (clojure.core/import 'org.eclipse.jetty.server.Response)
        (clojure.core/import 'org.eclipse.jetty.servlet.ServletHolder)
        (clojure.core/import 'org.eclipse.jetty.servlet.ServletContextHandler)
        (clojure.core/import 'org.eclipse.jetty.servlets.EventSourceServlet)
        (clojure.core/import 'org.eclipse.jetty.servlets.EventSource)
        (clojure.core/import 'org.eclipse.jetty.servlets.EventSource$Emitter)
        (clojure.core/import 'java.util.concurrent.BlockingQueue)
        (clojure.core/import 'java.io.InputStream)
        (clojure.core/import 'java.io.InputStreamReader)
        (clojure.core/import 'java.io.PushbackReader))))
  (set! *warn-on-reflection* true)
  (.setMeta (clojure.lang.RT/var "datomic.rest" "storages") {:column (int 1)})
  (let [v__6837__auto__ #'storages]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.rest" "storages") {:column (int 1)})
      (.bindRoot (clojure.lang.RT/var "datomic.rest" "storages") (atom nil))
      #'storages))
  (.setMeta (clojure.lang.RT/var "datomic.rest" "whitelist") {:column (int 1)})
  (let [v__6837__auto__ #'whitelist]
    (when-not (.hasRoot ^clojure.lang.Var v__6837__auto__)
      (.setMeta (clojure.lang.RT/var "datomic.rest" "whitelist") {:column (int 1)})
      (.bindRoot (clojure.lang.RT/var "datomic.rest" "whitelist") (atom nil))
      #'whitelist))
  (defn set-storage-map ([alias_uri_map] (reset! storages alias_uri_map)))
  (reset-meta!
    #'set-storage-map
    (assoc
      {:arglists (clojure.core/list ['alias-uri-map]), :column (int 1)}
      :name
      'set-storage-map
      :ns
      *ns*))
  (defn read-edn
    ([stm encoding]
      (edn/read
        {:readers *data-readers*}
        (java.io.PushbackReader.
          (java.io.InputStreamReader. ^java.io.InputStream stm ^java.lang.String encoding))))
    ([str] (when-not (empty? str) (edn/read-string {:readers *data-readers*} str))))
  (reset-meta!
    #'read-edn
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'str {:tag 'String})]
         [(.withMeta 'stm {:tag 'InputStream}) (.withMeta 'encoding {:tag 'String})]),
       :column (int 1)}
      :name
      'read-edn
      :ns
      *ns*))
  (defn db-uri
    ([storage dbname]
      (let [uri (get (deref storages) storage)]
        (when uri
          (let [qidx (.indexOf ^java.lang.String uri "?")]
            (if (or (= (long qidx) -1) (= (long qidx) (long (dec (count uri)))))
              (str uri dbname)
              (str
                (subs uri 0 (java.lang.Integer/valueOf (int qidx)))
                dbname
                (subs uri (java.lang.Integer/valueOf (int qidx))))))))))
  (reset-meta!
    #'db-uri
    (assoc
      {:arglists (clojure.core/list ['storage 'dbname]), :column (int 1)}
      :name
      'db-uri
      :ns
      *ns*))
  (defn conn
    ([storage dbname]
      (let [temp__5825__auto__ (db-uri storage dbname)]
        (when temp__5825__auto__ (let [uri temp__5825__auto__] (d/connect uri))))))
  (reset-meta!
    #'conn
    (assoc
      {:arglists (clojure.core/list ['storage 'dbname]), :column (int 1)}
      :name
      'conn
      :ns
      *ns*))
  (defn response
    ([data] (ring/content-type (ring/response (pr-str data)) "application/clojure;charset=UTF-8")))
  (reset-meta!
    #'response
    (assoc {:arglists (clojure.core/list ['data]), :column (int 1)} :name 'response :ns *ns*))
  (defn tst ([req] (response (:headers req))))
  (reset-meta!
    #'tst
    (assoc {:arglists (clojure.core/list ['req]), :column (int 1)} :name 'tst :ns *ns*))
  (defn windowed
    ([db p__29838]
      (let [map__29839 p__29838
            map__29839 (if (seq? map__29839)
                         (if (next map__29839)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__29839))
                           (if (seq map__29839) (first map__29839) {}))
                         map__29839)
            basis_t (get map__29839 :basis-t)
            as_of (get map__29839 :as-of)
            since (get map__29839 :since)
            history (get map__29839 :history)
            db (if (or as_of basis_t) (d/as-of db (or as_of basis_t)) db)
            db (if since (d/since db since) db)
            db (if history (d/history db) db)]
        db)))
  (reset-meta!
    #'windowed
    (assoc
      {:arglists (clojure.core/list ['db {:keys ['basis-t 'as-of 'since 'history]}]),
       :column (int 1)}
      :name
      'windowed
      :ns
      *ns*))
  (defn limited
    ([data p__29843]
      (let [map__29844 p__29843
            map__29844 (if (seq? map__29844)
                         (if (next map__29844)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__29844))
                           (if (seq map__29844) (first map__29844) {}))
                         map__29844)
            offset (get map__29844 :offset)
            limit (get map__29844 :limit)
            data (if offset (drop offset data) data)]
        (if limit (take limit data) data))))
  (reset-meta!
    #'limited
    (assoc
      {:arglists (clojure.core/list ['data {:keys ['offset 'limit]}]), :column (int 1)}
      :name
      'limited
      :ns
      *ns*))
  (defn datom->map ([d] (array-map :e (:e d) :a (:a d) :v (:v d) :tx (:tx d) :added (:added d))))
  (reset-meta!
    #'datom->map
    (assoc {:arglists (clojure.core/list ['d]), :column (int 1)} :name 'datom->map :ns *ns*))
  (defn prep-tx-ret
    ([tx_ret storage dbname]
      (let [db_base #:db{:alias (str storage "/" dbname)}]
        {:db-before (assoc db_base :basis-t (d/basis-t (:db-before tx_ret))),
         :db-after (assoc db_base :basis-t (d/basis-t (:db-after tx_ret))),
         :tx-data (mapv datom->map (:tx-data tx_ret)),
         :tempids (:tempids tx_ret)})))
  (reset-meta!
    #'prep-tx-ret
    (assoc
      {:arglists (clojure.core/list ['tx-ret 'storage 'dbname]), :column (int 1)}
      :name
      'prep-tx-ret
      :ns
      *ns*))
  (defn wrap-reading-params
    ([handler]
      (fn fn__29850
        ([p__29849]
          (let [map__29851 p__29849
                map__29851 (if (seq? map__29851)
                             (if (next map__29851)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__29851))
                               (if (seq map__29851) (first map__29851) {}))
                             map__29851)
                req map__29851
                content_type (get map__29851 :content-type)
                content_length (get map__29851 :content-length)]
            (^clojure.lang.IFn handler
              (if (or (not= content_type "application/edn") (zero? (or content_length 0)))
                (update-in
                  req
                  [:params]
                  (fn fn__29852
                    ([p1__29848#]
                      (reduce
                        (fn fn__29854
                          ([m p__29853]
                            (let [vec__29855 p__29853
                                  k (nth vec__29855 (int 0) nil)
                                  v (nth vec__29855 (int 1) nil)]
                              (try
                                (assoc m k (read-edn v))
                                (catch
                                  java.lang.Throwable
                                  t
                                  (error/raise
                                    :rest/invalid-params
                                    (str "Unable to read parameter " k)
                                    {:params p1__29848#}
                                    t))))))
                        {}
                        p1__29848#))))
                req)))))))
  (reset-meta!
    #'wrap-reading-params
    (assoc
      {:arglists (clojure.core/list ['handler]), :column (int 1)}
      :name
      'wrap-reading-params
      :ns
      *ns*))
  (defn matches-origin-whitelist?
    ([whitelist origin]
      (when-not origin
        (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'origin)))))
      (cond
        (= origin "null") false
        (contains? whitelist "*") true
        :default (do (contains? whitelist origin)))))
  (reset-meta!
    #'matches-origin-whitelist?
    (assoc
      {:arglists (clojure.core/list ['whitelist 'origin]), :column (int 1)}
      :name
      'matches-origin-whitelist?
      :ns
      *ns*))
  (defn wrap-cors-preflight
    ([handler]
      (fn fn__29865
        ([req]
          (if (= :options (:request-method req))
            (let [origin (get-in req [:headers "origin"])]
              {:status 204,
               :headers
               (when (matches-origin-whitelist? (deref whitelist) origin)
                 {"Access-Control-Allow-Origin" origin,
                  "Access-Control-Allow-Headers" "X-Requested-With"})})
            (^clojure.lang.IFn handler req))))))
  (reset-meta!
    #'wrap-cors-preflight
    (assoc
      {:arglists (clojure.core/list ['handler]), :column (int 1)}
      :name
      'wrap-cors-preflight
      :ns
      *ns*))
  (defn wrap-cors-request
    ([handler]
      (fn fn__29868
        ([req]
          (let [resp (^clojure.lang.IFn handler req)
                temp__5823__auto__ (get-in req [:headers "origin"])]
            (if temp__5823__auto__
              (let [origin temp__5823__auto__]
                (if (matches-origin-whitelist? (deref whitelist) origin)
                  (assoc-in resp [:headers "Access-Control-Allow-Origin"] origin)
                  resp))
              resp))))))
  (reset-meta!
    #'wrap-cors-request
    (assoc
      {:arglists (clojure.core/list ['handler]), :column (int 1)}
      :name
      'wrap-cors-request
      :ns
      *ns*))
  (def html-media ["text/html;q=0.9" "application/xhtml+xml;q=0.8"])
  (reset-meta! #'html-media (assoc {:column (int 1)} :name 'html-media :ns *ns*))
  (def edn-media ["application/edn"])
  (reset-meta! #'edn-media (assoc {:column (int 1)} :name 'edn-media :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.rest" "edn-or-html-media") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.rest" "edn-or-html-media")
    (concat edn-media html-media))
  (defn html5
    ([& forms]
      (let [options__29171__auto__ {}]
        (binding [hiccup.compiler/*html-mode* :html]
          (str
            (#'hiccup.compiler/render-html (hp/doctype :html5))
            "<html"
            (#'hiccup.compiler/render-attr-map
              {:id nil, :class nil, :lang (^clojure.lang.IFn options__29171__auto__ :lang)})
            ">"
            (let [attrs29872 (hp/include-css "/css/bootstrap.min.css")]
              (if (map? attrs29872)
                (str
                  "<head"
                  (#'hiccup.compiler/render-attr-map (merge {:id nil, :class nil} attrs29872))
                  ">"
                  "</head>")
                (str "<head>" (#'hiccup.compiler/render-html attrs29872) "</head>")))
            (#'hiccup.compiler/render-html (conj [:body] (into [:div {:class "container"}] forms)))
            "</html>")))))
  (reset-meta!
    #'html5
    (assoc {:arglists (clojure.core/list ['& 'forms]), :column (int 1)} :name 'html5 :ns *ns*))
  (def index
   (fn index
     ([request__29717__auto__]
       (lib/run-resource
         request__29717__auto__
         (lib/get-options
           (clojure.core/list
             :available-media-types
             html-media
             :handle-ok
             (html5
               [:h2 "Datomic REST Service"]
               [:p
                "The Datomic REST Service provides 2 categories of resources - those based on particular data storages and databases (data),\nand resources that represent activities that might cross data sources, e.g. query (api)."]
               [:p
                "Note that this web app "
                [:em "is"]
                " the service. It is not an app built on the service, nor a set of documentation pages about the service. The URIs, query params, and POST data are the same ones you will use when accessing the service programmatically."]
               [:p
                "The embedded documentation is designed to assist you in using the service API, but is not a reference nor tutorial for Datomic itself. Please consult the "
                [:a {:rel "help", :href "https://docs.datomic.com/"} "Datomic documentation"]
                "."]
               [:hr]
               [:ul
                [:li [:a {:rel "item", :href "data/"} "Data"]]
                [:li [:a {:rel "item", :href "api/"} "API"]]])))))))
  (reset-meta! #'index (assoc {:column (int 1)} :name 'index :ns *ns*))
  (def api
   (fn api
     ([request__29717__auto__]
       (lib/run-resource
         request__29717__auto__
         (lib/get-options
           (clojure.core/list
             :available-media-types
             html-media
             :handle-ok
             (html5
               [:h3 "GET " [:a {:rel "up", :href ".."} "/"] "api/"]
               [:span "Currently the only API is query."]
               [:hr]
               [:ul [:li [:a {:rel "item", :href "query"} "Query"]]])))))))
  (reset-meta! #'api (assoc {:column (int 1)} :name 'api :ns *ns*))
  (defn request-method-in
    ([method_set]
      (fn fn__29878
        ([p1__29877#] (contains? method_set (:request-method (:request p1__29877#)))))))
  (reset-meta!
    #'request-method-in
    (assoc
      {:arglists (clojure.core/list ['method-set]), :column (int 1)}
      :name
      'request-method-in
      :ns
      *ns*))
  (def stores
   (fn stores
     ([request__29717__auto__]
       (lib/run-resource
         request__29717__auto__
         (lib/get-options
           (clojure.core/list
             :available-media-types
             edn-or-html-media
             :method-allowed?
             (request-method-in #{:get :head :options})
             :handle-ok
             (let [stores (keys (deref storages))]
               (fn fn__29881
                 ([context]
                   (let [edn_ret (vec stores)
                         G__29882 (get-in context [:representation :media-type])]
                     (case
                       G__29882
                       "application/xhtml+xml"
                       (html5
                         [:h3 "GET " [:a {:rel "up", :href ".."} "/"] "data/"]
                         [:h4 "storages"]
                         [:p
                          "This service was started with the following storages. Note that the storage names used here and elsewhere\nin the API are aliases for storages known only to the service."]
                         [:p
                          "You are seeing them this way now because you\n (or your browser) requested the "
                          [:a {:href "http://www.rfc-editor.org/rfc/rfc2854.txt"} "text/html"]
                          " media type. The "
                          [:a {:href "http://edn-format.org"} "application/edn"]
                          " media type is also supported throughout the API, and is the preferred format for programmatic use. You can choose your preferred media type using an Accept header."]
                         [:hr]
                         [:ul
                          {:id "storages"}
                          (let [iter__6398__auto__ (fn iter__29883
                                                     ([s__29884]
                                                       (lazy-seq
                                                         (let [s__29884 s__29884
                                                               temp__5825__auto__ (seq s__29884)]
                                                           (when
                                                             temp__5825__auto__
                                                             (let
                                                               [s__29884 temp__5825__auto__]
                                                               (if
                                                                 (chunked-seq? s__29884)
                                                                 (let
                                                                   [c__6396__auto__
                                                                    (chunk-first s__29884)
                                                                    size__6397__auto__
                                                                    (int (count c__6396__auto__))
                                                                    b__29886
                                                                    (chunk-buffer
                                                                      (java.lang.Integer/valueOf
                                                                        (int size__6397__auto__)))]
                                                                   (if
                                                                     (loop
                                                                       [i__29885 (int 0)]
                                                                       (if
                                                                         (<
                                                                           i__29885
                                                                           size__6397__auto__)
                                                                         (let
                                                                           [s
                                                                            (.nth
                                                                              ^clojure.lang.Indexed c__6396__auto__
                                                                              (int i__29885))]
                                                                           (chunk-append
                                                                             b__29886
                                                                             [:li
                                                                              [:a
                                                                               {:rel "storage",
                                                                                :href (str s "/")}
                                                                               s]])
                                                                           (recur (inc i__29885)))
                                                                         true))
                                                                     (chunk-cons
                                                                       (chunk b__29886)
                                                                       (^clojure.lang.IFn iter__29883
                                                                         (chunk-rest s__29884)))
                                                                     (chunk-cons
                                                                       (chunk b__29886)
                                                                       nil)))
                                                                 (let
                                                                   [s (first s__29884)]
                                                                   (cons
                                                                     [:li
                                                                      [:a
                                                                       {:rel "storage",
                                                                        :href (str s "/")}
                                                                       s]]
                                                                     (^clojure.lang.IFn iter__29883
                                                                       (rest s__29884)))))))))))]
                            (^clojure.lang.IFn iter__6398__auto__ stores))]
                         [:hr]
                         [:h4 "application/edn"]
                         [:code (pr-str edn_ret)])
                       "text/html"
                       (html5
                         [:h3 "GET " [:a {:rel "up", :href ".."} "/"] "data/"]
                         [:h4 "storages"]
                         [:p
                          "This service was started with the following storages. Note that the storage names used here and elsewhere\nin the API are aliases for storages known only to the service."]
                         [:p
                          "You are seeing them this way now because you\n (or your browser) requested the "
                          [:a {:href "http://www.rfc-editor.org/rfc/rfc2854.txt"} "text/html"]
                          " media type. The "
                          [:a {:href "http://edn-format.org"} "application/edn"]
                          " media type is also supported throughout the API, and is the preferred format for programmatic use. You can choose your preferred media type using an Accept header."]
                         [:hr]
                         [:ul
                          {:id "storages"}
                          (let [iter__6398__auto__ (fn iter__29896
                                                     ([s__29897]
                                                       (lazy-seq
                                                         (let [s__29897 s__29897
                                                               temp__5825__auto__ (seq s__29897)]
                                                           (when
                                                             temp__5825__auto__
                                                             (let
                                                               [s__29897 temp__5825__auto__]
                                                               (if
                                                                 (chunked-seq? s__29897)
                                                                 (let
                                                                   [c__6396__auto__
                                                                    (chunk-first s__29897)
                                                                    size__6397__auto__
                                                                    (int (count c__6396__auto__))
                                                                    b__29899
                                                                    (chunk-buffer
                                                                      (java.lang.Integer/valueOf
                                                                        (int size__6397__auto__)))]
                                                                   (if
                                                                     (loop
                                                                       [i__29898 (int 0)]
                                                                       (if
                                                                         (<
                                                                           i__29898
                                                                           size__6397__auto__)
                                                                         (let
                                                                           [s
                                                                            (.nth
                                                                              ^clojure.lang.Indexed c__6396__auto__
                                                                              (int i__29898))]
                                                                           (chunk-append
                                                                             b__29899
                                                                             [:li
                                                                              [:a
                                                                               {:rel "storage",
                                                                                :href (str s "/")}
                                                                               s]])
                                                                           (recur (inc i__29898)))
                                                                         true))
                                                                     (chunk-cons
                                                                       (chunk b__29899)
                                                                       (^clojure.lang.IFn iter__29896
                                                                         (chunk-rest s__29897)))
                                                                     (chunk-cons
                                                                       (chunk b__29899)
                                                                       nil)))
                                                                 (let
                                                                   [s (first s__29897)]
                                                                   (cons
                                                                     [:li
                                                                      [:a
                                                                       {:rel "storage",
                                                                        :href (str s "/")}
                                                                       s]]
                                                                     (^clojure.lang.IFn iter__29896
                                                                       (rest s__29897)))))))))))]
                            (^clojure.lang.IFn iter__6398__auto__ stores))]
                         [:hr]
                         [:h4 "application/edn"]
                         [:code (pr-str edn_ret)])
                       "application/edn"
                       (pr-str edn_ret))))))))))))
  (reset-meta! #'stores (assoc {:column (int 1)} :name 'stores :ns *ns*))
  (defn catalog
    ([storage]
      (if (contains? (deref storages) storage)
        (let [uri (get (deref storages) storage)
              catalog (set (map name (peer/get-catalog uri)))
              db_name "db-name"
              created (atom false)
              handle (fn handle
                       ([context]
                         (let [uri (get (deref storages) storage)
                               catalog (into (sorted-set) (map name (peer/get-catalog uri)))
                               edn_ret (vec catalog)
                               G__29914 (get-in context [:representation :media-type])]
                           (case
                             G__29914
                             "application/xhtml+xml"
                             (html5
                               [:h3
                                "GET | POST "
                                [:a {:rel "up", :href "../.."} "/"]
                                [:a {:rel "up", :href ".."} "data" "/"]
                                storage
                                "/"]
                               [:h4 "databases"]
                               [:p "This is a catalog of the databases in this storage."]
                               [:p
                                "If you don't see any databases below, you can create one using the form."]
                               [:hr]
                               [:ul
                                {:id "databases"}
                                (let [iter__6398__auto__ (fn iter__29915
                                                           ([s__29916]
                                                             (lazy-seq
                                                               (let
                                                                 [s__29916 s__29916
                                                                  temp__5825__auto__
                                                                  (seq s__29916)]
                                                                 (when
                                                                   temp__5825__auto__
                                                                   (let
                                                                     [s__29916 temp__5825__auto__]
                                                                     (if
                                                                       (chunked-seq? s__29916)
                                                                       (let
                                                                         [c__6396__auto__
                                                                          (chunk-first s__29916)
                                                                          size__6397__auto__
                                                                          (int
                                                                            (count
                                                                              c__6396__auto__))
                                                                          b__29918
                                                                          (chunk-buffer
                                                                            (java.lang.Integer/valueOf
                                                                              (int
                                                                                size__6397__auto__)))]
                                                                         (if
                                                                           (loop
                                                                             [i__29917 (int 0)]
                                                                             (if
                                                                               (<
                                                                                 i__29917
                                                                                 size__6397__auto__)
                                                                               (let
                                                                                 [db
                                                                                  (.nth
                                                                                    ^clojure.lang.Indexed c__6396__auto__
                                                                                    (int
                                                                                      i__29917))]
                                                                                 (chunk-append
                                                                                   b__29918
                                                                                   [:li
                                                                                    [:a
                                                                                     {:rel "item",
                                                                                      :href
                                                                                      (str
                                                                                        db
                                                                                        "/-/")}
                                                                                     db]])
                                                                                 (recur
                                                                                   (inc i__29917)))
                                                                               true))
                                                                           (chunk-cons
                                                                             (chunk b__29918)
                                                                             (^clojure.lang.IFn iter__29915
                                                                               (chunk-rest
                                                                                 s__29916)))
                                                                           (chunk-cons
                                                                             (chunk b__29918)
                                                                             nil)))
                                                                       (let
                                                                         [db (first s__29916)]
                                                                         (cons
                                                                           [:li
                                                                            [:a
                                                                             {:rel "item",
                                                                              :href (str db "/-/")}
                                                                             db]]
                                                                           (^clojure.lang.IFn iter__29915
                                                                             (rest
                                                                               s__29916)))))))))))]
                                  (^clojure.lang.IFn iter__6398__auto__ catalog))]
                               [:hr]
                               [:h4 "application/edn"]
                               [:code (pr-str edn_ret)]
                               [:hr]
                               [:h3 "Create database:"]
                               (hf/form-to
                                 [:post ""]
                                 "db-name *: "
                                 (hf/text-field {:required "required"} db_name)
                                 [:br]
                                 (hf/submit-button "Create database"))
                               [:p
                                "The db-name should be suitable for use as a URI path subcomponent. It must be a valid "
                                [:a {:href "http://edn-format.org"} "edn"]
                                " symbol or string."]
                               [:p
                                "The above form POSTs to: /data/"
                                storage
                                "/ .\nThe form also illustrates how one would call the API via a POST. When you send form data in the body it should be in the standard "
                                [:a
                                 {:href
                                  "http://www.w3.org/MarkUp/html-spec/html-spec_8.html#SEC8.2.1"}
                                 "application/x-www-form-urlencoded"]
                                " media type. The API will return 201 if a new database was created, 200 if it already existed."])
                             "text/html"
                             (html5
                               [:h3
                                "GET | POST "
                                [:a {:rel "up", :href "../.."} "/"]
                                [:a {:rel "up", :href ".."} "data" "/"]
                                storage
                                "/"]
                               [:h4 "databases"]
                               [:p "This is a catalog of the databases in this storage."]
                               [:p
                                "If you don't see any databases below, you can create one using the form."]
                               [:hr]
                               [:ul
                                {:id "databases"}
                                (let [iter__6398__auto__ (fn iter__29928
                                                           ([s__29929]
                                                             (lazy-seq
                                                               (let
                                                                 [s__29929 s__29929
                                                                  temp__5825__auto__
                                                                  (seq s__29929)]
                                                                 (when
                                                                   temp__5825__auto__
                                                                   (let
                                                                     [s__29929 temp__5825__auto__]
                                                                     (if
                                                                       (chunked-seq? s__29929)
                                                                       (let
                                                                         [c__6396__auto__
                                                                          (chunk-first s__29929)
                                                                          size__6397__auto__
                                                                          (int
                                                                            (count
                                                                              c__6396__auto__))
                                                                          b__29931
                                                                          (chunk-buffer
                                                                            (java.lang.Integer/valueOf
                                                                              (int
                                                                                size__6397__auto__)))]
                                                                         (if
                                                                           (loop
                                                                             [i__29930 (int 0)]
                                                                             (if
                                                                               (<
                                                                                 i__29930
                                                                                 size__6397__auto__)
                                                                               (let
                                                                                 [db
                                                                                  (.nth
                                                                                    ^clojure.lang.Indexed c__6396__auto__
                                                                                    (int
                                                                                      i__29930))]
                                                                                 (chunk-append
                                                                                   b__29931
                                                                                   [:li
                                                                                    [:a
                                                                                     {:rel "item",
                                                                                      :href
                                                                                      (str
                                                                                        db
                                                                                        "/-/")}
                                                                                     db]])
                                                                                 (recur
                                                                                   (inc i__29930)))
                                                                               true))
                                                                           (chunk-cons
                                                                             (chunk b__29931)
                                                                             (^clojure.lang.IFn iter__29928
                                                                               (chunk-rest
                                                                                 s__29929)))
                                                                           (chunk-cons
                                                                             (chunk b__29931)
                                                                             nil)))
                                                                       (let
                                                                         [db (first s__29929)]
                                                                         (cons
                                                                           [:li
                                                                            [:a
                                                                             {:rel "item",
                                                                              :href (str db "/-/")}
                                                                             db]]
                                                                           (^clojure.lang.IFn iter__29928
                                                                             (rest
                                                                               s__29929)))))))))))]
                                  (^clojure.lang.IFn iter__6398__auto__ catalog))]
                               [:hr]
                               [:h4 "application/edn"]
                               [:code (pr-str edn_ret)]
                               [:hr]
                               [:h3 "Create database:"]
                               (hf/form-to
                                 [:post ""]
                                 "db-name *: "
                                 (hf/text-field {:required "required"} db_name)
                                 [:br]
                                 (hf/submit-button "Create database"))
                               [:p
                                "The db-name should be suitable for use as a URI path subcomponent. It must be a valid "
                                [:a {:href "http://edn-format.org"} "edn"]
                                " symbol or string."]
                               [:p
                                "The above form POSTs to: /data/"
                                storage
                                "/ .\nThe form also illustrates how one would call the API via a POST. When you send form data in the body it should be in the standard "
                                [:a
                                 {:href
                                  "http://www.w3.org/MarkUp/html-spec/html-spec_8.html#SEC8.2.1"}
                                 "application/x-www-form-urlencoded"]
                                " media type. The API will return 201 if a new database was created, 200 if it already existed."])
                             "application/edn"
                             (pr-str edn_ret)))))]
          (lib/resource
            :available-media-types
            edn-or-html-media
            :method-allowed?
            (request-method-in #{:get :head :post :options})
            :post!
            (fn fn__29944
              ([ctx]
                (let [dbn (get-in ctx [:request :params (keyword db_name)])
                      uri (db-uri storage dbn)]
                  (swap! created (fn fn__29945 ([_] (d/create-database uri)))))))
            :new?
            (fn fn__29948 ([_] (boolean (deref created))))
            :respond-with-entity?
            true
            :malformed?
            (fn fn__29950
              ([ctx]
                (and
                  ((request-method-in #{:post}) ctx)
                  (not (get-in ctx [:request :params (keyword db_name)])))))
            :handle-created
            handle
            :handle-ok
            handle))
        (lib/resource :available-media-types edn-or-html-media :exists? false))))
  (reset-meta!
    #'catalog
    (assoc {:arglists (clojure.core/list ['storage]), :column (int 1)} :name 'catalog :ns *ns*))
  (defn datoms-table
    ([id datoms ecell]
      [:table
       {:id id, :class "table"}
       [:tr [:th "e"] [:th "a"] [:th "v"] [:th "tx"] [:th "added"]]
       (let [iter__6398__auto__ (fn iter__29954
                                  ([s__29955]
                                    (lazy-seq
                                      (let [s__29955 s__29955 temp__5825__auto__ (seq s__29955)]
                                        (when temp__5825__auto__
                                          (let [s__29955 temp__5825__auto__]
                                            (if (chunked-seq? s__29955)
                                              (let [c__6396__auto__ (chunk-first s__29955)
                                                    size__6397__auto__ (int
                                                                         (count c__6396__auto__))
                                                    b__29957 (chunk-buffer
                                                               (java.lang.Integer/valueOf
                                                                 (int size__6397__auto__)))]
                                                (if (loop [i__29956 (int 0)]
                                                      (if (< i__29956 size__6397__auto__)
                                                        (let [map__29961
                                                              (.nth
                                                                ^clojure.lang.Indexed c__6396__auto__
                                                                (int i__29956))
                                                              map__29961
                                                              (if
                                                                (seq? map__29961)
                                                                (if
                                                                  (next map__29961)
                                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                    (to-array map__29961))
                                                                  (if
                                                                    (seq map__29961)
                                                                    (first map__29961)
                                                                    {}))
                                                                map__29961)
                                                              e (get map__29961 :e)
                                                              a (get map__29961 :a)
                                                              v (get map__29961 :v)
                                                              tx (get map__29961 :tx)
                                                              added (get map__29961 :added)]
                                                          (chunk-append
                                                            b__29957
                                                            [:tr
                                                             (^clojure.lang.IFn ecell e)
                                                             (^clojure.lang.IFn ecell a)
                                                             [:td (pr-str v)]
                                                             (^clojure.lang.IFn ecell tx)
                                                             [:td (str added)]])
                                                          (recur (inc i__29956)))
                                                        true))
                                                  (chunk-cons
                                                    (chunk b__29957)
                                                    (^clojure.lang.IFn iter__29954
                                                      (chunk-rest s__29955)))
                                                  (chunk-cons (chunk b__29957) nil)))
                                              (let [map__29963 (first s__29955)
                                                    map__29963 (if
                                                                 (seq? map__29963)
                                                                 (if
                                                                   (next map__29963)
                                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                     (to-array map__29963))
                                                                   (if
                                                                     (seq map__29963)
                                                                     (first map__29963)
                                                                     {}))
                                                                 map__29963)
                                                    e (get map__29963 :e)
                                                    a (get map__29963 :a)
                                                    v (get map__29963 :v)
                                                    tx (get map__29963 :tx)
                                                    added (get map__29963 :added)]
                                                (cons
                                                  [:tr
                                                   (^clojure.lang.IFn ecell e)
                                                   (^clojure.lang.IFn ecell a)
                                                   [:td (pr-str v)]
                                                   (^clojure.lang.IFn ecell tx)
                                                   [:td (str added)]]
                                                  (^clojure.lang.IFn iter__29954
                                                    (rest s__29955)))))))))))]
         (^clojure.lang.IFn iter__6398__auto__ datoms))]))
  (reset-meta!
    #'datoms-table
    (assoc
      {:arglists (clojure.core/list ['id 'datoms 'ecell]), :column (int 1)}
      :name
      'datoms-table
      :ns
      *ns*))
  (defn db-transact
    ([storage dbname]
      (let [temp__5823__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5823__auto__
          (let [c temp__5823__auto__ created (atom nil)]
            (lib/resource
              :available-media-types
              edn-or-html-media
              :method-allowed?
              (request-method-in #{:post :options})
              :post!
              (fn fn__29973
                ([ctx]
                  (let [txd (get-in ctx [:request :params :tx-data])
                        tx_ret (deref (d/transact c txd))]
                    (swap! created (fn fn__29974 ([_] (prep-tx-ret tx_ret storage dbname)))))))
              :new?
              (fn fn__29977 ([_] (boolean (deref created))))
              :respond-with-entity?
              true
              :malformed?
              (fn fn__29979
                ([ctx]
                  (and
                    ((request-method-in #{:post}) ctx)
                    (not (get-in ctx [:request :params :tx-data])))))
              :handle-created
              (fn fn__29982
                ([context]
                  (let [edn_ret (deref created)
                        G__29983 (get-in context [:representation :media-type])]
                    (case
                      G__29983
                      "application/xhtml+xml"
                      (let [tx_ret edn_ret
                            db (d/db c)
                            t (:basis-t (:db-after tx_ret))
                            ecell (fn ecell
                                    ([eid]
                                      [:td
                                       [:a
                                        {:rel "entity", :href (str t "/entity?e=" eid)}
                                        (pr-str (or (d/ident db eid) eid))]]))]
                        (html5
                          [:h3
                           "POST "
                           [:a {:rel "up", :href "../../.."} "/"]
                           [:a {:rel "up", :href "../.."} "data" "/"]
                           [:a {:rel "up", :href ".."} storage "/"]
                           [:a
                            {:rel "related", :href (str (:basis-t (:db-after tx_ret)) "/")}
                            dbname]
                           "/"]
                          [:h4 "transaction results"]
                          [:a
                           {:rel "db-before", :href (str (:basis-t (:db-before tx_ret)) "/")}
                           "db-before"]
                          [:span
                           " - A link to the db at the point immediately preceding the transaction."]
                          [:br]
                          [:a
                           {:rel "db-after", :href (str (:basis-t (:db-after tx_ret)) "/")}
                           "db-after"]
                          [:span
                           " - A link to the db at the point immediately following (including) the transaction."]
                          [:br]
                          [:h4 "tempids"]
                          [:p "tempids and the entities they became."]
                          [:table
                           {:id "tempids", :class "table"}
                           [:tr [:th "tempid"] [:th "entid"]]
                           (let [iter__6398__auto__ (fn iter__29987
                                                      ([s__29988]
                                                        (lazy-seq
                                                          (let [s__29988 s__29988
                                                                temp__5825__auto__ (seq s__29988)]
                                                            (when
                                                              temp__5825__auto__
                                                              (let
                                                                [s__29988 temp__5825__auto__]
                                                                (if
                                                                  (chunked-seq? s__29988)
                                                                  (let
                                                                    [c__6396__auto__
                                                                     (chunk-first s__29988)
                                                                     size__6397__auto__
                                                                     (int (count c__6396__auto__))
                                                                     b__29990
                                                                     (chunk-buffer
                                                                       (java.lang.Integer/valueOf
                                                                         (int
                                                                           size__6397__auto__)))]
                                                                    (if
                                                                      (loop
                                                                        [i__29989 (int 0)]
                                                                        (if
                                                                          (<
                                                                            i__29989
                                                                            size__6397__auto__)
                                                                          (let
                                                                            [vec__29994
                                                                             (.nth
                                                                               ^clojure.lang.Indexed c__6396__auto__
                                                                               (int i__29989))
                                                                             tid
                                                                             (nth
                                                                               vec__29994
                                                                               (int 0)
                                                                               nil)
                                                                             eid
                                                                             (nth
                                                                               vec__29994
                                                                               (int 1)
                                                                               nil)]
                                                                            (chunk-append
                                                                              b__29990
                                                                              [:tr
                                                                               [:td (pr-str tid)]
                                                                               (^clojure.lang.IFn ecell
                                                                                 eid)])
                                                                            (recur (inc i__29989)))
                                                                          true))
                                                                      (chunk-cons
                                                                        (chunk b__29990)
                                                                        (^clojure.lang.IFn iter__29987
                                                                          (chunk-rest s__29988)))
                                                                      (chunk-cons
                                                                        (chunk b__29990)
                                                                        nil)))
                                                                  (let
                                                                    [vec__29998 (first s__29988)
                                                                     tid
                                                                     (nth vec__29998 (int 0) nil)
                                                                     eid
                                                                     (nth vec__29998 (int 1) nil)]
                                                                    (cons
                                                                      [:tr
                                                                       [:td (pr-str tid)]
                                                                       (^clojure.lang.IFn ecell
                                                                         eid)]
                                                                      (^clojure.lang.IFn iter__29987
                                                                        (rest s__29988)))))))))))]
                             (^clojure.lang.IFn iter__6398__auto__ (:tempids tx_ret)))]
                          [:h4 "txdata"]
                          [:p "The datoms created by the transaction"]
                          (datoms-table "txdata" (:tx-data tx_ret) ecell)
                          [:hr]
                          [:h4 "application/edn"]
                          [:code (pr-str edn_ret)]))
                      "text/html"
                      (let [tx_ret edn_ret
                            db (d/db c)
                            t (:basis-t (:db-after tx_ret))
                            ecell (fn ecell
                                    ([eid]
                                      [:td
                                       [:a
                                        {:rel "entity", :href (str t "/entity?e=" eid)}
                                        (pr-str (or (d/ident db eid) eid))]]))]
                        (html5
                          [:h3
                           "POST "
                           [:a {:rel "up", :href "../../.."} "/"]
                           [:a {:rel "up", :href "../.."} "data" "/"]
                           [:a {:rel "up", :href ".."} storage "/"]
                           [:a
                            {:rel "related", :href (str (:basis-t (:db-after tx_ret)) "/")}
                            dbname]
                           "/"]
                          [:h4 "transaction results"]
                          [:a
                           {:rel "db-before", :href (str (:basis-t (:db-before tx_ret)) "/")}
                           "db-before"]
                          [:span
                           " - A link to the db at the point immediately preceding the transaction."]
                          [:br]
                          [:a
                           {:rel "db-after", :href (str (:basis-t (:db-after tx_ret)) "/")}
                           "db-after"]
                          [:span
                           " - A link to the db at the point immediately following (including) the transaction."]
                          [:br]
                          [:h4 "tempids"]
                          [:p "tempids and the entities they became."]
                          [:table
                           {:id "tempids", :class "table"}
                           [:tr [:th "tempid"] [:th "entid"]]
                           (let [iter__6398__auto__ (fn iter__30009
                                                      ([s__30010]
                                                        (lazy-seq
                                                          (let [s__30010 s__30010
                                                                temp__5825__auto__ (seq s__30010)]
                                                            (when
                                                              temp__5825__auto__
                                                              (let
                                                                [s__30010 temp__5825__auto__]
                                                                (if
                                                                  (chunked-seq? s__30010)
                                                                  (let
                                                                    [c__6396__auto__
                                                                     (chunk-first s__30010)
                                                                     size__6397__auto__
                                                                     (int (count c__6396__auto__))
                                                                     b__30012
                                                                     (chunk-buffer
                                                                       (java.lang.Integer/valueOf
                                                                         (int
                                                                           size__6397__auto__)))]
                                                                    (if
                                                                      (loop
                                                                        [i__30011 (int 0)]
                                                                        (if
                                                                          (<
                                                                            i__30011
                                                                            size__6397__auto__)
                                                                          (let
                                                                            [vec__30016
                                                                             (.nth
                                                                               ^clojure.lang.Indexed c__6396__auto__
                                                                               (int i__30011))
                                                                             tid
                                                                             (nth
                                                                               vec__30016
                                                                               (int 0)
                                                                               nil)
                                                                             eid
                                                                             (nth
                                                                               vec__30016
                                                                               (int 1)
                                                                               nil)]
                                                                            (chunk-append
                                                                              b__30012
                                                                              [:tr
                                                                               [:td (pr-str tid)]
                                                                               (^clojure.lang.IFn ecell
                                                                                 eid)])
                                                                            (recur (inc i__30011)))
                                                                          true))
                                                                      (chunk-cons
                                                                        (chunk b__30012)
                                                                        (^clojure.lang.IFn iter__30009
                                                                          (chunk-rest s__30010)))
                                                                      (chunk-cons
                                                                        (chunk b__30012)
                                                                        nil)))
                                                                  (let
                                                                    [vec__30020 (first s__30010)
                                                                     tid
                                                                     (nth vec__30020 (int 0) nil)
                                                                     eid
                                                                     (nth vec__30020 (int 1) nil)]
                                                                    (cons
                                                                      [:tr
                                                                       [:td (pr-str tid)]
                                                                       (^clojure.lang.IFn ecell
                                                                         eid)]
                                                                      (^clojure.lang.IFn iter__30009
                                                                        (rest s__30010)))))))))))]
                             (^clojure.lang.IFn iter__6398__auto__ (:tempids tx_ret)))]
                          [:h4 "txdata"]
                          [:p "The datoms created by the transaction"]
                          (datoms-table "txdata" (:tx-data tx_ret) ecell)
                          [:hr]
                          [:h4 "application/edn"]
                          [:code (pr-str edn_ret)]))
                      "application/edn"
                      (pr-str edn_ret)))))))
          (lib/resource :available-media-types edn-or-html-media :exists? false)))))
  (reset-meta!
    #'db-transact
    (assoc
      {:arglists (clojure.core/list ['storage 'dbname]), :column (int 1)}
      :name
      'db-transact
      :ns
      *ns*))
  (defn db-info
    ([storage dbname t]
      (let [temp__5823__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5823__auto__
          (let [c temp__5823__auto__ created (atom nil)]
            (lib/resource
              :available-media-types
              edn-or-html-media
              :method-allowed?
              (request-method-in #{:get :head :options})
              :handle-ok
              (fn fn__30035
                ([context]
                  (let [db (d/db c)
                        tt (if (= t "-") (d/basis-t db) (read-edn t))
                        edn_ret {:db/alias (str storage "/" dbname), :basis-t tt}
                        G__30036 (get-in context [:representation :media-type])]
                    (case
                      G__30036
                      ("application/xhtml+xml" "text/html")
                      (html5
                        [:h3
                         "GET "
                         [:a {:rel "up", :href "../../../.."} "/"]
                         [:a {:rel "up", :href "../../.."} "data" "/"]
                         [:a {:rel "up", :href "../.."} storage "/"]
                         (str dbname "/" (or t "-") "/")]
                        [:h4 "db info"]
                        [:a {:rel "bookmark", :href (str "../" tt "/")} "permalink"]
                        [:span
                         " - A link to the db at the point in time GET was called, or the t supplied in this GET."]
                        [:br]
                        [:a {:rel "latest-version", :href (str "../-/")} "latest"]
                        [:span
                         " - A link to the most recent version of the db at the point it is used."
                         "The '-' in the URI path segment following the db name indicates this."]
                        [:br]
                        [:p
                         "Note that the datoms and entity links below (and all sub-navigation) will adopt the t basis from this page."]
                        [:a {:rel "related", :href (str "../" t "/datoms")} "datoms"]
                        [:span " - Get a range of datoms from the index."]
                        [:br]
                        [:a {:rel "related", :href (str "../" t "/entity")} "entity"]
                        [:span " - Get a particular entity."]
                        [:br]
                        [:a {:rel "monitor", :href (str "../" "-/events")} "events"]
                        [:span " - Get a stream of transaction events for this db."]
                        [:hr]
                        [:h4 "application/edn"]
                        [:code (pr-str edn_ret)]
                        [:p]
                        [:p
                         "The data returned is the equivalent of the permalink. A map in ths format is called a "
                         [:em "database descriptor"]
                         ". You will use database descriptors to 'pass' databases to e.g. query. Other possible fields in descriptors are: "
                         [:code ":as-of,  :since and :history"]
                         ". Without :basis-t or :as-of, a descriptor behaves like 'latest'."]
                        [:hr]
                        [:h3 "Transact:"]
                        [:p
                         "Add data to the database via a transaction. The data should be in "
                         [:a {:href "http://edn-format.org"} "application/edn"]
                         " format, as further described in the "
                         [:a
                          {:rel "help", :href "https://docs.datomic.com/transactions.html"}
                          "transactions"]
                         " documentation, and the reference for "
                         [:a
                          {:rel "help",
                           :href
                           "https://docs.datomic.com/clojure/index.html#datomic.api/transact"}
                          "transact"]
                         "."]
                        (hf/form-to
                          [:post ".."]
                          "tx-data *:"
                          [:br]
                          (hf/text-area {:required "required", :rows "16", :cols "80"} "tx-data")
                          [:p " "]
                          (hf/submit-button "Submit transaction"))
                        [:p
                         "The above form POSTs to: /data/"
                         storage
                         "/"
                         dbname
                         "/ .\nThe form also illustrates how one would call the API via a POST. When you send form data in the body it should be in the standard "
                         [:a
                          {:href "http://www.w3.org/MarkUp/html-spec/html-spec_8.html#SEC8.2.1"}
                          "application/x-www-form-urlencoded"]
                         " media type. The API will return 201 on transaction success."]
                        [:p "You can try the data below (please use a test db!)"]
                        [:p [:pre "[{:db/id #db/id[:db.part/user]\n  :db/doc \"I'm new!\"}]"]])
                      "application/edn"
                      (pr-str edn_ret)))))))
          (lib/resource :available-media-types edn-or-html-media :exists? false)))))
  (reset-meta!
    #'db-info
    (assoc
      {:arglists (clojure.core/list ['storage 'dbname 't]), :column (int 1)}
      :name
      'db-info
      :ns
      *ns*))
  (defn get-datoms
    ([params db]
      (let [map__30042 params
            map__30042 (if (seq? map__30042)
                         (if (next map__30042)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30042))
                           (if (seq map__30042) (first map__30042) {}))
                         map__30042)
            e (get map__30042 :e)
            a (get map__30042 :a)
            v (get map__30042 :v)
            index (get map__30042 :index)
            index (keyword index)
            db (windowed db params)
            args (let [G__30043 index]
                   (case G__30043 :aevt [a e v] :avet [a v e] :eavt [e a v] :vaet [v a e]))]
        (mapv
          datom->map
          (limited (apply d/datoms db index (take-while (complement nil?) args)) params)))))
  (reset-meta!
    #'get-datoms
    (assoc
      {:arglists (clojure.core/list ['params 'db]), :column (int 1)}
      :name
      'get-datoms
      :ns
      *ns*))
  (defn get-range
    ([params db]
      (let [map__30045 params
            map__30045 (if (seq? map__30045)
                         (if (next map__30045)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30045))
                           (if (seq map__30045) (first map__30045) {}))
                         map__30045)
            a (get map__30045 :a)
            start (get map__30045 :start)
            end (get map__30045 :end)
            db (windowed db params)]
        (mapv datom->map (limited (d/index-range db a start end) params)))))
  (reset-meta!
    #'get-range
    (assoc
      {:arglists (clojure.core/list ['params 'db]), :column (int 1)}
      :name
      'get-range
      :ns
      *ns*))
  (defn db-datoms
    ([storage dbname t]
      (let [temp__5823__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5823__auto__
          (let [c temp__5823__auto__ created (atom nil)]
            (lib/resource
              :available-media-types
              edn-or-html-media
              :method-allowed?
              (request-method-in #{:get :head :options})
              :handle-ok
              (fn fn__30050
                ([context]
                  (let [params (:params (:request context))
                        map__30051 params
                        map__30051 (if (seq? map__30051)
                                     (if (next map__30051)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__30051))
                                       (if (seq map__30051) (first map__30051) {}))
                                     map__30051)
                        end (get map__30051 :end)
                        a (get map__30051 :a)
                        since (get map__30051 :since)
                        v (get map__30051 :v)
                        limit (get map__30051 :limit)
                        index (get map__30051 :index)
                        offset (get map__30051 :offset)
                        start (get map__30051 :start)
                        history (get map__30051 :history)
                        e (get map__30051 :e)
                        as_of (get map__30051 :as-of)
                        db (d/db c)
                        params (if (= t "-") params (assoc params :basis-t (read-edn t)))
                        p (fn p ([p1__30047#] (when p1__30047# (pr-str p1__30047#))))
                        datoms (when index
                                 ((if (and (= index 'avet) a (or start end)) get-range get-datoms)
                                   params
                                   db))
                        G__30054 (get-in context [:representation :media-type])]
                    (case
                      G__30054
                      ("application/xhtml+xml" "text/html")
                      (let [ecell (fn ecell
                                    ([eid]
                                      [:td
                                       [:a
                                        {:rel "entity", :href (str "entity?e=" eid)}
                                        (pr-str (or (d/ident db eid) eid))]]))]
                        (html5
                          [:h3
                           "GET "
                           [:a {:rel "up", :href "../../../.."} "/"]
                           [:a {:rel "up", :href "../../.."} "data" "/"]
                           [:a {:rel "up", :href "../.."} storage "/"]
                           [:a {:rel "up", :href "."} (str dbname "/" (or t "-") "/")]
                           "datoms"]
                          [:p
                           "Gets a set or range of datoms from an index. This resource covers both the "
                           [:a
                            {:rel "help",
                             :href
                             "https://docs.datomic.com/clojure/index.html#datomic.api/datoms"}
                            "datoms"]
                           " and "
                           [:a
                            {:rel "help",
                             :href
                             "https://docs.datomic.com/clojure/index.html#datomic.api/index-range"}
                            "index-range"]
                           " APIs."]
                          [:p
                           "The following helper form builds the query params for a GET on this same resource. For any given index, you can supply zero or more of the leading components (without skipping) to narrow the result."
                           " Iff the 'avet' index is selected, and 'a' is supplied, you can use 'start' and/or 'end' to get a range."]
                          [:p
                           " e/a/v can be passed entity ids or idents."
                           " as-of/since/history can be passed t, tx or dates."]
                          [:p
                           "The service expects query params to be "
                           [:a {:href "http://edn-format.org"} "edn"]
                           " elements, so you can pass\nkeywords or #inst dates etc."]
                          [:hr]
                          (hf/form-to
                            [:get ""]
                            "index : "
                            (hf/drop-down
                              "index"
                              ["aevt" "eavt" "avet" "vaet"]
                              (^clojure.lang.IFn p index))
                            [:br]
                            "e : "
                            (hf/text-field "e" (^clojure.lang.IFn p e))
                            "a : "
                            (hf/text-field "a" (^clojure.lang.IFn p a))
                            "v : "
                            (hf/text-field "v" (^clojure.lang.IFn p v))
                            [:br]
                            "start : "
                            (hf/text-field "start" (^clojure.lang.IFn p start))
                            "end : "
                            (hf/text-field "end" (^clojure.lang.IFn p end))
                            [:br]
                            "offset : "
                            (hf/text-field "offset" (^clojure.lang.IFn p offset))
                            "limit : "
                            (hf/text-field "limit" (if index (^clojure.lang.IFn p limit) "100"))
                            [:br]
                            "as-of : "
                            (hf/text-field "as-of" (^clojure.lang.IFn p as_of))
                            "since : "
                            (hf/text-field "since" (^clojure.lang.IFn p since))
                            [:br]
                            "history : "
                            (hf/check-box "history" (boolean (^clojure.lang.IFn p history)))
                            [:br]
                            [:p " "]
                            (hf/submit-button "Get datoms"))
                          [:hr]
                          [:h3 (str (.toUpperCase (str (:index params))) " Datoms")]
                          (datoms-table (:index params) datoms ecell)
                          [:hr]
                          [:h4 "application/edn"]
                          [:code (pr-str datoms)]))
                      "application/edn"
                      (pr-str datoms)))))))
          (lib/resource :available-media-types edn-or-html-media :exists? false)))))
  (reset-meta!
    #'db-datoms
    (assoc
      {:arglists (clojure.core/list ['storage 'dbname 't]), :column (int 1)}
      :name
      'db-datoms
      :ns
      *ns*))
  (defn db-entity
    ([storage dbname t]
      (let [temp__5823__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5823__auto__
          (let [c temp__5823__auto__ created (atom nil)]
            (lib/resource
              :available-media-types
              edn-or-html-media
              :method-allowed?
              (request-method-in #{:get :head :options})
              :handle-ok
              (fn fn__30075
                ([context]
                  (let [params (:params (:request context))
                        map__30076 params
                        map__30076 (if (seq? map__30076)
                                     (if (next map__30076)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__30076))
                                       (if (seq map__30076) (first map__30076) {}))
                                     map__30076)
                        e (get map__30076 :e)
                        as_of (get map__30076 :as-of)
                        since (get map__30076 :since)
                        db (d/db c)
                        params (if (= t "-") params (assoc params :basis-t (read-edn t)))
                        p (fn p ([p1__30069#] (when p1__30069# (pr-str p1__30069#))))
                        emap (when e (d/touch (d/entity (windowed db params) e)))
                        elink (fn elink
                                ([eid]
                                  (let [eid (db/resolve-id db eid)]
                                    [:a
                                     {:rel "entity", :href (str "./entity?e=" eid)}
                                     (pr-str (or (d/ident db eid) eid))])))
                        amany? (fn amany_QMARK_
                                 ([p1__30070#]
                                   (=
                                     36
                                     (.-cardinality
                                       (.elementAt
                                         ^datomic.db.IDbImpl db
                                         (db/resolve-id db p1__30070#))))))
                        ref? (fn ref_QMARK_
                               ([p1__30071#]
                                 (=
                                   20
                                   (.-vtypeid
                                     (.elementAt
                                       ^datomic.db.IDbImpl db
                                       (db/resolve-id db p1__30071#))))))
                        component? (fn component_QMARK_
                                     ([p1__30072#]
                                       (.-isComponent
                                         (.elementAt
                                           ^datomic.db.IDbImpl db
                                           (db/resolve-id db p1__30072#)))))
                        etable (fn etable
                                 ([emap]
                                   (let [vcell (fn vcell
                                                 ([a v]
                                                   (cond
                                                     (instance? datomic.query.EMapImpl v) (if
                                                                                            (^clojure.lang.IFn component?
                                                                                              a)
                                                                                            (^clojure.lang.IFn etable
                                                                                              v)
                                                                                            (^clojure.lang.IFn elink
                                                                                              (:db/id
                                                                                                v)))
                                                     (^clojure.lang.IFn ref? a) (^clojure.lang.IFn elink
                                                                                  (db/resolve-id
                                                                                    db
                                                                                    v))
                                                     :else (do (pr-str v)))))]
                                     [:table
                                      {:id (:db/id emap), :class "table"}
                                      [:tr
                                       [:td ":db/id"]
                                       [:td (^clojure.lang.IFn elink (:db/id emap))]]
                                      (let [iter__6398__auto__ (fn
                                                                 iter__30091
                                                                 ([s__30092]
                                                                   (lazy-seq
                                                                     (let
                                                                       [s__30092 s__30092
                                                                        temp__5825__auto__
                                                                        (seq s__30092)]
                                                                       (when
                                                                         temp__5825__auto__
                                                                         (let
                                                                           [s__30092
                                                                            temp__5825__auto__]
                                                                           (if
                                                                             (chunked-seq?
                                                                               s__30092)
                                                                             (let
                                                                               [c__6396__auto__
                                                                                (chunk-first
                                                                                  s__30092)
                                                                                size__6397__auto__
                                                                                (int
                                                                                  (count
                                                                                    c__6396__auto__))
                                                                                b__30094
                                                                                (chunk-buffer
                                                                                  (java.lang.Integer/valueOf
                                                                                    (int
                                                                                      size__6397__auto__)))]
                                                                               (if
                                                                                 (loop
                                                                                   [i__30093
                                                                                    (int 0)]
                                                                                   (if
                                                                                     (<
                                                                                       i__30093
                                                                                       size__6397__auto__)
                                                                                     (let
                                                                                       [vec__30098
                                                                                        (.nth
                                                                                          ^clojure.lang.Indexed c__6396__auto__
                                                                                          (int
                                                                                            i__30093))
                                                                                        a
                                                                                        (nth
                                                                                          vec__30098
                                                                                          (int 0)
                                                                                          nil)
                                                                                        v
                                                                                        (nth
                                                                                          vec__30098
                                                                                          (int 1)
                                                                                          nil)]
                                                                                       (chunk-append
                                                                                         b__30094
                                                                                         (if
                                                                                           (^clojure.lang.IFn amany?
                                                                                             a)
                                                                                           (seq
                                                                                             (into
                                                                                               [[:tr
                                                                                                 [:td
                                                                                                  (^clojure.lang.IFn elink
                                                                                                    a)]
                                                                                                 [:td
                                                                                                  (^clojure.lang.IFn vcell
                                                                                                    a
                                                                                                    (first
                                                                                                      v))]]]
                                                                                               (let
                                                                                                 [iter__6398__auto__
                                                                                                  (fn
                                                                                                    iter__30101
                                                                                                    ([s__30102]
                                                                                                      (lazy-seq
                                                                                                        (let
                                                                                                          [s__30102
                                                                                                           s__30102
                                                                                                           temp__5825__auto__
                                                                                                           (seq
                                                                                                             s__30102)]
                                                                                                          (when
                                                                                                            temp__5825__auto__
                                                                                                            (let
                                                                                                              [s__30102
                                                                                                               temp__5825__auto__]
                                                                                                              (if
                                                                                                                (chunked-seq?
                                                                                                                  s__30102)
                                                                                                                (let
                                                                                                                  [c__6396__auto__
                                                                                                                   (chunk-first
                                                                                                                     s__30102)
                                                                                                                   size__6397__auto__
                                                                                                                   (int
                                                                                                                     (count
                                                                                                                       c__6396__auto__))
                                                                                                                   b__30104
                                                                                                                   (chunk-buffer
                                                                                                                     (java.lang.Integer/valueOf
                                                                                                                       (int
                                                                                                                         size__6397__auto__)))]
                                                                                                                  (if
                                                                                                                    (loop
                                                                                                                      [i__30103
                                                                                                                       (int
                                                                                                                         0)]
                                                                                                                      (if
                                                                                                                        (<
                                                                                                                          i__30103
                                                                                                                          size__6397__auto__)
                                                                                                                        (let
                                                                                                                          [v
                                                                                                                           (.nth
                                                                                                                             ^clojure.lang.Indexed c__6396__auto__
                                                                                                                             (int
                                                                                                                               i__30103))]
                                                                                                                          (chunk-append
                                                                                                                            b__30104
                                                                                                                            [:tr
                                                                                                                             [:td]
                                                                                                                             [:td
                                                                                                                              (^clojure.lang.IFn vcell
                                                                                                                                a
                                                                                                                                v)]])
                                                                                                                          (recur
                                                                                                                            (inc
                                                                                                                              i__30103)))
                                                                                                                        true))
                                                                                                                    (chunk-cons
                                                                                                                      (chunk
                                                                                                                        b__30104)
                                                                                                                      (^clojure.lang.IFn iter__30101
                                                                                                                        (chunk-rest
                                                                                                                          s__30102)))
                                                                                                                    (chunk-cons
                                                                                                                      (chunk
                                                                                                                        b__30104)
                                                                                                                      nil)))
                                                                                                                (let
                                                                                                                  [v
                                                                                                                   (first
                                                                                                                     s__30102)]
                                                                                                                  (cons
                                                                                                                    [:tr
                                                                                                                     [:td]
                                                                                                                     [:td
                                                                                                                      (^clojure.lang.IFn vcell
                                                                                                                        a
                                                                                                                        v)]]
                                                                                                                    (^clojure.lang.IFn iter__30101
                                                                                                                      (rest
                                                                                                                        s__30102)))))))))))]
                                                                                                 (^clojure.lang.IFn iter__6398__auto__
                                                                                                   (rest
                                                                                                     v)))))
                                                                                           [:tr
                                                                                            [:td
                                                                                             (^clojure.lang.IFn elink
                                                                                               a)]
                                                                                            [:td
                                                                                             (^clojure.lang.IFn vcell
                                                                                               a
                                                                                               v)]]))
                                                                                       (recur
                                                                                         (inc
                                                                                           i__30093)))
                                                                                     true))
                                                                                 (chunk-cons
                                                                                   (chunk b__30094)
                                                                                   (^clojure.lang.IFn iter__30091
                                                                                     (chunk-rest
                                                                                       s__30092)))
                                                                                 (chunk-cons
                                                                                   (chunk b__30094)
                                                                                   nil)))
                                                                             (let
                                                                               [vec__30116
                                                                                (first s__30092)
                                                                                a
                                                                                (nth
                                                                                  vec__30116
                                                                                  (int 0)
                                                                                  nil)
                                                                                v
                                                                                (nth
                                                                                  vec__30116
                                                                                  (int 1)
                                                                                  nil)]
                                                                               (cons
                                                                                 (if
                                                                                   (^clojure.lang.IFn amany?
                                                                                     a)
                                                                                   (seq
                                                                                     (into
                                                                                       [[:tr
                                                                                         [:td
                                                                                          (^clojure.lang.IFn elink
                                                                                            a)]
                                                                                         [:td
                                                                                          (^clojure.lang.IFn vcell
                                                                                            a
                                                                                            (first
                                                                                              v))]]]
                                                                                       (let
                                                                                         [iter__6398__auto__
                                                                                          (fn
                                                                                            iter__30119
                                                                                            ([s__30120]
                                                                                              (lazy-seq
                                                                                                (let
                                                                                                  [s__30120
                                                                                                   s__30120
                                                                                                   temp__5825__auto__
                                                                                                   (seq
                                                                                                     s__30120)]
                                                                                                  (when
                                                                                                    temp__5825__auto__
                                                                                                    (let
                                                                                                      [s__30120
                                                                                                       temp__5825__auto__]
                                                                                                      (if
                                                                                                        (chunked-seq?
                                                                                                          s__30120)
                                                                                                        (let
                                                                                                          [c__6396__auto__
                                                                                                           (chunk-first
                                                                                                             s__30120)
                                                                                                           size__6397__auto__
                                                                                                           (int
                                                                                                             (count
                                                                                                               c__6396__auto__))
                                                                                                           b__30122
                                                                                                           (chunk-buffer
                                                                                                             (java.lang.Integer/valueOf
                                                                                                               (int
                                                                                                                 size__6397__auto__)))]
                                                                                                          (if
                                                                                                            (loop
                                                                                                              [i__30121
                                                                                                               (int
                                                                                                                 0)]
                                                                                                              (if
                                                                                                                (<
                                                                                                                  i__30121
                                                                                                                  size__6397__auto__)
                                                                                                                (let
                                                                                                                  [v
                                                                                                                   (.nth
                                                                                                                     ^clojure.lang.Indexed c__6396__auto__
                                                                                                                     (int
                                                                                                                       i__30121))]
                                                                                                                  (chunk-append
                                                                                                                    b__30122
                                                                                                                    [:tr
                                                                                                                     [:td]
                                                                                                                     [:td
                                                                                                                      (^clojure.lang.IFn vcell
                                                                                                                        a
                                                                                                                        v)]])
                                                                                                                  (recur
                                                                                                                    (inc
                                                                                                                      i__30121)))
                                                                                                                true))
                                                                                                            (chunk-cons
                                                                                                              (chunk
                                                                                                                b__30122)
                                                                                                              (^clojure.lang.IFn iter__30119
                                                                                                                (chunk-rest
                                                                                                                  s__30120)))
                                                                                                            (chunk-cons
                                                                                                              (chunk
                                                                                                                b__30122)
                                                                                                              nil)))
                                                                                                        (let
                                                                                                          [v
                                                                                                           (first
                                                                                                             s__30120)]
                                                                                                          (cons
                                                                                                            [:tr
                                                                                                             [:td]
                                                                                                             [:td
                                                                                                              (^clojure.lang.IFn vcell
                                                                                                                a
                                                                                                                v)]]
                                                                                                            (^clojure.lang.IFn iter__30119
                                                                                                              (rest
                                                                                                                s__30120)))))))))))]
                                                                                         (^clojure.lang.IFn iter__6398__auto__
                                                                                           (rest
                                                                                             v)))))
                                                                                   [:tr
                                                                                    [:td
                                                                                     (^clojure.lang.IFn elink
                                                                                       a)]
                                                                                    [:td
                                                                                     (^clojure.lang.IFn vcell
                                                                                       a
                                                                                       v)]])
                                                                                 (^clojure.lang.IFn iter__30091
                                                                                   (rest
                                                                                     s__30092)))))))))))]
                                        (^clojure.lang.IFn iter__6398__auto__ emap))])))
                        G__30140 (get-in context [:representation :media-type])]
                    (case
                      G__30140
                      ("application/xhtml+xml" "text/html")
                      (html5
                        [:h3
                         "GET "
                         [:a {:rel "up", :href "../../../.."} "/"]
                         [:a {:rel "up", :href "../../.."} "data" "/"]
                         [:a {:rel "up", :href "../.."} storage "/"]
                         [:a {:rel "up", :href "."} dbname "/" (or t "-") "/"]
                         "entity"]
                        [:p "Gets an entity and all of its components."]
                        [:p
                         "The following helper form builds the query params for a GET on this same resource. "]
                        [:p
                         " e can be passed as entity id or ident."
                         " as-of/since can be passed t, tx or dates."]
                        [:p
                         "The service expects query params to be "
                         [:a {:href "http://edn-format.org"} "edn"]
                         " elements, so you can pass\nkeywords or #inst dates etc."]
                        [:hr]
                        (hf/form-to
                          [:get ""]
                          "e *: "
                          (hf/text-field {:required "required"} "e" (^clojure.lang.IFn p e))
                          " as-of : "
                          (hf/text-field "as-of" (^clojure.lang.IFn p as_of))
                          " since : "
                          (hf/text-field "since" (^clojure.lang.IFn p since))
                          [:p " "]
                          (hf/submit-button "Get entity"))
                        [:hr]
                        [:h3 (str "Entity: " e)]
                        (when e (^clojure.lang.IFn etable emap))
                        [:hr]
                        [:h4 "application/edn"]
                        [:code (pr-str emap)])
                      "application/edn"
                      (pr-str emap)))))))
          (lib/resource :available-media-types edn-or-html-media :exists? false)))))
  (reset-meta!
    #'db-entity
    (assoc
      {:arglists (clojure.core/list ['storage 'dbname 't]), :column (int 1)}
      :name
      'db-entity
      :ns
      *ns*))
  (def query
   (fn query
     ([request__29717__auto__]
       (lib/run-resource
         request__29717__auto__
         (lib/get-options
           (clojure.core/list
             :available-media-types
             edn-or-html-media
             :method-allowed?
             (request-method-in #{:get :head :post :options})
             :post-redirect?
             false
             :new?
             false
             :respond-with-entity?
             true
             :multiple-representations?
             false
             :post!
             true
             :handle-ok
             (fn fn__30147
               ([context]
                 (let [params (:params (:request context))
                       map__30148 params
                       map__30148 (if (seq? map__30148)
                                    (if (next map__30148)
                                      (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                        (to-array map__30148))
                                      (if (seq map__30148) (first map__30148) {}))
                                    map__30148)
                       q (get map__30148 :q)
                       args (get map__30148 :args)
                       offset (get map__30148 :offset)
                       limit (get map__30148 :limit)
                       oq q
                       q (if (sequential? q) (dq/listq->mapq q) q)
                       xargs (when args
                               (mapv
                                 (fn fn__30149
                                   ([p1__30146#]
                                     (if (:db/alias p1__30146#)
                                       (let [dbsym (symbol (:db/alias p1__30146#))
                                             storage (namespace dbsym)
                                             dbname (name dbsym)
                                             c (conn storage dbname)]
                                         (when-not c
                                           (throw
                                             (java.lang.AssertionError.
                                               (str
                                                 "Assert failed: "
                                                 (str "Can't find db: " (:db/alias p1__30146#))
                                                 "\n"
                                                 (pr-str 'c)))))
                                         (windowed (d/db c) p1__30146#))
                                       p1__30146#)))
                                 args))
                       result (if q (vec (limited (apply d/q q xargs) params)) [])
                       G__30151 (get-in context [:representation :media-type])]
                   (case
                     G__30151
                     "application/xhtml+xml"
                     (html5
                       [:h3
                        "GET "
                        [:a {:rel "up", :href "../.."} "/"]
                        [:a {:rel "up", :href "."} "api" "/"]
                        "query"]
                       [:p "Issue a query."]
                       [:p
                        "The following helper form builds the query params for a GET on this same resource."]
                       [:p
                        "The query data (q) should be in "
                        [:a {:href "http://edn-format.org"} "application/edn"]
                        " format, as further described in the "
                        [:a {:rel "help", :href "https://docs.datomic.com/query.html"} "query"]
                        " documentation, and the reference for "
                        [:a
                         {:rel "help",
                          :href "https://docs.datomic.com/clojure/index.html#datomic.api/q"}
                         "q"]
                        "."
                        " args, if supplied, must be in a vector, as multiple args are conveyed in a single query parameter."]
                       [:p
                        "Note that api/query is not associated with any specific db, and is capable of querying any and all dbs accessible by the service. Thus, any dbs must be conveyed in 'args' via descriptors."]
                       [:hr]
                       (hf/form-to
                         [:get ""]
                         "q *: "
                         (hf/text-area
                           {:required "required", :rows "6", :cols "80"}
                           "q"
                           (when oq (pr-str oq)))
                         " args : "
                         (hf/text-area {:rows "6", :cols "80"} "args" (when args (pr-str args)))
                         [:br]
                         "offset : "
                         (hf/text-field "offset" (when offset (str offset)))
                         " limit : "
                         (hf/text-field "limit" (when limit (str limit)))
                         [:p " "]
                         (hf/submit-button "Query"))
                       [:p
                        "You can try this query: "
                        [:code "[:find ?e ?v :in $ :where [?e :db/doc ?v]]"]
                        " with these args: "
                        [:code "[{:db/alias \"your-storage/your-db\"}]"]]
                       [:hr]
                       [:h3 "Result:"]
                       [:table
                        {:id "result", :class "table"}
                        [:tr
                         (let [iter__6398__auto__ (fn iter__30152
                                                    ([s__30153]
                                                      (lazy-seq
                                                        (let [s__30153 s__30153
                                                              temp__5825__auto__ (seq s__30153)]
                                                          (when temp__5825__auto__
                                                            (let
                                                              [s__30153 temp__5825__auto__]
                                                              (if
                                                                (chunked-seq? s__30153)
                                                                (let
                                                                  [c__6396__auto__
                                                                   (chunk-first s__30153)
                                                                   size__6397__auto__
                                                                   (int (count c__6396__auto__))
                                                                   b__30155
                                                                   (chunk-buffer
                                                                     (java.lang.Integer/valueOf
                                                                       (int size__6397__auto__)))]
                                                                  (if
                                                                    (loop
                                                                      [i__30154 (int 0)]
                                                                      (if
                                                                        (<
                                                                          i__30154
                                                                          size__6397__auto__)
                                                                        (let
                                                                          [bind
                                                                           (.nth
                                                                             ^clojure.lang.Indexed c__6396__auto__
                                                                             (int i__30154))]
                                                                          (chunk-append
                                                                            b__30155
                                                                            [:th (str bind)])
                                                                          (recur (inc i__30154)))
                                                                        true))
                                                                    (chunk-cons
                                                                      (chunk b__30155)
                                                                      (^clojure.lang.IFn iter__30152
                                                                        (chunk-rest s__30153)))
                                                                    (chunk-cons
                                                                      (chunk b__30155)
                                                                      nil)))
                                                                (let
                                                                  [bind (first s__30153)]
                                                                  (cons
                                                                    [:th (str bind)]
                                                                    (^clojure.lang.IFn iter__30152
                                                                      (rest s__30153)))))))))))]
                           (^clojure.lang.IFn iter__6398__auto__ (:find q)))]
                        (let [iter__6398__auto__ (fn iter__30165
                                                   ([s__30166]
                                                     (lazy-seq
                                                       (let [s__30166 s__30166
                                                             temp__5825__auto__ (seq s__30166)]
                                                         (when temp__5825__auto__
                                                           (let
                                                             [s__30166 temp__5825__auto__]
                                                             (if
                                                               (chunked-seq? s__30166)
                                                               (let
                                                                 [c__6396__auto__
                                                                  (chunk-first s__30166)
                                                                  size__6397__auto__
                                                                  (int (count c__6396__auto__))
                                                                  b__30168
                                                                  (chunk-buffer
                                                                    (java.lang.Integer/valueOf
                                                                      (int size__6397__auto__)))]
                                                                 (if
                                                                   (loop
                                                                     [i__30167 (int 0)]
                                                                     (if
                                                                       (<
                                                                         i__30167
                                                                         size__6397__auto__)
                                                                       (let
                                                                         [r
                                                                          (.nth
                                                                            ^clojure.lang.Indexed c__6396__auto__
                                                                            (int i__30167))]
                                                                         (chunk-append
                                                                           b__30168
                                                                           [:tr
                                                                            (let
                                                                              [iter__6398__auto__
                                                                               (fn
                                                                                 iter__30172
                                                                                 ([s__30173]
                                                                                   (lazy-seq
                                                                                     (let
                                                                                       [s__30173
                                                                                        s__30173
                                                                                        temp__5825__auto__
                                                                                        (seq
                                                                                          s__30173)]
                                                                                       (when
                                                                                         temp__5825__auto__
                                                                                         (let
                                                                                           [s__30173
                                                                                            temp__5825__auto__]
                                                                                           (if
                                                                                             (chunked-seq?
                                                                                               s__30173)
                                                                                             (let
                                                                                               [c__6396__auto__
                                                                                                (chunk-first
                                                                                                  s__30173)
                                                                                                size__6397__auto__
                                                                                                (int
                                                                                                  (count
                                                                                                    c__6396__auto__))
                                                                                                b__30175
                                                                                                (chunk-buffer
                                                                                                  (java.lang.Integer/valueOf
                                                                                                    (int
                                                                                                      size__6397__auto__)))]
                                                                                               (if
                                                                                                 (loop
                                                                                                   [i__30174
                                                                                                    (int
                                                                                                      0)]
                                                                                                   (if
                                                                                                     (<
                                                                                                       i__30174
                                                                                                       size__6397__auto__)
                                                                                                     (let
                                                                                                       [c
                                                                                                        (.nth
                                                                                                          ^clojure.lang.Indexed c__6396__auto__
                                                                                                          (int
                                                                                                            i__30174))]
                                                                                                       (chunk-append
                                                                                                         b__30175
                                                                                                         [:td
                                                                                                          (pr-str
                                                                                                            c)])
                                                                                                       (recur
                                                                                                         (inc
                                                                                                           i__30174)))
                                                                                                     true))
                                                                                                 (chunk-cons
                                                                                                   (chunk
                                                                                                     b__30175)
                                                                                                   (^clojure.lang.IFn iter__30172
                                                                                                     (chunk-rest
                                                                                                       s__30173)))
                                                                                                 (chunk-cons
                                                                                                   (chunk
                                                                                                     b__30175)
                                                                                                   nil)))
                                                                                             (let
                                                                                               [c
                                                                                                (first
                                                                                                  s__30173)]
                                                                                               (cons
                                                                                                 [:td
                                                                                                  (pr-str
                                                                                                    c)]
                                                                                                 (^clojure.lang.IFn iter__30172
                                                                                                   (rest
                                                                                                     s__30173)))))))))))]
                                                                              (^clojure.lang.IFn iter__6398__auto__
                                                                                r))])
                                                                         (recur (inc i__30167)))
                                                                       true))
                                                                   (chunk-cons
                                                                     (chunk b__30168)
                                                                     (^clojure.lang.IFn iter__30165
                                                                       (chunk-rest s__30166)))
                                                                   (chunk-cons
                                                                     (chunk b__30168)
                                                                     nil)))
                                                               (let
                                                                 [r (first s__30166)]
                                                                 (cons
                                                                   [:tr
                                                                    (let
                                                                      [iter__6398__auto__
                                                                       (fn
                                                                         iter__30187
                                                                         ([s__30188]
                                                                           (lazy-seq
                                                                             (let
                                                                               [s__30188 s__30188
                                                                                temp__5825__auto__
                                                                                (seq s__30188)]
                                                                               (when
                                                                                 temp__5825__auto__
                                                                                 (let
                                                                                   [s__30188
                                                                                    temp__5825__auto__]
                                                                                   (if
                                                                                     (chunked-seq?
                                                                                       s__30188)
                                                                                     (let
                                                                                       [c__6396__auto__
                                                                                        (chunk-first
                                                                                          s__30188)
                                                                                        size__6397__auto__
                                                                                        (int
                                                                                          (count
                                                                                            c__6396__auto__))
                                                                                        b__30190
                                                                                        (chunk-buffer
                                                                                          (java.lang.Integer/valueOf
                                                                                            (int
                                                                                              size__6397__auto__)))]
                                                                                       (if
                                                                                         (loop
                                                                                           [i__30189
                                                                                            (int
                                                                                              0)]
                                                                                           (if
                                                                                             (<
                                                                                               i__30189
                                                                                               size__6397__auto__)
                                                                                             (let
                                                                                               [c
                                                                                                (.nth
                                                                                                  ^clojure.lang.Indexed c__6396__auto__
                                                                                                  (int
                                                                                                    i__30189))]
                                                                                               (chunk-append
                                                                                                 b__30190
                                                                                                 [:td
                                                                                                  (pr-str
                                                                                                    c)])
                                                                                               (recur
                                                                                                 (inc
                                                                                                   i__30189)))
                                                                                             true))
                                                                                         (chunk-cons
                                                                                           (chunk
                                                                                             b__30190)
                                                                                           (^clojure.lang.IFn iter__30187
                                                                                             (chunk-rest
                                                                                               s__30188)))
                                                                                         (chunk-cons
                                                                                           (chunk
                                                                                             b__30190)
                                                                                           nil)))
                                                                                     (let
                                                                                       [c
                                                                                        (first
                                                                                          s__30188)]
                                                                                       (cons
                                                                                         [:td
                                                                                          (pr-str
                                                                                            c)]
                                                                                         (^clojure.lang.IFn iter__30187
                                                                                           (rest
                                                                                             s__30188)))))))))))]
                                                                      (^clojure.lang.IFn iter__6398__auto__
                                                                        r))]
                                                                   (^clojure.lang.IFn iter__30165
                                                                     (rest s__30166)))))))))))]
                          (^clojure.lang.IFn iter__6398__auto__ result))]
                       [:hr]
                       [:h4 "application/edn"]
                       [:code (pr-str result)])
                     "text/html"
                     (html5
                       [:h3
                        "GET "
                        [:a {:rel "up", :href "../.."} "/"]
                        [:a {:rel "up", :href "."} "api" "/"]
                        "query"]
                       [:p "Issue a query."]
                       [:p
                        "The following helper form builds the query params for a GET on this same resource."]
                       [:p
                        "The query data (q) should be in "
                        [:a {:href "http://edn-format.org"} "application/edn"]
                        " format, as further described in the "
                        [:a {:rel "help", :href "https://docs.datomic.com/query.html"} "query"]
                        " documentation, and the reference for "
                        [:a
                         {:rel "help",
                          :href "https://docs.datomic.com/clojure/index.html#datomic.api/q"}
                         "q"]
                        "."
                        " args, if supplied, must be in a vector, as multiple args are conveyed in a single query parameter."]
                       [:p
                        "Note that api/query is not associated with any specific db, and is capable of querying any and all dbs accessible by the service. Thus, any dbs must be conveyed in 'args' via descriptors."]
                       [:hr]
                       (hf/form-to
                         [:get ""]
                         "q *: "
                         (hf/text-area
                           {:required "required", :rows "6", :cols "80"}
                           "q"
                           (when oq (pr-str oq)))
                         " args : "
                         (hf/text-area {:rows "6", :cols "80"} "args" (when args (pr-str args)))
                         [:br]
                         "offset : "
                         (hf/text-field "offset" (when offset (str offset)))
                         " limit : "
                         (hf/text-field "limit" (when limit (str limit)))
                         [:p " "]
                         (hf/submit-button "Query"))
                       [:p
                        "You can try this query: "
                        [:code "[:find ?e ?v :in $ :where [?e :db/doc ?v]]"]
                        " with these args: "
                        [:code "[{:db/alias \"your-storage/your-db\"}]"]]
                       [:hr]
                       [:h3 "Result:"]
                       [:table
                        {:id "result", :class "table"}
                        [:tr
                         (let [iter__6398__auto__ (fn iter__30206
                                                    ([s__30207]
                                                      (lazy-seq
                                                        (let [s__30207 s__30207
                                                              temp__5825__auto__ (seq s__30207)]
                                                          (when temp__5825__auto__
                                                            (let
                                                              [s__30207 temp__5825__auto__]
                                                              (if
                                                                (chunked-seq? s__30207)
                                                                (let
                                                                  [c__6396__auto__
                                                                   (chunk-first s__30207)
                                                                   size__6397__auto__
                                                                   (int (count c__6396__auto__))
                                                                   b__30209
                                                                   (chunk-buffer
                                                                     (java.lang.Integer/valueOf
                                                                       (int size__6397__auto__)))]
                                                                  (if
                                                                    (loop
                                                                      [i__30208 (int 0)]
                                                                      (if
                                                                        (<
                                                                          i__30208
                                                                          size__6397__auto__)
                                                                        (let
                                                                          [bind
                                                                           (.nth
                                                                             ^clojure.lang.Indexed c__6396__auto__
                                                                             (int i__30208))]
                                                                          (chunk-append
                                                                            b__30209
                                                                            [:th (str bind)])
                                                                          (recur (inc i__30208)))
                                                                        true))
                                                                    (chunk-cons
                                                                      (chunk b__30209)
                                                                      (^clojure.lang.IFn iter__30206
                                                                        (chunk-rest s__30207)))
                                                                    (chunk-cons
                                                                      (chunk b__30209)
                                                                      nil)))
                                                                (let
                                                                  [bind (first s__30207)]
                                                                  (cons
                                                                    [:th (str bind)]
                                                                    (^clojure.lang.IFn iter__30206
                                                                      (rest s__30207)))))))))))]
                           (^clojure.lang.IFn iter__6398__auto__ (:find q)))]
                        (let [iter__6398__auto__ (fn iter__30219
                                                   ([s__30220]
                                                     (lazy-seq
                                                       (let [s__30220 s__30220
                                                             temp__5825__auto__ (seq s__30220)]
                                                         (when temp__5825__auto__
                                                           (let
                                                             [s__30220 temp__5825__auto__]
                                                             (if
                                                               (chunked-seq? s__30220)
                                                               (let
                                                                 [c__6396__auto__
                                                                  (chunk-first s__30220)
                                                                  size__6397__auto__
                                                                  (int (count c__6396__auto__))
                                                                  b__30222
                                                                  (chunk-buffer
                                                                    (java.lang.Integer/valueOf
                                                                      (int size__6397__auto__)))]
                                                                 (if
                                                                   (loop
                                                                     [i__30221 (int 0)]
                                                                     (if
                                                                       (<
                                                                         i__30221
                                                                         size__6397__auto__)
                                                                       (let
                                                                         [r
                                                                          (.nth
                                                                            ^clojure.lang.Indexed c__6396__auto__
                                                                            (int i__30221))]
                                                                         (chunk-append
                                                                           b__30222
                                                                           [:tr
                                                                            (let
                                                                              [iter__6398__auto__
                                                                               (fn
                                                                                 iter__30226
                                                                                 ([s__30227]
                                                                                   (lazy-seq
                                                                                     (let
                                                                                       [s__30227
                                                                                        s__30227
                                                                                        temp__5825__auto__
                                                                                        (seq
                                                                                          s__30227)]
                                                                                       (when
                                                                                         temp__5825__auto__
                                                                                         (let
                                                                                           [s__30227
                                                                                            temp__5825__auto__]
                                                                                           (if
                                                                                             (chunked-seq?
                                                                                               s__30227)
                                                                                             (let
                                                                                               [c__6396__auto__
                                                                                                (chunk-first
                                                                                                  s__30227)
                                                                                                size__6397__auto__
                                                                                                (int
                                                                                                  (count
                                                                                                    c__6396__auto__))
                                                                                                b__30229
                                                                                                (chunk-buffer
                                                                                                  (java.lang.Integer/valueOf
                                                                                                    (int
                                                                                                      size__6397__auto__)))]
                                                                                               (if
                                                                                                 (loop
                                                                                                   [i__30228
                                                                                                    (int
                                                                                                      0)]
                                                                                                   (if
                                                                                                     (<
                                                                                                       i__30228
                                                                                                       size__6397__auto__)
                                                                                                     (let
                                                                                                       [c
                                                                                                        (.nth
                                                                                                          ^clojure.lang.Indexed c__6396__auto__
                                                                                                          (int
                                                                                                            i__30228))]
                                                                                                       (chunk-append
                                                                                                         b__30229
                                                                                                         [:td
                                                                                                          (pr-str
                                                                                                            c)])
                                                                                                       (recur
                                                                                                         (inc
                                                                                                           i__30228)))
                                                                                                     true))
                                                                                                 (chunk-cons
                                                                                                   (chunk
                                                                                                     b__30229)
                                                                                                   (^clojure.lang.IFn iter__30226
                                                                                                     (chunk-rest
                                                                                                       s__30227)))
                                                                                                 (chunk-cons
                                                                                                   (chunk
                                                                                                     b__30229)
                                                                                                   nil)))
                                                                                             (let
                                                                                               [c
                                                                                                (first
                                                                                                  s__30227)]
                                                                                               (cons
                                                                                                 [:td
                                                                                                  (pr-str
                                                                                                    c)]
                                                                                                 (^clojure.lang.IFn iter__30226
                                                                                                   (rest
                                                                                                     s__30227)))))))))))]
                                                                              (^clojure.lang.IFn iter__6398__auto__
                                                                                r))])
                                                                         (recur (inc i__30221)))
                                                                       true))
                                                                   (chunk-cons
                                                                     (chunk b__30222)
                                                                     (^clojure.lang.IFn iter__30219
                                                                       (chunk-rest s__30220)))
                                                                   (chunk-cons
                                                                     (chunk b__30222)
                                                                     nil)))
                                                               (let
                                                                 [r (first s__30220)]
                                                                 (cons
                                                                   [:tr
                                                                    (let
                                                                      [iter__6398__auto__
                                                                       (fn
                                                                         iter__30241
                                                                         ([s__30242]
                                                                           (lazy-seq
                                                                             (let
                                                                               [s__30242 s__30242
                                                                                temp__5825__auto__
                                                                                (seq s__30242)]
                                                                               (when
                                                                                 temp__5825__auto__
                                                                                 (let
                                                                                   [s__30242
                                                                                    temp__5825__auto__]
                                                                                   (if
                                                                                     (chunked-seq?
                                                                                       s__30242)
                                                                                     (let
                                                                                       [c__6396__auto__
                                                                                        (chunk-first
                                                                                          s__30242)
                                                                                        size__6397__auto__
                                                                                        (int
                                                                                          (count
                                                                                            c__6396__auto__))
                                                                                        b__30244
                                                                                        (chunk-buffer
                                                                                          (java.lang.Integer/valueOf
                                                                                            (int
                                                                                              size__6397__auto__)))]
                                                                                       (if
                                                                                         (loop
                                                                                           [i__30243
                                                                                            (int
                                                                                              0)]
                                                                                           (if
                                                                                             (<
                                                                                               i__30243
                                                                                               size__6397__auto__)
                                                                                             (let
                                                                                               [c
                                                                                                (.nth
                                                                                                  ^clojure.lang.Indexed c__6396__auto__
                                                                                                  (int
                                                                                                    i__30243))]
                                                                                               (chunk-append
                                                                                                 b__30244
                                                                                                 [:td
                                                                                                  (pr-str
                                                                                                    c)])
                                                                                               (recur
                                                                                                 (inc
                                                                                                   i__30243)))
                                                                                             true))
                                                                                         (chunk-cons
                                                                                           (chunk
                                                                                             b__30244)
                                                                                           (^clojure.lang.IFn iter__30241
                                                                                             (chunk-rest
                                                                                               s__30242)))
                                                                                         (chunk-cons
                                                                                           (chunk
                                                                                             b__30244)
                                                                                           nil)))
                                                                                     (let
                                                                                       [c
                                                                                        (first
                                                                                          s__30242)]
                                                                                       (cons
                                                                                         [:td
                                                                                          (pr-str
                                                                                            c)]
                                                                                         (^clojure.lang.IFn iter__30241
                                                                                           (rest
                                                                                             s__30242)))))))))))]
                                                                      (^clojure.lang.IFn iter__6398__auto__
                                                                        r))]
                                                                   (^clojure.lang.IFn iter__30219
                                                                     (rest s__30220)))))))))))]
                          (^clojure.lang.IFn iter__6398__auto__ result))]
                       [:hr]
                       [:h4 "application/edn"]
                       [:code (pr-str result)])
                     "application/edn"
                     (pr-str result)))))))))))
  (reset-meta! #'query (assoc {:column (int 1)} :name 'query :ns *ns*))
  (defn db-events
    ([storage dbname t]
      (let [temp__5823__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5823__auto__
          (let [c temp__5823__auto__]
            (lib/resource
              :available-media-types
              html-media
              :method-allowed?
              (request-method-in #{:get :head :options})
              :handle-ok
              (fn fn__30268
                ([context]
                  (let [G__30269 (get-in context [:representation :media-type])]
                    (case
                      G__30269
                      "application/xhtml+xml"
                      (let [uri (str "/events/" storage "/" dbname) options__29171__auto__ {}]
                        (binding [hiccup.compiler/*html-mode* :html]
                          (str
                            (#'hiccup.compiler/render-html (hp/doctype :html5))
                            "<html"
                            (#'hiccup.compiler/render-attr-map
                              {:id nil,
                               :class nil,
                               :lang (^clojure.lang.IFn options__29171__auto__ :lang)})
                            ">"
                            (let [attrs30270 (hp/include-css "/css/bootstrap.min.css")]
                              (if (map? attrs30270)
                                (str
                                  "<head"
                                  (#'hiccup.compiler/render-attr-map
                                    (merge {:id nil, :class nil} attrs30270))
                                  ">"
                                  (let [attrs30271 (str
                                                     "var source = new EventSource('/events/"
                                                     storage
                                                     "/"
                                                     dbname
                                                     "');\n\nsource.onmessage = function (event) {\n\n  \tvar ta = document.getElementById('tx-txt');\n\n\tta.value += (event.data + '\\n\\n');\n\n};\n")]
                                    (if (map? attrs30271)
                                      (str
                                        "<script"
                                        (#'hiccup.compiler/render-attr-map
                                          (merge {:id nil, :class nil} attrs30271))
                                        ">"
                                        "</script>")
                                      (str
                                        "<script>"
                                        (#'hiccup.compiler/render-html attrs30271)
                                        "</script>")))
                                  "</head>")
                                (str
                                  "<head>"
                                  (#'hiccup.compiler/render-html attrs30270)
                                  (let [attrs30272 (str
                                                     "var source = new EventSource('/events/"
                                                     storage
                                                     "/"
                                                     dbname
                                                     "');\n\nsource.onmessage = function (event) {\n\n  \tvar ta = document.getElementById('tx-txt');\n\n\tta.value += (event.data + '\\n\\n');\n\n};\n")]
                                    (if (map? attrs30272)
                                      (str
                                        "<script"
                                        (#'hiccup.compiler/render-attr-map
                                          (merge {:id nil, :class nil} attrs30272))
                                        ">"
                                        "</script>")
                                      (str
                                        "<script>"
                                        (#'hiccup.compiler/render-html attrs30272)
                                        "</script>")))
                                  "</head>")))
                            "<body"
                            ""
                            ">"
                            "<div"
                            " class=\"container\""
                            ">"
                            "<h3"
                            ""
                            ">"
                            "<a href=\"../../../..\" rel=\"up\">/</a>"
                            "<a href=\"../../..\" rel=\"up\">data/</a>"
                            "<a"
                            " href=\"../..\" rel=\"up\""
                            ">"
                            (#'hiccup.compiler/render-html storage)
                            "/"
                            "</a>"
                            "<a"
                            " href=\".\" rel=\"up\""
                            ">"
                            (#'hiccup.compiler/render-html dbname)
                            "/"
                            (#'hiccup.compiler/render-html (or t "-"))
                            "/"
                            "</a>"
                            "events"
                            "</h3>"
                            "<p>The service can push <a href=\"https://docs.datomic.com/clojure/index.html#datomic.api/tx-report-queue\" rel=\"help\">transaction reports</a> using <a href=\"http://dev.w3.org/html5/eventsource/\" rel=\"help\"> Server-Sent Events</a>.</p>"
                            "<p"
                            ""
                            ">"
                            "This page embeds a JavaScript element that monitors the following event source: "
                            "<a"
                            (#'hiccup.compiler/render-attr-map
                              {:id nil, :class nil, :rel "monitor", :href uri})
                            ">"
                            (#'hiccup.compiler/render-html uri)
                            "</a>"
                            "</p>"
                            "<p>To consume the queue programmatically you will have to GET the above source, sending the 'Accept: text/event-stream'\n header, per the spec.</p>"
                            "<p>A successful connection returns 200 and leaves the connection open, transmitting heartbeat comment (:) lines and tx reports.</p>"
                            "<p>Each message will consist of a map with :db-before, :db-after and :tx-data</p>"
                            "<textarea cols=\"80\" id=\"tx-txt\" rows=\"24\"></textarea>"
                            "<br>"
                            "</div>"
                            "</body>"
                            "</html>")))
                      "text/html"
                      (let [uri (str "/events/" storage "/" dbname) options__29171__auto__ {}]
                        (binding [hiccup.compiler/*html-mode* :html]
                          (str
                            (#'hiccup.compiler/render-html (hp/doctype :html5))
                            "<html"
                            (#'hiccup.compiler/render-attr-map
                              {:id nil,
                               :class nil,
                               :lang (^clojure.lang.IFn options__29171__auto__ :lang)})
                            ">"
                            (let [attrs30297 (hp/include-css "/css/bootstrap.min.css")]
                              (if (map? attrs30297)
                                (str
                                  "<head"
                                  (#'hiccup.compiler/render-attr-map
                                    (merge {:id nil, :class nil} attrs30297))
                                  ">"
                                  (let [attrs30298 (str
                                                     "var source = new EventSource('/events/"
                                                     storage
                                                     "/"
                                                     dbname
                                                     "');\n\nsource.onmessage = function (event) {\n\n  \tvar ta = document.getElementById('tx-txt');\n\n\tta.value += (event.data + '\\n\\n');\n\n};\n")]
                                    (if (map? attrs30298)
                                      (str
                                        "<script"
                                        (#'hiccup.compiler/render-attr-map
                                          (merge {:id nil, :class nil} attrs30298))
                                        ">"
                                        "</script>")
                                      (str
                                        "<script>"
                                        (#'hiccup.compiler/render-html attrs30298)
                                        "</script>")))
                                  "</head>")
                                (str
                                  "<head>"
                                  (#'hiccup.compiler/render-html attrs30297)
                                  (let [attrs30299 (str
                                                     "var source = new EventSource('/events/"
                                                     storage
                                                     "/"
                                                     dbname
                                                     "');\n\nsource.onmessage = function (event) {\n\n  \tvar ta = document.getElementById('tx-txt');\n\n\tta.value += (event.data + '\\n\\n');\n\n};\n")]
                                    (if (map? attrs30299)
                                      (str
                                        "<script"
                                        (#'hiccup.compiler/render-attr-map
                                          (merge {:id nil, :class nil} attrs30299))
                                        ">"
                                        "</script>")
                                      (str
                                        "<script>"
                                        (#'hiccup.compiler/render-html attrs30299)
                                        "</script>")))
                                  "</head>")))
                            "<body"
                            ""
                            ">"
                            "<div"
                            " class=\"container\""
                            ">"
                            "<h3"
                            ""
                            ">"
                            "<a href=\"../../../..\" rel=\"up\">/</a>"
                            "<a href=\"../../..\" rel=\"up\">data/</a>"
                            "<a"
                            " href=\"../..\" rel=\"up\""
                            ">"
                            (#'hiccup.compiler/render-html storage)
                            "/"
                            "</a>"
                            "<a"
                            " href=\".\" rel=\"up\""
                            ">"
                            (#'hiccup.compiler/render-html dbname)
                            "/"
                            (#'hiccup.compiler/render-html (or t "-"))
                            "/"
                            "</a>"
                            "events"
                            "</h3>"
                            "<p>The service can push <a href=\"https://docs.datomic.com/clojure/index.html#datomic.api/tx-report-queue\" rel=\"help\">transaction reports</a> using <a href=\"http://dev.w3.org/html5/eventsource/\" rel=\"help\"> Server-Sent Events</a>.</p>"
                            "<p"
                            ""
                            ">"
                            "This page embeds a JavaScript element that monitors the following event source: "
                            "<a"
                            (#'hiccup.compiler/render-attr-map
                              {:id nil, :class nil, :rel "monitor", :href uri})
                            ">"
                            (#'hiccup.compiler/render-html uri)
                            "</a>"
                            "</p>"
                            "<p>To consume the queue programmatically you will have to GET the above source, sending the 'Accept: text/event-stream'\n header, per the spec.</p>"
                            "<p>A successful connection returns 200 and leaves the connection open, transmitting heartbeat comment (:) lines and tx reports.</p>"
                            "<p>Each message will consist of a map with :db-before, :db-after and :tx-data</p>"
                            "<textarea cols=\"80\" id=\"tx-txt\" rows=\"24\"></textarea>"
                            "<br>"
                            "</div>"
                            "</body>"
                            "</html>")))))))))
          (lib/resource :available-media-types edn-or-html-media :exists? false)))))
  (reset-meta!
    #'db-events
    (assoc
      {:arglists (clojure.core/list ['storage 'dbname 't]), :column (int 1)}
      :name
      'db-events
      :ns
      *ns*))
  (defn load-resource
    ([req res]
      (let [temp__5823__auto__ (.getResource
                                 (.getContextClassLoader (java.lang.Thread/currentThread))
                                 ^java.lang.String res)]
        (if temp__5823__auto__
          (let [s temp__5823__auto__]
            (assoc-in
              (ring/response (.openStream ^java.net.URL s))
              [:headers "Cache-Control"]
              "max-age=3600"))
          (ring/not-found nil)))))
  (reset-meta!
    #'load-resource
    (assoc
      {:arglists (clojure.core/list ['req 'res]), :column (int 1)}
      :name
      'load-resource
      :ns
      *ns*))
  (defn parse-edn-post-body
    ([h]
      (fn fn__30334
        ([p__30333]
          (let [map__30335 p__30333
                map__30335 (if (seq? map__30335)
                             (if (next map__30335)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__30335))
                               (if (seq map__30335) (first map__30335) {}))
                             map__30335)
                request map__30335
                content_type (get map__30335 :content-type)
                request_method (get map__30335 :request-method)
                body (get map__30335 :body)
                new_request (if (and (= content_type "application/edn") (= request_method :post))
                              (assoc
                                request
                                :params
                                (read-edn body (or (:character-encoding request) "UTF-8")))
                              request)]
            (^clojure.lang.IFn h new_request))))))
  (reset-meta!
    #'parse-edn-post-body
    (assoc
      {:arglists (clojure.core/list ['h]), :column (int 1)}
      :name
      'parse-edn-post-body
      :ns
      *ns*))
  (defn accept-edn-default
    ([h]
      (fn fn__30340
        ([request]
          (^clojure.lang.IFn h
            (assoc-in
              request
              [:headers "accept"]
              (or (get-in request [:headers "accept"]) "application/edn")))))))
  (reset-meta!
    #'accept-edn-default
    (assoc
      {:arglists (clojure.core/list ['h]), :column (int 1)}
      :name
      'accept-edn-default
      :ns
      *ns*))
  (defn log-request
    ([h] (fn fn__30344 ([request] (common/log-and-print request) (^clojure.lang.IFn h request)))))
  (reset-meta!
    #'log-request
    (assoc {:arglists (clojure.core/list ['h]), :column (int 1)} :name 'log-request :ns *ns*))
  (defn remove-nil-params
    ([h]
      (fn fn__30347
        ([request]
          (^clojure.lang.IFn h
            (update-in
              request
              [:params]
              (fn fn__30348 ([param] (into {} (remove (comp nil? val) param))))))))))
  (reset-meta!
    #'remove-nil-params
    (assoc
      {:arglists (clojure.core/list ['h]), :column (int 1)}
      :name
      'remove-nil-params
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.rest" "routes") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.rest" "routes")
    (-> (wrap-cors-request
          (fn fn__30362
            ([req30361]
              (let [segments30360 (m/path-info-segments req30361)]
                (or
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list ""))]
                                             (when temp__5825__auto__
                                               (let [vec__30376 temp__5825__auto__] index)))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "data"
                                                                        ""))]
                                             (when temp__5825__auto__
                                               (let [vec__30379 temp__5825__auto__] stores)))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "api"
                                                                        ""))]
                                             (when temp__5825__auto__
                                               (let [vec__30382 temp__5825__auto__] api)))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "api"
                                                                        "query"))]
                                             (when temp__5825__auto__
                                               (let [vec__30385 temp__5825__auto__] query)))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "data"
                                                                        '_
                                                                        ""))]
                                             (when temp__5825__auto__
                                               (let [vec__30388 temp__5825__auto__
                                                     storage (nth vec__30388 (int 0) nil)]
                                                 (fn fn__30391
                                                   ([p1__30352#]
                                                     ((catalog storage) p1__30352#))))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "data"
                                                                        '_
                                                                        '_
                                                                        ""))]
                                             (when temp__5825__auto__
                                               (let [vec__30393 temp__5825__auto__
                                                     storage (nth vec__30393 (int 0) nil)
                                                     db (nth vec__30393 (int 1) nil)]
                                                 (fn fn__30396
                                                   ([p1__30353#]
                                                     ((db-transact storage db) p1__30353#))))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "data"
                                                                        '_
                                                                        '_
                                                                        '_
                                                                        ""))]
                                             (when temp__5825__auto__
                                               (let [vec__30398 temp__5825__auto__
                                                     storage (nth vec__30398 (int 0) nil)
                                                     db (nth vec__30398 (int 1) nil)
                                                     t (nth vec__30398 (int 2) nil)]
                                                 (fn fn__30401
                                                   ([p1__30354#]
                                                     ((db-info storage db t) p1__30354#))))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "data"
                                                                        '_
                                                                        '_
                                                                        '_
                                                                        "datoms"))]
                                             (when temp__5825__auto__
                                               (let [vec__30403 temp__5825__auto__
                                                     storage (nth vec__30403 (int 0) nil)
                                                     db (nth vec__30403 (int 1) nil)
                                                     t (nth vec__30403 (int 2) nil)]
                                                 (fn fn__30406
                                                   ([p1__30355#]
                                                     ((db-datoms storage db t) p1__30355#))))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "data"
                                                                        '_
                                                                        '_
                                                                        '_
                                                                        "entity"))]
                                             (when temp__5825__auto__
                                               (let [vec__30408 temp__5825__auto__
                                                     storage (nth vec__30408 (int 0) nil)
                                                     db (nth vec__30408 (int 1) nil)
                                                     t (nth vec__30408 (int 2) nil)]
                                                 (fn fn__30411
                                                   ([p1__30356#]
                                                     ((db-entity storage db t) p1__30356#))))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "data"
                                                                        '_
                                                                        '_
                                                                        '_
                                                                        "events"))]
                                             (when temp__5825__auto__
                                               (let [vec__30413 temp__5825__auto__
                                                     storage (nth vec__30413 (int 0) nil)
                                                     db (nth vec__30413 (int 1) nil)
                                                     t (nth vec__30413 (int 2) nil)]
                                                 (fn fn__30416
                                                   ([p1__30357#]
                                                     ((db-events storage db t) p1__30357#))))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list
                                                                        "css"
                                                                        '_))]
                                             (when temp__5825__auto__
                                               (let [vec__30418 temp__5825__auto__
                                                     css (nth vec__30418 (int 0) nil)]
                                                 (fn fn__30421
                                                   ([req__28656__auto__]
                                                     ((let [pred__30422 =
                                                            expr__30423
                                                            (:request-method req__28656__auto__)]
                                                        (if (^clojure.lang.IFn pred__30422
                                                              :get
                                                              expr__30423)
                                                          (fn fn__30424
                                                            ([p1__30358#]
                                                              (load-resource
                                                                p1__30358#
                                                                (str "css/" css))))
                                                          (constantly
                                                            {:headers {"Allow" "GET"},
                                                             :status 405})))
                                                       req__28656__auto__))))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list "js" '_))]
                                             (when temp__5825__auto__
                                               (let [vec__30427 temp__5825__auto__
                                                     js (nth vec__30427 (int 0) nil)]
                                                 (fn fn__30430
                                                   ([req__28656__auto__]
                                                     ((let [pred__30431 =
                                                            expr__30432
                                                            (:request-method req__28656__auto__)]
                                                        (if (^clojure.lang.IFn pred__30431
                                                              :get
                                                              expr__30432)
                                                          (fn fn__30433
                                                            ([p1__30359#]
                                                              (load-resource
                                                                p1__30359#
                                                                (str "js/" js))))
                                                          (constantly
                                                            {:headers {"Allow" "GET"},
                                                             :status 405})))
                                                       req__28656__auto__))))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361))))
                  (let [temp__5825__auto__ (let [temp__5825__auto__ (m/match-route
                                                                      segments30360
                                                                      (clojure.core/list '&))]
                                             (when temp__5825__auto__
                                               (let [vec__30436 temp__5825__auto__
                                                     etc30375 (nth vec__30436 (int 0) nil)]
                                                 (m/alter-request
                                                   m/not-found
                                                   assoc
                                                   :path-info
                                                   (m/uri etc30375)))))]
                    (when temp__5825__auto__
                      (let [handler__28648__auto__ temp__5825__auto__]
                        (^clojure.lang.IFn handler__28648__auto__ req30361)))))))))
     (wrap-cors-preflight)
     (remove-nil-params)
     (parse-edn-post-body)
     (wrap-reading-params)
     (ringkw/wrap-keyword-params)
     (ringp/wrap-params)
     (accept-edn-default)))
  (defn servlet
    ([handler]
      (let [servicer (servlet/make-service-method handler)]
        (proxy
          [javax.servlet.http.HttpServlet]
          []
          (service [request response] (^clojure.lang.IFn servicer this request response))))))
  (reset-meta!
    #'servlet
    (assoc {:arglists (clojure.core/list ['handler]), :column (int 1)} :name 'servlet :ns *ns*))
  (defn service-queue
    ([desc clients]
      (let [vec__30495 (.split ^java.lang.String desc "/")
            storage (nth vec__30495 (int 0) nil)
            dbname (nth vec__30495 (int 1) nil)
            c (conn storage dbname)]
        (when c
          (let [q (d/tx-report-queue c)]
            (future-call
              (fn fn__30498
                ([]
                  (try
                    (do
                      (loop []
                        (do
                          (let [tx_ret (.take ^java.util.concurrent.BlockingQueue q)
                                tx_report (pr-str
                                            (dissoc (prep-tx-ret tx_ret storage dbname) :tempids))]
                            (loop [seq_30499 (seq (get (deref clients) desc))
                                   chunk_30500 nil
                                   count_30501 0
                                   i_30502 0]
                              (if (< i_30502 count_30501)
                                (let [e (.nth ^clojure.lang.Indexed chunk_30500 (int i_30502))]
                                  (try
                                    (do
                                      (.data
                                        ^org.eclipse.jetty.servlets.EventSource$Emitter e
                                        ^java.lang.String tx_report)
                                      nil)
                                    (catch java.lang.Exception ex nil))
                                  (recur seq_30499 chunk_30500 count_30501 (inc i_30502)))
                                (let [temp__5825__auto__ (seq seq_30499)]
                                  (when temp__5825__auto__
                                    (let [seq_30499 temp__5825__auto__]
                                      (if (chunked-seq? seq_30499)
                                        (let [c__6090__auto__ (chunk-first seq_30499)]
                                          (recur
                                            (chunk-rest seq_30499)
                                            c__6090__auto__
                                            (int (count c__6090__auto__))
                                            (int 0)))
                                        (let [e (first seq_30499)]
                                          (try
                                            (do
                                              (.data
                                                ^org.eclipse.jetty.servlets.EventSource$Emitter e
                                                ^java.lang.String tx_report)
                                              nil)
                                            (catch java.lang.Exception ex nil))
                                          (recur (next seq_30499) nil 0 0)))))))))
                          (recur)))
                      nil)
                    (catch
                      java.lang.Throwable
                      t__8765__auto__
                      (do
                        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.rest")
                              ex t__8765__auto__]
                          (when (.isWarnEnabled ^org.slf4j.Logger logger)
                            (.warn
                              ^org.slf4j.Logger logger
                              (datomic.slf4j/process "error executing future")
                              ^java.lang.Throwable ex)
                            (datomic.slf4j/caused-by logger ex))
                          nil)
                        (datomic.monitor/alarm :UnhandledException)
                        (throw ^java.lang.Throwable t__8765__auto__)
                        nil)))))))))))
  (reset-meta!
    #'service-queue
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'desc {:tag 'String}) 'clients]), :column (int 1)}
      :name
      'service-queue
      :ns
      *ns*))
  (defn event-servlet
    ([]
      (let [clients (atom {})
            threads (atom #{})
            ensure_thread (fn ensure_thread
                            ([desc]
                              (when-not (contains? (deref threads) desc)
                                (locking threads
                                 (when-not (contains? (deref threads) desc)
                                   (when (service-queue desc clients)
                                     (swap! threads conj desc)))))))]
        (proxy
          [org.eclipse.jetty.servlets.EventSourceServlet]
          []
          (newEventSource
            [request]
            (let [desc (subs (.getPathInfo ^javax.servlet.http.HttpServletRequest request) 1)
                  vec__30516 (.split ^java.lang.String desc "/")
                  storage (nth vec__30516 (int 0) nil)
                  dbname (nth vec__30516 (int 1) nil)
                  eref (atom nil)]
              (when (try (conn storage dbname) (catch java.lang.Exception ex nil))
                (reify
                  org.eclipse.jetty.servlets.EventSource
                  (^void onClose
                    [this]
                    (do (swap! clients update-in [desc] disj (deref eref)) nil))
                  (^void onOpen
                    [this ^org.eclipse.jetty.servlets.EventSource$Emitter emitter]
                    (do
                      (reset! eref emitter)
                      (swap! clients update-in [desc] (fnil conj #{}) emitter)
                      (^clojure.lang.IFn ensure_thread desc)
                      nil))))))))))
  (reset-meta!
    #'event-servlet
    (assoc {:arglists (clojure.core/list []), :column (int 1)} :name 'event-servlet :ns *ns*))
  (defn start
    ([port]
      (let [s (jetty/create-server {:port (or port 8080), :join? false})
            route_servlet (servlet #'routes)
            context (org.eclipse.jetty.servlet.ServletContextHandler.
                      ^org.eclipse.jetty.server.HandlerContainer s
                      "/")]
        (.addServlet
          ^org.eclipse.jetty.servlet.ServletContextHandler context
          (org.eclipse.jetty.servlet.ServletHolder. (event-servlet))
          "/events/*")
        (.addServlet
          ^org.eclipse.jetty.servlet.ServletContextHandler context
          (org.eclipse.jetty.servlet.ServletHolder. ^javax.servlet.Servlet route_servlet)
          "/")
        (.start ^org.eclipse.jetty.util.component.AbstractLifeCycle s)
        nil)))
  (reset-meta!
    #'start
    (assoc {:arglists (clojure.core/list ['port]), :column (int 1)} :name 'start :ns *ns*))
  (defn -main
    ([& args]
      (let [vec__30530 (cli/cli
                         args
                         ["-o"
                          "--origins"
                          "Comma-delimited list of origins to allow for CORS"
                          :parse-fn
                          (fn fn__30534 ([p1__30528#] (into #{} (str/split p1__30528# #","))))]
                         ["-p"
                          "--port"
                          "Listen on this port"
                          :parse-fn
                          (fn fn__30536
                            ([p1__30529#] (let [n (read-edn p1__30529#)] (when (integer? n) n))))])
            map__30533 (nth vec__30530 (int 0) nil)
            map__30533 (if (seq? map__30533)
                         (if (next map__30533)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__30533))
                           (if (seq map__30533) (first map__30533) {}))
                         map__30533)
            port (get map__30533 :port)
            origins (get map__30533 :origins)
            args (nth vec__30530 (int 1) nil)
            banner (nth vec__30530 (int 2) nil)]
        (reset! whitelist origins)
        (if (or
              (not (number? port))
              (not (even? (java.lang.Integer/valueOf (int (count args)))))
              (not (clojure.lang.Numbers/isPos (long (count args)))))
          (do
            (println
              banner
              "\n Followed by args\n -----------------\n alias1 uri1 alias2 uri2 ...\n where uri is a Datomic db uri with the dbname missing")
            (d/shutdown true)
            -1)
          (let [smap (apply hash-map args)]
            (set-storage-map smap)
            (start port)
            (println "REST API started on port:" port)
            (when (deref whitelist)
              (println "CORS requests allowed from origins: " (deref whitelist)))
            (loop [seq_30538 (seq smap) chunk_30539 nil count_30540 0 i_30541 0]
              (if (< i_30541 count_30540)
                (let [vec__30542 (.nth ^clojure.lang.Indexed chunk_30539 (int i_30541))
                      a (nth vec__30542 (int 0) nil)
                      u (nth vec__30542 (int 1) nil)]
                  (println "  " a "=" u)
                  (recur seq_30538 chunk_30539 count_30540 (inc i_30541)))
                (let [temp__5825__auto__ (seq seq_30538)]
                  (when temp__5825__auto__
                    (let [seq_30538 temp__5825__auto__]
                      (if (chunked-seq? seq_30538)
                        (let [c__6090__auto__ (chunk-first seq_30538)]
                          (recur
                            (chunk-rest seq_30538)
                            c__6090__auto__
                            (int (count c__6090__auto__))
                            (int 0)))
                        (let [vec__30545 (first seq_30538)
                              a (nth vec__30545 (int 0) nil)
                              u (nth vec__30545 (int 1) nil)]
                          (println "  " a "=" u)
                          (recur (next seq_30538) nil 0 0)))))))))))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main :ns *ns*)))