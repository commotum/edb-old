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
  (defonce storages (atom nil))
  (defonce whitelist (atom nil))
  (defn set-storage-map ([alias_uri_map] (reset! storages alias_uri_map)))
  (defn read-edn
    ([stm encoding]
      (edn/read
        {:readers *data-readers*}
        (java.io.PushbackReader.
          (java.io.InputStreamReader. ^java.io.InputStream stm ^java.lang.String encoding))))
    ([str] (when-not (empty? str) (edn/read-string {:readers *data-readers*} str))))
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
  (defn conn
    ([storage dbname]
      (let [temp__5804__auto__ (db-uri storage dbname)]
        (when temp__5804__auto__ (let [uri temp__5804__auto__] (d/connect uri))))))
  (defn response
    ([data] (ring/content-type (ring/response (pr-str data)) "application/clojure;charset=UTF-8")))
  (defn tst ([req] (response (:headers req))))
  (defn windowed
    ([db p__29189]
      (let [map__29190 p__29189
            map__29190 (if (seq? map__29190)
                         (if (next map__29190)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__29190))
                           (if (seq map__29190) (first map__29190) {}))
                         map__29190)
            basis_t (get map__29190 :basis-t)
            as_of (get map__29190 :as-of)
            since (get map__29190 :since)
            history (get map__29190 :history)
            db (if (or as_of basis_t) (d/as-of db (or as_of basis_t)) db)
            db (if since (d/since db since) db)
            db (if history (d/history db) db)]
        db)))
  (defn limited
    ([data p__29194]
      (let [map__29195 p__29194
            map__29195 (if (seq? map__29195)
                         (if (next map__29195)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__29195))
                           (if (seq map__29195) (first map__29195) {}))
                         map__29195)
            offset (get map__29195 :offset)
            limit (get map__29195 :limit)
            data (if offset (drop offset data) data)]
        (if limit (take limit data) data))))
  (defn datom->map ([d] (array-map :e (:e d) :a (:a d) :v (:v d) :tx (:tx d) :added (:added d))))
  (defn prep-tx-ret
    ([tx_ret storage dbname]
      (let [db_base #:db{:alias (str storage "/" dbname)}]
        {:db-before (assoc db_base :basis-t (d/basis-t (:db-before tx_ret))),
         :db-after (assoc db_base :basis-t (d/basis-t (:db-after tx_ret))),
         :tx-data (mapv datom->map (:tx-data tx_ret)),
         :tempids (:tempids tx_ret)})))
  (defn wrap-reading-params
    ([handler]
      (fn fn__29201
        ([p__29200]
          (let [map__29202 p__29200
                map__29202 (if (seq? map__29202)
                             (if (next map__29202)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__29202))
                               (if (seq map__29202) (first map__29202) {}))
                             map__29202)
                req map__29202
                content_type (get map__29202 :content-type)
                content_length (get map__29202 :content-length)]
            (^clojure.lang.IFn handler
              (if (or (not= content_type "application/edn") (zero? (or content_length 0)))
                (update-in
                  req
                  [:params]
                  (fn fn__29203
                    ([p1__29199#]
                      (reduce
                        (fn fn__29205
                          ([m p__29204]
                            (let [vec__29206 p__29204
                                  k (nth vec__29206 (int 0) nil)
                                  v (nth vec__29206 (int 1) nil)]
                              (try
                                (assoc m k (read-edn v))
                                (catch
                                  java.lang.Throwable
                                  t
                                  (error/raise
                                    :rest/invalid-params
                                    (str "Unable to read parameter " k)
                                    {:params p1__29199#}
                                    t))))))
                        {}
                        p1__29199#))))
                req)))))))
  (defn matches-origin-whitelist?
    ([whitelist origin]
      (when-not origin
        (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'origin)))))
      (cond
        (= origin "null") false
        (contains? whitelist "*") true
        :default (do (contains? whitelist origin)))))
  (defn wrap-cors-preflight
    ([handler]
      (fn fn__29216
        ([req]
          (if (= :options (:request-method req))
            (let [origin (get-in req [:headers "origin"])]
              {:status 204,
               :headers
               (when (matches-origin-whitelist? (deref whitelist) origin)
                 {"Access-Control-Allow-Origin" origin,
                  "Access-Control-Allow-Headers" "X-Requested-With"})})
            (^clojure.lang.IFn handler req))))))
  (defn wrap-cors-request
    ([handler]
      (fn fn__29219
        ([req]
          (let [resp (^clojure.lang.IFn handler req)
                temp__5802__auto__ (get-in req [:headers "origin"])]
            (if temp__5802__auto__
              (let [origin temp__5802__auto__]
                (if (matches-origin-whitelist? (deref whitelist) origin)
                  (assoc-in resp [:headers "Access-Control-Allow-Origin"] origin)
                  resp))
              resp))))))
  (def html-media ["text/html;q=0.9" "application/xhtml+xml;q=0.8"])
  (def edn-media ["application/edn"])
  (def edn-or-html-media (concat edn-media html-media))
  (defn html5
    ([& forms]
      (let [options__28522__auto__ {}]
        (binding [hiccup.compiler/*html-mode* :html]
          (str
            (#'hiccup.compiler/render-html (hp/doctype :html5))
            "<html"
            (#'hiccup.compiler/render-attr-map
              {:id nil, :class nil, :lang (^clojure.lang.IFn options__28522__auto__ :lang)})
            ">"
            (let [attrs29223 (hp/include-css "/css/bootstrap.min.css")]
              (if (map? attrs29223)
                (str
                  "<head"
                  (#'hiccup.compiler/render-attr-map (merge {:id nil, :class nil} attrs29223))
                  ">"
                  "</head>")
                (str "<head>" (#'hiccup.compiler/render-html attrs29223) "</head>")))
            (#'hiccup.compiler/render-html
              (conj
                [:body]
                (into [:div {:class "container"}] forms)
                (hp/include-js "http://code.jquery.com/jquery-latest.js" "/js/bootstrap.min.js")))
            "</html>")))))
  (defn index
    ([request__29068__auto__]
      (lib/run-resource
        request__29068__auto__
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
               [:li [:a {:rel "item", :href "api/"} "API"]]]))))))
  (defn api
    ([request__29068__auto__]
      (lib/run-resource
        request__29068__auto__
        (lib/get-options
          (clojure.core/list
            :available-media-types
            html-media
            :handle-ok
            (html5
              [:h3 "GET " [:a {:rel "up", :href ".."} "/"] "api/"]
              [:span "Currently the only API is query."]
              [:hr]
              [:ul [:li [:a {:rel "item", :href "query"} "Query"]]]))))))
  (defn request-method-in
    ([method_set]
      (fn fn__29229
        ([p1__29228#] (contains? method_set (:request-method (:request p1__29228#)))))))
  (defn stores
    ([request__29068__auto__]
      (lib/run-resource
        request__29068__auto__
        (lib/get-options
          (clojure.core/list
            :available-media-types
            edn-or-html-media
            :method-allowed?
            (request-method-in #{:get :head :options})
            :handle-ok
            (let [stores (keys (deref storages))]
              (fn fn__29232
                ([context]
                  (let [edn_ret (vec stores)
                        G__29233 (get-in context [:representation :media-type])]
                    (case
                      G__29233
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
                         (let [iter__6373__auto__ (fn iter__29234
                                                    ([s__29235]
                                                      (lazy-seq
                                                        (let [s__29235 s__29235
                                                              temp__5804__auto__ (seq s__29235)]
                                                          (when temp__5804__auto__
                                                            (let 
                                                              [s__29235 temp__5804__auto__]
                                                              (if
                                                                (chunked-seq? s__29235)
                                                                (let 
                                                                  [c__6371__auto__
                                                                   (chunk-first s__29235)
                                                                   size__6372__auto__
                                                                   (int (count c__6371__auto__))
                                                                   b__29237
                                                                   (chunk-buffer
                                                                     (java.lang.Integer/valueOf
                                                                       (int size__6372__auto__)))]
                                                                  (if
                                                                    (loop 
                                                                      [i__29236 (int 0)]
                                                                      (if
                                                                        (<
                                                                          i__29236
                                                                          size__6372__auto__)
                                                                        (let 
                                                                          [s
                                                                           (.nth
                                                                             ^clojure.lang.Indexed c__6371__auto__
                                                                             (int i__29236))]
                                                                          (chunk-append
                                                                            b__29237
                                                                            [:li
                                                                             [:a
                                                                              {:rel "storage",
                                                                               :href (str s "/")}
                                                                              s]])
                                                                          (recur (inc i__29236)))
                                                                        true))
                                                                    (chunk-cons
                                                                      (chunk b__29237)
                                                                      (^clojure.lang.IFn iter__29234
                                                                        (chunk-rest s__29235)))
                                                                    (chunk-cons
                                                                      (chunk b__29237)
                                                                      nil)))
                                                                (let 
                                                                  [s (first s__29235)]
                                                                  (cons
                                                                    [:li
                                                                     [:a
                                                                      {:rel "storage",
                                                                       :href (str s "/")}
                                                                      s]]
                                                                    (^clojure.lang.IFn iter__29234
                                                                      (rest s__29235)))))))))))]
                           (^clojure.lang.IFn iter__6373__auto__ stores))]
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
                         (let [iter__6373__auto__ (fn iter__29247
                                                    ([s__29248]
                                                      (lazy-seq
                                                        (let [s__29248 s__29248
                                                              temp__5804__auto__ (seq s__29248)]
                                                          (when temp__5804__auto__
                                                            (let 
                                                              [s__29248 temp__5804__auto__]
                                                              (if
                                                                (chunked-seq? s__29248)
                                                                (let 
                                                                  [c__6371__auto__
                                                                   (chunk-first s__29248)
                                                                   size__6372__auto__
                                                                   (int (count c__6371__auto__))
                                                                   b__29250
                                                                   (chunk-buffer
                                                                     (java.lang.Integer/valueOf
                                                                       (int size__6372__auto__)))]
                                                                  (if
                                                                    (loop 
                                                                      [i__29249 (int 0)]
                                                                      (if
                                                                        (<
                                                                          i__29249
                                                                          size__6372__auto__)
                                                                        (let 
                                                                          [s
                                                                           (.nth
                                                                             ^clojure.lang.Indexed c__6371__auto__
                                                                             (int i__29249))]
                                                                          (chunk-append
                                                                            b__29250
                                                                            [:li
                                                                             [:a
                                                                              {:rel "storage",
                                                                               :href (str s "/")}
                                                                              s]])
                                                                          (recur (inc i__29249)))
                                                                        true))
                                                                    (chunk-cons
                                                                      (chunk b__29250)
                                                                      (^clojure.lang.IFn iter__29247
                                                                        (chunk-rest s__29248)))
                                                                    (chunk-cons
                                                                      (chunk b__29250)
                                                                      nil)))
                                                                (let 
                                                                  [s (first s__29248)]
                                                                  (cons
                                                                    [:li
                                                                     [:a
                                                                      {:rel "storage",
                                                                       :href (str s "/")}
                                                                      s]]
                                                                    (^clojure.lang.IFn iter__29247
                                                                      (rest s__29248)))))))))))]
                           (^clojure.lang.IFn iter__6373__auto__ stores))]
                        [:hr]
                        [:h4 "application/edn"]
                        [:code (pr-str edn_ret)])
                      "application/edn"
                      (pr-str edn_ret)))))))))))
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
                               G__29265 (get-in context [:representation :media-type])]
                           (case
                             G__29265
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
                                (let [iter__6373__auto__ (fn iter__29266
                                                           ([s__29267]
                                                             (lazy-seq
                                                               (let 
                                                                 [s__29267 s__29267
                                                                  temp__5804__auto__
                                                                  (seq s__29267)]
                                                                 (when
                                                                   temp__5804__auto__
                                                                   (let 
                                                                     [s__29267 temp__5804__auto__]
                                                                     (if
                                                                       (chunked-seq? s__29267)
                                                                       (let 
                                                                         [c__6371__auto__
                                                                          (chunk-first s__29267)
                                                                          size__6372__auto__
                                                                          (int
                                                                            (count
                                                                              c__6371__auto__))
                                                                          b__29269
                                                                          (chunk-buffer
                                                                            (java.lang.Integer/valueOf
                                                                              (int
                                                                                size__6372__auto__)))]
                                                                         (if
                                                                           (loop 
                                                                             [i__29268 (int 0)]
                                                                             (if
                                                                               (<
                                                                                 i__29268
                                                                                 size__6372__auto__)
                                                                               (let 
                                                                                 [db
                                                                                  (.nth
                                                                                    ^clojure.lang.Indexed c__6371__auto__
                                                                                    (int
                                                                                      i__29268))]
                                                                                 (chunk-append
                                                                                   b__29269
                                                                                   [:li
                                                                                    [:a
                                                                                     {:rel "item",
                                                                                      :href
                                                                                      (str
                                                                                        db
                                                                                        "/-/")}
                                                                                     db]])
                                                                                 (recur
                                                                                   (inc i__29268)))
                                                                               true))
                                                                           (chunk-cons
                                                                             (chunk b__29269)
                                                                             (^clojure.lang.IFn iter__29266
                                                                               (chunk-rest
                                                                                 s__29267)))
                                                                           (chunk-cons
                                                                             (chunk b__29269)
                                                                             nil)))
                                                                       (let 
                                                                         [db (first s__29267)]
                                                                         (cons
                                                                           [:li
                                                                            [:a
                                                                             {:rel "item",
                                                                              :href (str db "/-/")}
                                                                             db]]
                                                                           (^clojure.lang.IFn iter__29266
                                                                             (rest
                                                                               s__29267)))))))))))]
                                  (^clojure.lang.IFn iter__6373__auto__ catalog))]
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
                                (let [iter__6373__auto__ (fn iter__29279
                                                           ([s__29280]
                                                             (lazy-seq
                                                               (let 
                                                                 [s__29280 s__29280
                                                                  temp__5804__auto__
                                                                  (seq s__29280)]
                                                                 (when
                                                                   temp__5804__auto__
                                                                   (let 
                                                                     [s__29280 temp__5804__auto__]
                                                                     (if
                                                                       (chunked-seq? s__29280)
                                                                       (let 
                                                                         [c__6371__auto__
                                                                          (chunk-first s__29280)
                                                                          size__6372__auto__
                                                                          (int
                                                                            (count
                                                                              c__6371__auto__))
                                                                          b__29282
                                                                          (chunk-buffer
                                                                            (java.lang.Integer/valueOf
                                                                              (int
                                                                                size__6372__auto__)))]
                                                                         (if
                                                                           (loop 
                                                                             [i__29281 (int 0)]
                                                                             (if
                                                                               (<
                                                                                 i__29281
                                                                                 size__6372__auto__)
                                                                               (let 
                                                                                 [db
                                                                                  (.nth
                                                                                    ^clojure.lang.Indexed c__6371__auto__
                                                                                    (int
                                                                                      i__29281))]
                                                                                 (chunk-append
                                                                                   b__29282
                                                                                   [:li
                                                                                    [:a
                                                                                     {:rel "item",
                                                                                      :href
                                                                                      (str
                                                                                        db
                                                                                        "/-/")}
                                                                                     db]])
                                                                                 (recur
                                                                                   (inc i__29281)))
                                                                               true))
                                                                           (chunk-cons
                                                                             (chunk b__29282)
                                                                             (^clojure.lang.IFn iter__29279
                                                                               (chunk-rest
                                                                                 s__29280)))
                                                                           (chunk-cons
                                                                             (chunk b__29282)
                                                                             nil)))
                                                                       (let 
                                                                         [db (first s__29280)]
                                                                         (cons
                                                                           [:li
                                                                            [:a
                                                                             {:rel "item",
                                                                              :href (str db "/-/")}
                                                                             db]]
                                                                           (^clojure.lang.IFn iter__29279
                                                                             (rest
                                                                               s__29280)))))))))))]
                                  (^clojure.lang.IFn iter__6373__auto__ catalog))]
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
            (fn fn__29295
              ([ctx]
                (let [dbn (get-in ctx [:request :params (keyword db_name)])
                      uri (db-uri storage dbn)]
                  (swap! created (fn fn__29296 ([_] (d/create-database uri)))))))
            :new?
            (fn fn__29299 ([_] (boolean (deref created))))
            :respond-with-entity?
            true
            :malformed?
            (fn fn__29301
              ([ctx]
                (and
                  ((request-method-in #{:post}) ctx)
                  (not (get-in ctx [:request :params (keyword db_name)])))))
            :handle-created
            handle
            :handle-ok
            handle))
        (lib/resource :available-media-types edn-or-html-media :exists? false))))
  (defn datoms-table
    ([id datoms ecell]
      [:table
       {:id id, :class "table"}
       [:tr [:th "e"] [:th "a"] [:th "v"] [:th "tx"] [:th "added"]]
       (let [iter__6373__auto__ (fn iter__29305
                                  ([s__29306]
                                    (lazy-seq
                                      (let [s__29306 s__29306 temp__5804__auto__ (seq s__29306)]
                                        (when temp__5804__auto__
                                          (let [s__29306 temp__5804__auto__]
                                            (if (chunked-seq? s__29306)
                                              (let [c__6371__auto__ (chunk-first s__29306)
                                                    size__6372__auto__ (int
                                                                         (count c__6371__auto__))
                                                    b__29308 (chunk-buffer
                                                               (java.lang.Integer/valueOf
                                                                 (int size__6372__auto__)))]
                                                (if (loop [i__29307 (int 0)]
                                                      (if (< i__29307 size__6372__auto__)
                                                        (let [map__29312
                                                              (.nth
                                                                ^clojure.lang.Indexed c__6371__auto__
                                                                (int i__29307))
                                                              map__29312
                                                              (if
                                                                (seq? map__29312)
                                                                (if
                                                                  (next map__29312)
                                                                  (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                    (to-array map__29312))
                                                                  (if
                                                                    (seq map__29312)
                                                                    (first map__29312)
                                                                    {}))
                                                                map__29312)
                                                              e (get map__29312 :e)
                                                              a (get map__29312 :a)
                                                              v (get map__29312 :v)
                                                              tx (get map__29312 :tx)
                                                              added (get map__29312 :added)]
                                                          (chunk-append
                                                            b__29308
                                                            [:tr
                                                             (^clojure.lang.IFn ecell e)
                                                             (^clojure.lang.IFn ecell a)
                                                             [:td (pr-str v)]
                                                             (^clojure.lang.IFn ecell tx)
                                                             [:td (str added)]])
                                                          (recur (inc i__29307)))
                                                        true))
                                                  (chunk-cons
                                                    (chunk b__29308)
                                                    (^clojure.lang.IFn iter__29305
                                                      (chunk-rest s__29306)))
                                                  (chunk-cons (chunk b__29308) nil)))
                                              (let [map__29314 (first s__29306)
                                                    map__29314 (if
                                                                 (seq? map__29314)
                                                                 (if
                                                                   (next map__29314)
                                                                   (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                                                     (to-array map__29314))
                                                                   (if
                                                                     (seq map__29314)
                                                                     (first map__29314)
                                                                     {}))
                                                                 map__29314)
                                                    e (get map__29314 :e)
                                                    a (get map__29314 :a)
                                                    v (get map__29314 :v)
                                                    tx (get map__29314 :tx)
                                                    added (get map__29314 :added)]
                                                (cons
                                                  [:tr
                                                   (^clojure.lang.IFn ecell e)
                                                   (^clojure.lang.IFn ecell a)
                                                   [:td (pr-str v)]
                                                   (^clojure.lang.IFn ecell tx)
                                                   [:td (str added)]]
                                                  (^clojure.lang.IFn iter__29305
                                                    (rest s__29306)))))))))))]
         (^clojure.lang.IFn iter__6373__auto__ datoms))]))
  (defn db-transact
    ([storage dbname]
      (let [temp__5802__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5802__auto__
          (let [c temp__5802__auto__ created (atom nil)]
            (lib/resource
              :available-media-types
              edn-or-html-media
              :method-allowed?
              (request-method-in #{:post :options})
              :post!
              (fn fn__29324
                ([ctx]
                  (let [txd (get-in ctx [:request :params :tx-data])
                        tx_ret (deref (d/transact c txd))]
                    (swap! created (fn fn__29325 ([_] (prep-tx-ret tx_ret storage dbname)))))))
              :new?
              (fn fn__29328 ([_] (boolean (deref created))))
              :respond-with-entity?
              true
              :malformed?
              (fn fn__29330
                ([ctx]
                  (and
                    ((request-method-in #{:post}) ctx)
                    (not (get-in ctx [:request :params :tx-data])))))
              :handle-created
              (fn fn__29333
                ([context]
                  (let [edn_ret (deref created)
                        G__29334 (get-in context [:representation :media-type])]
                    (case
                      G__29334
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
                           (let [iter__6373__auto__ (fn iter__29338
                                                      ([s__29339]
                                                        (lazy-seq
                                                          (let [s__29339 s__29339
                                                                temp__5804__auto__ (seq s__29339)]
                                                            (when
                                                              temp__5804__auto__
                                                              (let 
                                                                [s__29339 temp__5804__auto__]
                                                                (if
                                                                  (chunked-seq? s__29339)
                                                                  (let 
                                                                    [c__6371__auto__
                                                                     (chunk-first s__29339)
                                                                     size__6372__auto__
                                                                     (int (count c__6371__auto__))
                                                                     b__29341
                                                                     (chunk-buffer
                                                                       (java.lang.Integer/valueOf
                                                                         (int
                                                                           size__6372__auto__)))]
                                                                    (if
                                                                      (loop 
                                                                        [i__29340 (int 0)]
                                                                        (if
                                                                          (<
                                                                            i__29340
                                                                            size__6372__auto__)
                                                                          (let 
                                                                            [vec__29345
                                                                             (.nth
                                                                               ^clojure.lang.Indexed c__6371__auto__
                                                                               (int i__29340))
                                                                             tid
                                                                             (nth
                                                                               vec__29345
                                                                               (int 0)
                                                                               nil)
                                                                             eid
                                                                             (nth
                                                                               vec__29345
                                                                               (int 1)
                                                                               nil)]
                                                                            (chunk-append
                                                                              b__29341
                                                                              [:tr
                                                                               [:td (pr-str tid)]
                                                                               (^clojure.lang.IFn ecell
                                                                                 eid)])
                                                                            (recur (inc i__29340)))
                                                                          true))
                                                                      (chunk-cons
                                                                        (chunk b__29341)
                                                                        (^clojure.lang.IFn iter__29338
                                                                          (chunk-rest s__29339)))
                                                                      (chunk-cons
                                                                        (chunk b__29341)
                                                                        nil)))
                                                                  (let 
                                                                    [vec__29349 (first s__29339)
                                                                     tid
                                                                     (nth vec__29349 (int 0) nil)
                                                                     eid
                                                                     (nth vec__29349 (int 1) nil)]
                                                                    (cons
                                                                      [:tr
                                                                       [:td (pr-str tid)]
                                                                       (^clojure.lang.IFn ecell
                                                                         eid)]
                                                                      (^clojure.lang.IFn iter__29338
                                                                        (rest s__29339)))))))))))]
                             (^clojure.lang.IFn iter__6373__auto__ (:tempids tx_ret)))]
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
                           (let [iter__6373__auto__ (fn iter__29360
                                                      ([s__29361]
                                                        (lazy-seq
                                                          (let [s__29361 s__29361
                                                                temp__5804__auto__ (seq s__29361)]
                                                            (when
                                                              temp__5804__auto__
                                                              (let 
                                                                [s__29361 temp__5804__auto__]
                                                                (if
                                                                  (chunked-seq? s__29361)
                                                                  (let 
                                                                    [c__6371__auto__
                                                                     (chunk-first s__29361)
                                                                     size__6372__auto__
                                                                     (int (count c__6371__auto__))
                                                                     b__29363
                                                                     (chunk-buffer
                                                                       (java.lang.Integer/valueOf
                                                                         (int
                                                                           size__6372__auto__)))]
                                                                    (if
                                                                      (loop 
                                                                        [i__29362 (int 0)]
                                                                        (if
                                                                          (<
                                                                            i__29362
                                                                            size__6372__auto__)
                                                                          (let 
                                                                            [vec__29367
                                                                             (.nth
                                                                               ^clojure.lang.Indexed c__6371__auto__
                                                                               (int i__29362))
                                                                             tid
                                                                             (nth
                                                                               vec__29367
                                                                               (int 0)
                                                                               nil)
                                                                             eid
                                                                             (nth
                                                                               vec__29367
                                                                               (int 1)
                                                                               nil)]
                                                                            (chunk-append
                                                                              b__29363
                                                                              [:tr
                                                                               [:td (pr-str tid)]
                                                                               (^clojure.lang.IFn ecell
                                                                                 eid)])
                                                                            (recur (inc i__29362)))
                                                                          true))
                                                                      (chunk-cons
                                                                        (chunk b__29363)
                                                                        (^clojure.lang.IFn iter__29360
                                                                          (chunk-rest s__29361)))
                                                                      (chunk-cons
                                                                        (chunk b__29363)
                                                                        nil)))
                                                                  (let 
                                                                    [vec__29371 (first s__29361)
                                                                     tid
                                                                     (nth vec__29371 (int 0) nil)
                                                                     eid
                                                                     (nth vec__29371 (int 1) nil)]
                                                                    (cons
                                                                      [:tr
                                                                       [:td (pr-str tid)]
                                                                       (^clojure.lang.IFn ecell
                                                                         eid)]
                                                                      (^clojure.lang.IFn iter__29360
                                                                        (rest s__29361)))))))))))]
                             (^clojure.lang.IFn iter__6373__auto__ (:tempids tx_ret)))]
                          [:h4 "txdata"]
                          [:p "The datoms created by the transaction"]
                          (datoms-table "txdata" (:tx-data tx_ret) ecell)
                          [:hr]
                          [:h4 "application/edn"]
                          [:code (pr-str edn_ret)]))
                      "application/edn"
                      (pr-str edn_ret)))))))
          (lib/resource :available-media-types edn-or-html-media :exists? false)))))
  (defn db-info
    ([storage dbname t]
      (let [temp__5802__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5802__auto__
          (let [c temp__5802__auto__ created (atom nil)]
            (lib/resource
              :available-media-types
              edn-or-html-media
              :method-allowed?
              (request-method-in #{:get :head :options})
              :handle-ok
              (fn fn__29386
                ([context]
                  (let [db (d/db c)
                        tt (if (= t "-") (d/basis-t db) (read-edn t))
                        edn_ret {:db/alias (str storage "/" dbname), :basis-t tt}
                        G__29387 (get-in context [:representation :media-type])]
                    (case
                      G__29387
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
  (defn get-datoms
    ([params db]
      (let [map__29393 params
            map__29393 (if (seq? map__29393)
                         (if (next map__29393)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__29393))
                           (if (seq map__29393) (first map__29393) {}))
                         map__29393)
            e (get map__29393 :e)
            a (get map__29393 :a)
            v (get map__29393 :v)
            index (get map__29393 :index)
            index (keyword index)
            db (windowed db params)
            args (let [G__29394 index]
                   (case G__29394 :aevt [a e v] :avet [a v e] :eavt [e a v] :vaet [v a e]))]
        (mapv
          datom->map
          (limited (apply d/datoms db index (take-while (complement nil?) args)) params)))))
  (defn get-range
    ([params db]
      (let [map__29396 params
            map__29396 (if (seq? map__29396)
                         (if (next map__29396)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__29396))
                           (if (seq map__29396) (first map__29396) {}))
                         map__29396)
            a (get map__29396 :a)
            start (get map__29396 :start)
            end (get map__29396 :end)
            db (windowed db params)]
        (mapv datom->map (limited (d/index-range db a start end) params)))))
  (defn db-datoms
    ([storage dbname t]
      (let [temp__5802__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5802__auto__
          (let [c temp__5802__auto__ created (atom nil)]
            (lib/resource
              :available-media-types
              edn-or-html-media
              :method-allowed?
              (request-method-in #{:get :head :options})
              :handle-ok
              (fn fn__29401
                ([context]
                  (let [params (:params (:request context))
                        map__29402 params
                        map__29402 (if (seq? map__29402)
                                     (if (next map__29402)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__29402))
                                       (if (seq map__29402) (first map__29402) {}))
                                     map__29402)
                        end (get map__29402 :end)
                        a (get map__29402 :a)
                        since (get map__29402 :since)
                        v (get map__29402 :v)
                        limit (get map__29402 :limit)
                        index (get map__29402 :index)
                        offset (get map__29402 :offset)
                        start (get map__29402 :start)
                        history (get map__29402 :history)
                        e (get map__29402 :e)
                        as_of (get map__29402 :as-of)
                        db (d/db c)
                        params (if (= t "-") params (assoc params :basis-t (read-edn t)))
                        p (fn p ([p1__29398#] (when p1__29398# (pr-str p1__29398#))))
                        datoms (when index
                                 ((if (and (= index 'avet) a (or start end)) get-range get-datoms)
                                   params
                                   db))
                        G__29405 (get-in context [:representation :media-type])]
                    (case
                      G__29405
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
  (defn db-entity
    ([storage dbname t]
      (let [temp__5802__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5802__auto__
          (let [c temp__5802__auto__ created (atom nil)]
            (lib/resource
              :available-media-types
              edn-or-html-media
              :method-allowed?
              (request-method-in #{:get :head :options})
              :handle-ok
              (fn fn__29426
                ([context]
                  (let [params (:params (:request context))
                        map__29427 params
                        map__29427 (if (seq? map__29427)
                                     (if (next map__29427)
                                       (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                         (to-array map__29427))
                                       (if (seq map__29427) (first map__29427) {}))
                                     map__29427)
                        e (get map__29427 :e)
                        as_of (get map__29427 :as-of)
                        since (get map__29427 :since)
                        db (d/db c)
                        params (if (= t "-") params (assoc params :basis-t (read-edn t)))
                        p (fn p ([p1__29420#] (when p1__29420# (pr-str p1__29420#))))
                        emap (when e (d/touch (d/entity (windowed db params) e)))
                        elink (fn elink
                                ([eid]
                                  (let [eid (db/resolve-id db eid)]
                                    [:a
                                     {:rel "entity", :href (str "./entity?e=" eid)}
                                     (pr-str (or (d/ident db eid) eid))])))
                        amany? (fn amany_QMARK_
                                 ([p1__29421#]
                                   (=
                                     36
                                     (.-cardinality
                                       (.elementAt
                                         ^datomic.db.IDbImpl db
                                         (db/resolve-id db p1__29421#))))))
                        ref? (fn ref_QMARK_
                               ([p1__29422#]
                                 (=
                                   20
                                   (.-vtypeid
                                     (.elementAt
                                       ^datomic.db.IDbImpl db
                                       (db/resolve-id db p1__29422#))))))
                        component? (fn component_QMARK_
                                     ([p1__29423#]
                                       (.-isComponent
                                         (.elementAt
                                           ^datomic.db.IDbImpl db
                                           (db/resolve-id db p1__29423#)))))
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
                                      (let [iter__6373__auto__ (fn 
                                                                 iter__29442
                                                                 ([s__29443]
                                                                   (lazy-seq
                                                                     (let 
                                                                       [s__29443 s__29443
                                                                        temp__5804__auto__
                                                                        (seq s__29443)]
                                                                       (when
                                                                         temp__5804__auto__
                                                                         (let 
                                                                           [s__29443
                                                                            temp__5804__auto__]
                                                                           (if
                                                                             (chunked-seq?
                                                                               s__29443)
                                                                             (let 
                                                                               [c__6371__auto__
                                                                                (chunk-first
                                                                                  s__29443)
                                                                                size__6372__auto__
                                                                                (int
                                                                                  (count
                                                                                    c__6371__auto__))
                                                                                b__29445
                                                                                (chunk-buffer
                                                                                  (java.lang.Integer/valueOf
                                                                                    (int
                                                                                      size__6372__auto__)))]
                                                                               (if
                                                                                 (loop 
                                                                                   [i__29444
                                                                                    (int 0)]
                                                                                   (if
                                                                                     (<
                                                                                       i__29444
                                                                                       size__6372__auto__)
                                                                                     (let 
                                                                                       [vec__29449
                                                                                        (.nth
                                                                                          ^clojure.lang.Indexed c__6371__auto__
                                                                                          (int
                                                                                            i__29444))
                                                                                        a
                                                                                        (nth
                                                                                          vec__29449
                                                                                          (int 0)
                                                                                          nil)
                                                                                        v
                                                                                        (nth
                                                                                          vec__29449
                                                                                          (int 1)
                                                                                          nil)]
                                                                                       (chunk-append
                                                                                         b__29445
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
                                                                                                 [iter__6373__auto__
                                                                                                  (fn 
                                                                                                    iter__29452
                                                                                                    ([s__29453]
                                                                                                      (lazy-seq
                                                                                                        (let 
                                                                                                          [s__29453
                                                                                                           s__29453
                                                                                                           temp__5804__auto__
                                                                                                           (seq
                                                                                                             s__29453)]
                                                                                                          (when
                                                                                                            temp__5804__auto__
                                                                                                            (let 
                                                                                                              [s__29453
                                                                                                               temp__5804__auto__]
                                                                                                              (if
                                                                                                                (chunked-seq?
                                                                                                                  s__29453)
                                                                                                                (let 
                                                                                                                  [c__6371__auto__
                                                                                                                   (chunk-first
                                                                                                                     s__29453)
                                                                                                                   size__6372__auto__
                                                                                                                   (int
                                                                                                                     (count
                                                                                                                       c__6371__auto__))
                                                                                                                   b__29455
                                                                                                                   (chunk-buffer
                                                                                                                     (java.lang.Integer/valueOf
                                                                                                                       (int
                                                                                                                         size__6372__auto__)))]
                                                                                                                  (if
                                                                                                                    (loop 
                                                                                                                      [i__29454
                                                                                                                       (int
                                                                                                                         0)]
                                                                                                                      (if
                                                                                                                        (<
                                                                                                                          i__29454
                                                                                                                          size__6372__auto__)
                                                                                                                        (let 
                                                                                                                          [v
                                                                                                                           (.nth
                                                                                                                             ^clojure.lang.Indexed c__6371__auto__
                                                                                                                             (int
                                                                                                                               i__29454))]
                                                                                                                          (chunk-append
                                                                                                                            b__29455
                                                                                                                            [:tr
                                                                                                                             [:td]
                                                                                                                             [:td
                                                                                                                              (^clojure.lang.IFn vcell
                                                                                                                                a
                                                                                                                                v)]])
                                                                                                                          (recur
                                                                                                                            (inc
                                                                                                                              i__29454)))
                                                                                                                        true))
                                                                                                                    (chunk-cons
                                                                                                                      (chunk
                                                                                                                        b__29455)
                                                                                                                      (^clojure.lang.IFn iter__29452
                                                                                                                        (chunk-rest
                                                                                                                          s__29453)))
                                                                                                                    (chunk-cons
                                                                                                                      (chunk
                                                                                                                        b__29455)
                                                                                                                      nil)))
                                                                                                                (let 
                                                                                                                  [v
                                                                                                                   (first
                                                                                                                     s__29453)]
                                                                                                                  (cons
                                                                                                                    [:tr
                                                                                                                     [:td]
                                                                                                                     [:td
                                                                                                                      (^clojure.lang.IFn vcell
                                                                                                                        a
                                                                                                                        v)]]
                                                                                                                    (^clojure.lang.IFn iter__29452
                                                                                                                      (rest
                                                                                                                        s__29453)))))))))))]
                                                                                                 (^clojure.lang.IFn iter__6373__auto__
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
                                                                                           i__29444)))
                                                                                     true))
                                                                                 (chunk-cons
                                                                                   (chunk b__29445)
                                                                                   (^clojure.lang.IFn iter__29442
                                                                                     (chunk-rest
                                                                                       s__29443)))
                                                                                 (chunk-cons
                                                                                   (chunk b__29445)
                                                                                   nil)))
                                                                             (let 
                                                                               [vec__29467
                                                                                (first s__29443)
                                                                                a
                                                                                (nth
                                                                                  vec__29467
                                                                                  (int 0)
                                                                                  nil)
                                                                                v
                                                                                (nth
                                                                                  vec__29467
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
                                                                                         [iter__6373__auto__
                                                                                          (fn 
                                                                                            iter__29470
                                                                                            ([s__29471]
                                                                                              (lazy-seq
                                                                                                (let 
                                                                                                  [s__29471
                                                                                                   s__29471
                                                                                                   temp__5804__auto__
                                                                                                   (seq
                                                                                                     s__29471)]
                                                                                                  (when
                                                                                                    temp__5804__auto__
                                                                                                    (let 
                                                                                                      [s__29471
                                                                                                       temp__5804__auto__]
                                                                                                      (if
                                                                                                        (chunked-seq?
                                                                                                          s__29471)
                                                                                                        (let 
                                                                                                          [c__6371__auto__
                                                                                                           (chunk-first
                                                                                                             s__29471)
                                                                                                           size__6372__auto__
                                                                                                           (int
                                                                                                             (count
                                                                                                               c__6371__auto__))
                                                                                                           b__29473
                                                                                                           (chunk-buffer
                                                                                                             (java.lang.Integer/valueOf
                                                                                                               (int
                                                                                                                 size__6372__auto__)))]
                                                                                                          (if
                                                                                                            (loop 
                                                                                                              [i__29472
                                                                                                               (int
                                                                                                                 0)]
                                                                                                              (if
                                                                                                                (<
                                                                                                                  i__29472
                                                                                                                  size__6372__auto__)
                                                                                                                (let 
                                                                                                                  [v
                                                                                                                   (.nth
                                                                                                                     ^clojure.lang.Indexed c__6371__auto__
                                                                                                                     (int
                                                                                                                       i__29472))]
                                                                                                                  (chunk-append
                                                                                                                    b__29473
                                                                                                                    [:tr
                                                                                                                     [:td]
                                                                                                                     [:td
                                                                                                                      (^clojure.lang.IFn vcell
                                                                                                                        a
                                                                                                                        v)]])
                                                                                                                  (recur
                                                                                                                    (inc
                                                                                                                      i__29472)))
                                                                                                                true))
                                                                                                            (chunk-cons
                                                                                                              (chunk
                                                                                                                b__29473)
                                                                                                              (^clojure.lang.IFn iter__29470
                                                                                                                (chunk-rest
                                                                                                                  s__29471)))
                                                                                                            (chunk-cons
                                                                                                              (chunk
                                                                                                                b__29473)
                                                                                                              nil)))
                                                                                                        (let 
                                                                                                          [v
                                                                                                           (first
                                                                                                             s__29471)]
                                                                                                          (cons
                                                                                                            [:tr
                                                                                                             [:td]
                                                                                                             [:td
                                                                                                              (^clojure.lang.IFn vcell
                                                                                                                a
                                                                                                                v)]]
                                                                                                            (^clojure.lang.IFn iter__29470
                                                                                                              (rest
                                                                                                                s__29471)))))))))))]
                                                                                         (^clojure.lang.IFn iter__6373__auto__
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
                                                                                 (^clojure.lang.IFn iter__29442
                                                                                   (rest
                                                                                     s__29443)))))))))))]
                                        (^clojure.lang.IFn iter__6373__auto__ emap))])))
                        G__29491 (get-in context [:representation :media-type])]
                    (case
                      G__29491
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
  (defn query
    ([request__29068__auto__]
      (lib/run-resource
        request__29068__auto__
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
            (fn fn__29498
              ([context]
                (let [params (:params (:request context))
                      map__29499 params
                      map__29499 (if (seq? map__29499)
                                   (if (next map__29499)
                                     (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                       (to-array map__29499))
                                     (if (seq map__29499) (first map__29499) {}))
                                   map__29499)
                      q (get map__29499 :q)
                      args (get map__29499 :args)
                      offset (get map__29499 :offset)
                      limit (get map__29499 :limit)
                      oq q
                      q (if (sequential? q) (dq/listq->mapq q) q)
                      xargs (when args
                              (mapv
                                (fn fn__29500
                                  ([p1__29497#]
                                    (if (:db/alias p1__29497#)
                                      (let [dbsym (symbol (:db/alias p1__29497#))
                                            storage (namespace dbsym)
                                            dbname (name dbsym)
                                            c (conn storage dbname)]
                                        (when-not c
                                          (throw
                                            (java.lang.AssertionError.
                                              (str
                                                "Assert failed: "
                                                (str "Can't find db: " (:db/alias p1__29497#))
                                                "\n"
                                                (pr-str 'c)))))
                                        (windowed (d/db c) p1__29497#))
                                      p1__29497#)))
                                args))
                      result (if q (vec (limited (apply d/q q xargs) params)) [])
                      G__29502 (get-in context [:representation :media-type])]
                  (case
                    G__29502
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
                        (let [iter__6373__auto__ (fn iter__29503
                                                   ([s__29504]
                                                     (lazy-seq
                                                       (let [s__29504 s__29504
                                                             temp__5804__auto__ (seq s__29504)]
                                                         (when temp__5804__auto__
                                                           (let 
                                                             [s__29504 temp__5804__auto__]
                                                             (if
                                                               (chunked-seq? s__29504)
                                                               (let 
                                                                 [c__6371__auto__
                                                                  (chunk-first s__29504)
                                                                  size__6372__auto__
                                                                  (int (count c__6371__auto__))
                                                                  b__29506
                                                                  (chunk-buffer
                                                                    (java.lang.Integer/valueOf
                                                                      (int size__6372__auto__)))]
                                                                 (if
                                                                   (loop 
                                                                     [i__29505 (int 0)]
                                                                     (if
                                                                       (<
                                                                         i__29505
                                                                         size__6372__auto__)
                                                                       (let 
                                                                         [bind
                                                                          (.nth
                                                                            ^clojure.lang.Indexed c__6371__auto__
                                                                            (int i__29505))]
                                                                         (chunk-append
                                                                           b__29506
                                                                           [:th (str bind)])
                                                                         (recur (inc i__29505)))
                                                                       true))
                                                                   (chunk-cons
                                                                     (chunk b__29506)
                                                                     (^clojure.lang.IFn iter__29503
                                                                       (chunk-rest s__29504)))
                                                                   (chunk-cons
                                                                     (chunk b__29506)
                                                                     nil)))
                                                               (let 
                                                                 [bind (first s__29504)]
                                                                 (cons
                                                                   [:th (str bind)]
                                                                   (^clojure.lang.IFn iter__29503
                                                                     (rest s__29504)))))))))))]
                          (^clojure.lang.IFn iter__6373__auto__ (:find q)))]
                       (let [iter__6373__auto__ (fn iter__29516
                                                  ([s__29517]
                                                    (lazy-seq
                                                      (let [s__29517 s__29517
                                                            temp__5804__auto__ (seq s__29517)]
                                                        (when temp__5804__auto__
                                                          (let [s__29517 temp__5804__auto__]
                                                            (if
                                                              (chunked-seq? s__29517)
                                                              (let 
                                                                [c__6371__auto__
                                                                 (chunk-first s__29517)
                                                                 size__6372__auto__
                                                                 (int (count c__6371__auto__))
                                                                 b__29519
                                                                 (chunk-buffer
                                                                   (java.lang.Integer/valueOf
                                                                     (int size__6372__auto__)))]
                                                                (if
                                                                  (loop 
                                                                    [i__29518 (int 0)]
                                                                    (if
                                                                      (<
                                                                        i__29518
                                                                        size__6372__auto__)
                                                                      (let 
                                                                        [r
                                                                         (.nth
                                                                           ^clojure.lang.Indexed c__6371__auto__
                                                                           (int i__29518))]
                                                                        (chunk-append
                                                                          b__29519
                                                                          [:tr
                                                                           (let 
                                                                             [iter__6373__auto__
                                                                              (fn 
                                                                                iter__29523
                                                                                ([s__29524]
                                                                                  (lazy-seq
                                                                                    (let 
                                                                                      [s__29524
                                                                                       s__29524
                                                                                       temp__5804__auto__
                                                                                       (seq
                                                                                         s__29524)]
                                                                                      (when
                                                                                        temp__5804__auto__
                                                                                        (let 
                                                                                          [s__29524
                                                                                           temp__5804__auto__]
                                                                                          (if
                                                                                            (chunked-seq?
                                                                                              s__29524)
                                                                                            (let 
                                                                                              [c__6371__auto__
                                                                                               (chunk-first
                                                                                                 s__29524)
                                                                                               size__6372__auto__
                                                                                               (int
                                                                                                 (count
                                                                                                   c__6371__auto__))
                                                                                               b__29526
                                                                                               (chunk-buffer
                                                                                                 (java.lang.Integer/valueOf
                                                                                                   (int
                                                                                                     size__6372__auto__)))]
                                                                                              (if
                                                                                                (loop 
                                                                                                  [i__29525
                                                                                                   (int
                                                                                                     0)]
                                                                                                  (if
                                                                                                    (<
                                                                                                      i__29525
                                                                                                      size__6372__auto__)
                                                                                                    (let 
                                                                                                      [c
                                                                                                       (.nth
                                                                                                         ^clojure.lang.Indexed c__6371__auto__
                                                                                                         (int
                                                                                                           i__29525))]
                                                                                                      (chunk-append
                                                                                                        b__29526
                                                                                                        [:td
                                                                                                         (pr-str
                                                                                                           c)])
                                                                                                      (recur
                                                                                                        (inc
                                                                                                          i__29525)))
                                                                                                    true))
                                                                                                (chunk-cons
                                                                                                  (chunk
                                                                                                    b__29526)
                                                                                                  (^clojure.lang.IFn iter__29523
                                                                                                    (chunk-rest
                                                                                                      s__29524)))
                                                                                                (chunk-cons
                                                                                                  (chunk
                                                                                                    b__29526)
                                                                                                  nil)))
                                                                                            (let 
                                                                                              [c
                                                                                               (first
                                                                                                 s__29524)]
                                                                                              (cons
                                                                                                [:td
                                                                                                 (pr-str
                                                                                                   c)]
                                                                                                (^clojure.lang.IFn iter__29523
                                                                                                  (rest
                                                                                                    s__29524)))))))))))]
                                                                             (^clojure.lang.IFn iter__6373__auto__
                                                                               r))])
                                                                        (recur (inc i__29518)))
                                                                      true))
                                                                  (chunk-cons
                                                                    (chunk b__29519)
                                                                    (^clojure.lang.IFn iter__29516
                                                                      (chunk-rest s__29517)))
                                                                  (chunk-cons
                                                                    (chunk b__29519)
                                                                    nil)))
                                                              (let 
                                                                [r (first s__29517)]
                                                                (cons
                                                                  [:tr
                                                                   (let 
                                                                     [iter__6373__auto__
                                                                      (fn 
                                                                        iter__29538
                                                                        ([s__29539]
                                                                          (lazy-seq
                                                                            (let 
                                                                              [s__29539 s__29539
                                                                               temp__5804__auto__
                                                                               (seq s__29539)]
                                                                              (when
                                                                                temp__5804__auto__
                                                                                (let 
                                                                                  [s__29539
                                                                                   temp__5804__auto__]
                                                                                  (if
                                                                                    (chunked-seq?
                                                                                      s__29539)
                                                                                    (let 
                                                                                      [c__6371__auto__
                                                                                       (chunk-first
                                                                                         s__29539)
                                                                                       size__6372__auto__
                                                                                       (int
                                                                                         (count
                                                                                           c__6371__auto__))
                                                                                       b__29541
                                                                                       (chunk-buffer
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             size__6372__auto__)))]
                                                                                      (if
                                                                                        (loop 
                                                                                          [i__29540
                                                                                           (int 0)]
                                                                                          (if
                                                                                            (<
                                                                                              i__29540
                                                                                              size__6372__auto__)
                                                                                            (let 
                                                                                              [c
                                                                                               (.nth
                                                                                                 ^clojure.lang.Indexed c__6371__auto__
                                                                                                 (int
                                                                                                   i__29540))]
                                                                                              (chunk-append
                                                                                                b__29541
                                                                                                [:td
                                                                                                 (pr-str
                                                                                                   c)])
                                                                                              (recur
                                                                                                (inc
                                                                                                  i__29540)))
                                                                                            true))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__29541)
                                                                                          (^clojure.lang.IFn iter__29538
                                                                                            (chunk-rest
                                                                                              s__29539)))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__29541)
                                                                                          nil)))
                                                                                    (let 
                                                                                      [c
                                                                                       (first
                                                                                         s__29539)]
                                                                                      (cons
                                                                                        [:td
                                                                                         (pr-str
                                                                                           c)]
                                                                                        (^clojure.lang.IFn iter__29538
                                                                                          (rest
                                                                                            s__29539)))))))))))]
                                                                     (^clojure.lang.IFn iter__6373__auto__
                                                                       r))]
                                                                  (^clojure.lang.IFn iter__29516
                                                                    (rest s__29517)))))))))))]
                         (^clojure.lang.IFn iter__6373__auto__ result))]
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
                        (let [iter__6373__auto__ (fn iter__29557
                                                   ([s__29558]
                                                     (lazy-seq
                                                       (let [s__29558 s__29558
                                                             temp__5804__auto__ (seq s__29558)]
                                                         (when temp__5804__auto__
                                                           (let 
                                                             [s__29558 temp__5804__auto__]
                                                             (if
                                                               (chunked-seq? s__29558)
                                                               (let 
                                                                 [c__6371__auto__
                                                                  (chunk-first s__29558)
                                                                  size__6372__auto__
                                                                  (int (count c__6371__auto__))
                                                                  b__29560
                                                                  (chunk-buffer
                                                                    (java.lang.Integer/valueOf
                                                                      (int size__6372__auto__)))]
                                                                 (if
                                                                   (loop 
                                                                     [i__29559 (int 0)]
                                                                     (if
                                                                       (<
                                                                         i__29559
                                                                         size__6372__auto__)
                                                                       (let 
                                                                         [bind
                                                                          (.nth
                                                                            ^clojure.lang.Indexed c__6371__auto__
                                                                            (int i__29559))]
                                                                         (chunk-append
                                                                           b__29560
                                                                           [:th (str bind)])
                                                                         (recur (inc i__29559)))
                                                                       true))
                                                                   (chunk-cons
                                                                     (chunk b__29560)
                                                                     (^clojure.lang.IFn iter__29557
                                                                       (chunk-rest s__29558)))
                                                                   (chunk-cons
                                                                     (chunk b__29560)
                                                                     nil)))
                                                               (let 
                                                                 [bind (first s__29558)]
                                                                 (cons
                                                                   [:th (str bind)]
                                                                   (^clojure.lang.IFn iter__29557
                                                                     (rest s__29558)))))))))))]
                          (^clojure.lang.IFn iter__6373__auto__ (:find q)))]
                       (let [iter__6373__auto__ (fn iter__29570
                                                  ([s__29571]
                                                    (lazy-seq
                                                      (let [s__29571 s__29571
                                                            temp__5804__auto__ (seq s__29571)]
                                                        (when temp__5804__auto__
                                                          (let [s__29571 temp__5804__auto__]
                                                            (if
                                                              (chunked-seq? s__29571)
                                                              (let 
                                                                [c__6371__auto__
                                                                 (chunk-first s__29571)
                                                                 size__6372__auto__
                                                                 (int (count c__6371__auto__))
                                                                 b__29573
                                                                 (chunk-buffer
                                                                   (java.lang.Integer/valueOf
                                                                     (int size__6372__auto__)))]
                                                                (if
                                                                  (loop 
                                                                    [i__29572 (int 0)]
                                                                    (if
                                                                      (<
                                                                        i__29572
                                                                        size__6372__auto__)
                                                                      (let 
                                                                        [r
                                                                         (.nth
                                                                           ^clojure.lang.Indexed c__6371__auto__
                                                                           (int i__29572))]
                                                                        (chunk-append
                                                                          b__29573
                                                                          [:tr
                                                                           (let 
                                                                             [iter__6373__auto__
                                                                              (fn 
                                                                                iter__29577
                                                                                ([s__29578]
                                                                                  (lazy-seq
                                                                                    (let 
                                                                                      [s__29578
                                                                                       s__29578
                                                                                       temp__5804__auto__
                                                                                       (seq
                                                                                         s__29578)]
                                                                                      (when
                                                                                        temp__5804__auto__
                                                                                        (let 
                                                                                          [s__29578
                                                                                           temp__5804__auto__]
                                                                                          (if
                                                                                            (chunked-seq?
                                                                                              s__29578)
                                                                                            (let 
                                                                                              [c__6371__auto__
                                                                                               (chunk-first
                                                                                                 s__29578)
                                                                                               size__6372__auto__
                                                                                               (int
                                                                                                 (count
                                                                                                   c__6371__auto__))
                                                                                               b__29580
                                                                                               (chunk-buffer
                                                                                                 (java.lang.Integer/valueOf
                                                                                                   (int
                                                                                                     size__6372__auto__)))]
                                                                                              (if
                                                                                                (loop 
                                                                                                  [i__29579
                                                                                                   (int
                                                                                                     0)]
                                                                                                  (if
                                                                                                    (<
                                                                                                      i__29579
                                                                                                      size__6372__auto__)
                                                                                                    (let 
                                                                                                      [c
                                                                                                       (.nth
                                                                                                         ^clojure.lang.Indexed c__6371__auto__
                                                                                                         (int
                                                                                                           i__29579))]
                                                                                                      (chunk-append
                                                                                                        b__29580
                                                                                                        [:td
                                                                                                         (pr-str
                                                                                                           c)])
                                                                                                      (recur
                                                                                                        (inc
                                                                                                          i__29579)))
                                                                                                    true))
                                                                                                (chunk-cons
                                                                                                  (chunk
                                                                                                    b__29580)
                                                                                                  (^clojure.lang.IFn iter__29577
                                                                                                    (chunk-rest
                                                                                                      s__29578)))
                                                                                                (chunk-cons
                                                                                                  (chunk
                                                                                                    b__29580)
                                                                                                  nil)))
                                                                                            (let 
                                                                                              [c
                                                                                               (first
                                                                                                 s__29578)]
                                                                                              (cons
                                                                                                [:td
                                                                                                 (pr-str
                                                                                                   c)]
                                                                                                (^clojure.lang.IFn iter__29577
                                                                                                  (rest
                                                                                                    s__29578)))))))))))]
                                                                             (^clojure.lang.IFn iter__6373__auto__
                                                                               r))])
                                                                        (recur (inc i__29572)))
                                                                      true))
                                                                  (chunk-cons
                                                                    (chunk b__29573)
                                                                    (^clojure.lang.IFn iter__29570
                                                                      (chunk-rest s__29571)))
                                                                  (chunk-cons
                                                                    (chunk b__29573)
                                                                    nil)))
                                                              (let 
                                                                [r (first s__29571)]
                                                                (cons
                                                                  [:tr
                                                                   (let 
                                                                     [iter__6373__auto__
                                                                      (fn 
                                                                        iter__29592
                                                                        ([s__29593]
                                                                          (lazy-seq
                                                                            (let 
                                                                              [s__29593 s__29593
                                                                               temp__5804__auto__
                                                                               (seq s__29593)]
                                                                              (when
                                                                                temp__5804__auto__
                                                                                (let 
                                                                                  [s__29593
                                                                                   temp__5804__auto__]
                                                                                  (if
                                                                                    (chunked-seq?
                                                                                      s__29593)
                                                                                    (let 
                                                                                      [c__6371__auto__
                                                                                       (chunk-first
                                                                                         s__29593)
                                                                                       size__6372__auto__
                                                                                       (int
                                                                                         (count
                                                                                           c__6371__auto__))
                                                                                       b__29595
                                                                                       (chunk-buffer
                                                                                         (java.lang.Integer/valueOf
                                                                                           (int
                                                                                             size__6372__auto__)))]
                                                                                      (if
                                                                                        (loop 
                                                                                          [i__29594
                                                                                           (int 0)]
                                                                                          (if
                                                                                            (<
                                                                                              i__29594
                                                                                              size__6372__auto__)
                                                                                            (let 
                                                                                              [c
                                                                                               (.nth
                                                                                                 ^clojure.lang.Indexed c__6371__auto__
                                                                                                 (int
                                                                                                   i__29594))]
                                                                                              (chunk-append
                                                                                                b__29595
                                                                                                [:td
                                                                                                 (pr-str
                                                                                                   c)])
                                                                                              (recur
                                                                                                (inc
                                                                                                  i__29594)))
                                                                                            true))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__29595)
                                                                                          (^clojure.lang.IFn iter__29592
                                                                                            (chunk-rest
                                                                                              s__29593)))
                                                                                        (chunk-cons
                                                                                          (chunk
                                                                                            b__29595)
                                                                                          nil)))
                                                                                    (let 
                                                                                      [c
                                                                                       (first
                                                                                         s__29593)]
                                                                                      (cons
                                                                                        [:td
                                                                                         (pr-str
                                                                                           c)]
                                                                                        (^clojure.lang.IFn iter__29592
                                                                                          (rest
                                                                                            s__29593)))))))))))]
                                                                     (^clojure.lang.IFn iter__6373__auto__
                                                                       r))]
                                                                  (^clojure.lang.IFn iter__29570
                                                                    (rest s__29571)))))))))))]
                         (^clojure.lang.IFn iter__6373__auto__ result))]
                      [:hr]
                      [:h4 "application/edn"]
                      [:code (pr-str result)])
                    "application/edn"
                    (pr-str result))))))))))
  (defn db-events
    ([storage dbname t]
      (let [temp__5802__auto__ (try (conn storage dbname) (catch java.lang.Exception ex nil))]
        (if temp__5802__auto__
          (let [c temp__5802__auto__]
            (lib/resource
              :available-media-types
              html-media
              :method-allowed?
              (request-method-in #{:get :head :options})
              :handle-ok
              (fn fn__29619
                ([context]
                  (let [G__29620 (get-in context [:representation :media-type])]
                    (case
                      G__29620
                      "application/xhtml+xml"
                      (let [uri (str "/events/" storage "/" dbname) options__28522__auto__ {}]
                        (binding [hiccup.compiler/*html-mode* :html]
                          (str
                            (#'hiccup.compiler/render-html (hp/doctype :html5))
                            "<html"
                            (#'hiccup.compiler/render-attr-map
                              {:id nil,
                               :class nil,
                               :lang (^clojure.lang.IFn options__28522__auto__ :lang)})
                            ">"
                            (let [attrs29621 (hp/include-css "/css/bootstrap.min.css")]
                              (if (map? attrs29621)
                                (str
                                  "<head"
                                  (#'hiccup.compiler/render-attr-map
                                    (merge {:id nil, :class nil} attrs29621))
                                  ">"
                                  (let [attrs29622 (str
                                                     "var source = new EventSource('/events/"
                                                     storage
                                                     "/"
                                                     dbname
                                                     "');\n\nsource.onmessage = function (event) {\n\n  \tvar ta = document.getElementById('tx-txt');\n\n\tta.value += (event.data + '\\n\\n');\n\n};\n")]
                                    (if (map? attrs29622)
                                      (str
                                        "<script"
                                        (#'hiccup.compiler/render-attr-map
                                          (merge {:id nil, :class nil} attrs29622))
                                        ">"
                                        "</script>")
                                      (str
                                        "<script>"
                                        (#'hiccup.compiler/render-html attrs29622)
                                        "</script>")))
                                  "</head>")
                                (str
                                  "<head>"
                                  (#'hiccup.compiler/render-html attrs29621)
                                  (let [attrs29623 (str
                                                     "var source = new EventSource('/events/"
                                                     storage
                                                     "/"
                                                     dbname
                                                     "');\n\nsource.onmessage = function (event) {\n\n  \tvar ta = document.getElementById('tx-txt');\n\n\tta.value += (event.data + '\\n\\n');\n\n};\n")]
                                    (if (map? attrs29623)
                                      (str
                                        "<script"
                                        (#'hiccup.compiler/render-attr-map
                                          (merge {:id nil, :class nil} attrs29623))
                                        ">"
                                        "</script>")
                                      (str
                                        "<script>"
                                        (#'hiccup.compiler/render-html attrs29623)
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
                      (let [uri (str "/events/" storage "/" dbname) options__28522__auto__ {}]
                        (binding [hiccup.compiler/*html-mode* :html]
                          (str
                            (#'hiccup.compiler/render-html (hp/doctype :html5))
                            "<html"
                            (#'hiccup.compiler/render-attr-map
                              {:id nil,
                               :class nil,
                               :lang (^clojure.lang.IFn options__28522__auto__ :lang)})
                            ">"
                            (let [attrs29648 (hp/include-css "/css/bootstrap.min.css")]
                              (if (map? attrs29648)
                                (str
                                  "<head"
                                  (#'hiccup.compiler/render-attr-map
                                    (merge {:id nil, :class nil} attrs29648))
                                  ">"
                                  (let [attrs29649 (str
                                                     "var source = new EventSource('/events/"
                                                     storage
                                                     "/"
                                                     dbname
                                                     "');\n\nsource.onmessage = function (event) {\n\n  \tvar ta = document.getElementById('tx-txt');\n\n\tta.value += (event.data + '\\n\\n');\n\n};\n")]
                                    (if (map? attrs29649)
                                      (str
                                        "<script"
                                        (#'hiccup.compiler/render-attr-map
                                          (merge {:id nil, :class nil} attrs29649))
                                        ">"
                                        "</script>")
                                      (str
                                        "<script>"
                                        (#'hiccup.compiler/render-html attrs29649)
                                        "</script>")))
                                  "</head>")
                                (str
                                  "<head>"
                                  (#'hiccup.compiler/render-html attrs29648)
                                  (let [attrs29650 (str
                                                     "var source = new EventSource('/events/"
                                                     storage
                                                     "/"
                                                     dbname
                                                     "');\n\nsource.onmessage = function (event) {\n\n  \tvar ta = document.getElementById('tx-txt');\n\n\tta.value += (event.data + '\\n\\n');\n\n};\n")]
                                    (if (map? attrs29650)
                                      (str
                                        "<script"
                                        (#'hiccup.compiler/render-attr-map
                                          (merge {:id nil, :class nil} attrs29650))
                                        ">"
                                        "</script>")
                                      (str
                                        "<script>"
                                        (#'hiccup.compiler/render-html attrs29650)
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
  (defn load-resource
    ([req res]
      (let [temp__5802__auto__ (.getResource
                                 (.getContextClassLoader (java.lang.Thread/currentThread))
                                 ^java.lang.String res)]
        (if temp__5802__auto__
          (let [s temp__5802__auto__]
            (assoc-in
              (ring/response (.openStream ^java.net.URL s))
              [:headers "Cache-Control"]
              "max-age=3600"))
          (ring/not-found nil)))))
  (defn parse-edn-post-body
    ([h]
      (fn fn__29685
        ([p__29684]
          (let [map__29686 p__29684
                map__29686 (if (seq? map__29686)
                             (if (next map__29686)
                               (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                 (to-array map__29686))
                               (if (seq map__29686) (first map__29686) {}))
                             map__29686)
                request map__29686
                content_type (get map__29686 :content-type)
                request_method (get map__29686 :request-method)
                body (get map__29686 :body)
                new_request (if (and (= content_type "application/edn") (= request_method :post))
                              (assoc
                                request
                                :params
                                (read-edn body (or (:character-encoding request) "UTF-8")))
                              request)]
            (^clojure.lang.IFn h new_request))))))
  (defn accept-edn-default
    ([h]
      (fn fn__29691
        ([request]
          (^clojure.lang.IFn h
            (assoc-in
              request
              [:headers "accept"]
              (or (get-in request [:headers "accept"]) "application/edn")))))))
  (defn log-request
    ([h] (fn fn__29695 ([request] (common/log-and-print request) (^clojure.lang.IFn h request)))))
  (defn remove-nil-params
    ([h]
      (fn fn__29698
        ([request]
          (^clojure.lang.IFn h
            (update-in
              request
              [:params]
              (fn fn__29699 ([param] (into {} (remove (comp nil? val) param))))))))))
  (def routes
   (-> (wrap-cors-request
         (fn fn__29713
           ([req29712]
             (let [segments29711 (m/path-info-segments req29712)]
               (or
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list ""))]
                                            (when temp__5804__auto__
                                              (let [vec__29727 temp__5804__auto__] index)))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list
                                                                       "data"
                                                                       ""))]
                                            (when temp__5804__auto__
                                              (let [vec__29730 temp__5804__auto__] stores)))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list "api" ""))]
                                            (when temp__5804__auto__
                                              (let [vec__29733 temp__5804__auto__] api)))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list
                                                                       "api"
                                                                       "query"))]
                                            (when temp__5804__auto__
                                              (let [vec__29736 temp__5804__auto__] query)))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list
                                                                       "data"
                                                                       '_
                                                                       ""))]
                                            (when temp__5804__auto__
                                              (let [vec__29739 temp__5804__auto__
                                                    storage (nth vec__29739 (int 0) nil)]
                                                (fn fn__29742
                                                  ([p1__29703#]
                                                    ((catalog storage) p1__29703#))))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list
                                                                       "data"
                                                                       '_
                                                                       '_
                                                                       ""))]
                                            (when temp__5804__auto__
                                              (let [vec__29744 temp__5804__auto__
                                                    storage (nth vec__29744 (int 0) nil)
                                                    db (nth vec__29744 (int 1) nil)]
                                                (fn fn__29747
                                                  ([p1__29704#]
                                                    ((db-transact storage db) p1__29704#))))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list
                                                                       "data"
                                                                       '_
                                                                       '_
                                                                       '_
                                                                       ""))]
                                            (when temp__5804__auto__
                                              (let [vec__29749 temp__5804__auto__
                                                    storage (nth vec__29749 (int 0) nil)
                                                    db (nth vec__29749 (int 1) nil)
                                                    t (nth vec__29749 (int 2) nil)]
                                                (fn fn__29752
                                                  ([p1__29705#]
                                                    ((db-info storage db t) p1__29705#))))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list
                                                                       "data"
                                                                       '_
                                                                       '_
                                                                       '_
                                                                       "datoms"))]
                                            (when temp__5804__auto__
                                              (let [vec__29754 temp__5804__auto__
                                                    storage (nth vec__29754 (int 0) nil)
                                                    db (nth vec__29754 (int 1) nil)
                                                    t (nth vec__29754 (int 2) nil)]
                                                (fn fn__29757
                                                  ([p1__29706#]
                                                    ((db-datoms storage db t) p1__29706#))))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list
                                                                       "data"
                                                                       '_
                                                                       '_
                                                                       '_
                                                                       "entity"))]
                                            (when temp__5804__auto__
                                              (let [vec__29759 temp__5804__auto__
                                                    storage (nth vec__29759 (int 0) nil)
                                                    db (nth vec__29759 (int 1) nil)
                                                    t (nth vec__29759 (int 2) nil)]
                                                (fn fn__29762
                                                  ([p1__29707#]
                                                    ((db-entity storage db t) p1__29707#))))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list
                                                                       "data"
                                                                       '_
                                                                       '_
                                                                       '_
                                                                       "events"))]
                                            (when temp__5804__auto__
                                              (let [vec__29764 temp__5804__auto__
                                                    storage (nth vec__29764 (int 0) nil)
                                                    db (nth vec__29764 (int 1) nil)
                                                    t (nth vec__29764 (int 2) nil)]
                                                (fn fn__29767
                                                  ([p1__29708#]
                                                    ((db-events storage db t) p1__29708#))))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list "css" '_))]
                                            (when temp__5804__auto__
                                              (let [vec__29769 temp__5804__auto__
                                                    css (nth vec__29769 (int 0) nil)]
                                                (fn fn__29772
                                                  ([req__28007__auto__]
                                                    ((let [pred__29773 =
                                                           expr__29774 (:request-method
                                                                         req__28007__auto__)]
                                                       (if (^clojure.lang.IFn pred__29773
                                                             :get
                                                             expr__29774)
                                                         (fn fn__29775
                                                           ([p1__29709#]
                                                             (load-resource
                                                               p1__29709#
                                                               (str "css/" css))))
                                                         (constantly
                                                           {:headers {"Allow" "GET"},
                                                            :status 405})))
                                                      req__28007__auto__))))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list "js" '_))]
                                            (when temp__5804__auto__
                                              (let [vec__29778 temp__5804__auto__
                                                    js (nth vec__29778 (int 0) nil)]
                                                (fn fn__29781
                                                  ([req__28007__auto__]
                                                    ((let [pred__29782 =
                                                           expr__29783 (:request-method
                                                                         req__28007__auto__)]
                                                       (if (^clojure.lang.IFn pred__29782
                                                             :get
                                                             expr__29783)
                                                         (fn fn__29784
                                                           ([p1__29710#]
                                                             (load-resource
                                                               p1__29710#
                                                               (str "js/" js))))
                                                         (constantly
                                                           {:headers {"Allow" "GET"},
                                                            :status 405})))
                                                      req__28007__auto__))))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712))))
                 (let [temp__5804__auto__ (let [temp__5804__auto__ (m/match-route
                                                                     segments29711
                                                                     (clojure.core/list '&))]
                                            (when temp__5804__auto__
                                              (let [vec__29787 temp__5804__auto__
                                                    etc29726 (nth vec__29787 (int 0) nil)]
                                                (m/alter-request
                                                  m/not-found
                                                  assoc
                                                  :path-info
                                                  (m/uri etc29726)))))]
                   (when temp__5804__auto__
                     (let [handler__27999__auto__ temp__5804__auto__]
                       (^clojure.lang.IFn handler__27999__auto__ req29712)))))))))
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
  (defn service-queue
    ([desc clients]
      (let [vec__29846 (.split ^java.lang.String desc "/")
            storage (nth vec__29846 (int 0) nil)
            dbname (nth vec__29846 (int 1) nil)
            c (conn storage dbname)]
        (when c
          (let [q (d/tx-report-queue c)]
            (future-call
              (fn fn__29849
                ([]
                  (try
                    (do
                      (loop []
                        (do
                          (let [tx_ret (.take ^java.util.concurrent.BlockingQueue q)
                                tx_report (pr-str
                                            (dissoc (prep-tx-ret tx_ret storage dbname) :tempids))]
                            (loop [seq_29850 (seq (get (deref clients) desc))
                                   chunk_29851 nil
                                   count_29852 0
                                   i_29853 0]
                              (if (< i_29853 count_29852)
                                (let [e (.nth ^clojure.lang.Indexed chunk_29851 (int i_29853))]
                                  (try
                                    (do
                                      (.data
                                        ^org.eclipse.jetty.servlets.EventSource$Emitter e
                                        ^java.lang.String tx_report)
                                      nil)
                                    (catch java.lang.Exception ex nil))
                                  (recur seq_29850 chunk_29851 count_29852 (inc i_29853)))
                                (let [temp__5804__auto__ (seq seq_29850)]
                                  (when temp__5804__auto__
                                    (let [seq_29850 temp__5804__auto__]
                                      (if (chunked-seq? seq_29850)
                                        (let [c__6065__auto__ (chunk-first seq_29850)]
                                          (recur
                                            (chunk-rest seq_29850)
                                            c__6065__auto__
                                            (int (count c__6065__auto__))
                                            (int 0)))
                                        (let [e (first seq_29850)]
                                          (try
                                            (do
                                              (.data
                                                ^org.eclipse.jetty.servlets.EventSource$Emitter e
                                                ^java.lang.String tx_report)
                                              nil)
                                            (catch java.lang.Exception ex nil))
                                          (recur (next seq_29850) nil 0 0)))))))))
                          (recur)))
                      nil)
                    (catch
                      java.lang.Throwable
                      t__8829__auto__
                      (do
                        (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.rest")
                              ex t__8829__auto__]
                          (when (.isWarnEnabled ^org.slf4j.Logger logger)
                            (.warn
                              ^org.slf4j.Logger logger
                              (datomic.slf4j/process "error executing future")
                              ^java.lang.Throwable ex)
                            (datomic.slf4j/caused-by logger ex))
                          nil)
                        (datomic.monitor/alarm :UnhandledException)
                        (throw ^java.lang.Throwable t__8829__auto__)
                        nil)))))))))))
  (defn event-servlet
    ([]
      (let [clients (atom {})
            threads (atom #{})
            ensure_thread (fn ensure_thread
                            ([desc]
                              (when-not (contains? (deref threads) desc)
                                (let [lockee__5782__auto__ threads
                                      locklocal__5783__auto__ lockee__5782__auto__]
                                  (monitor-enter locklocal__5783__auto__)
                                  (try
                                    (when-not (contains? (deref threads) desc)
                                      (when (service-queue desc clients)
                                        (swap! threads conj desc)))
                                    (finally (do (monitor-exit locklocal__5783__auto__) nil)))))))]
        (proxy
          [org.eclipse.jetty.servlets.EventSourceServlet]
          []
          (newEventSource
            [request]
            (let [desc (subs (.getPathInfo ^javax.servlet.http.HttpServletRequest request) 1)
                  vec__29867 (.split ^java.lang.String desc "/")
                  storage (nth vec__29867 (int 0) nil)
                  dbname (nth vec__29867 (int 1) nil)
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
  (defn -main
    ([& args]
      (let [vec__29881 (cli/cli
                         args
                         ["-o"
                          "--origins"
                          "Comma-delimited list of origins to allow for CORS"
                          :parse-fn
                          (fn fn__29885 ([p1__29879#] (into #{} (str/split p1__29879# #","))))]
                         ["-p"
                          "--port"
                          "Listen on this port"
                          :parse-fn
                          (fn fn__29887
                            ([p1__29880#] (let [n (read-edn p1__29880#)] (when (integer? n) n))))])
            map__29884 (nth vec__29881 (int 0) nil)
            map__29884 (if (seq? map__29884)
                         (if (next map__29884)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__29884))
                           (if (seq map__29884) (first map__29884) {}))
                         map__29884)
            port (get map__29884 :port)
            origins (get map__29884 :origins)
            args (nth vec__29881 (int 1) nil)
            banner (nth vec__29881 (int 2) nil)]
        (reset! whitelist origins)
        (if (or
              (not (number? port))
              (not (even? (java.lang.Integer/valueOf (int (count args)))))
              (not (clojure.lang.Numbers/isPos (long (count args)))))
          (do
            (println
              banner
              "\n Followed by args\n -----------------\n alias1 uri1 alias2 uri2 ... \n where uri is a Datomic db uri with the dbname missing")
            (d/shutdown true)
            -1)
          (let [smap (apply hash-map args)]
            (set-storage-map smap)
            (start port)
            (println "REST API started on port:" port)
            (when (deref whitelist)
              (println "CORS requests allowed from origins: " (deref whitelist)))
            (loop [seq_29889 (seq smap) chunk_29890 nil count_29891 0 i_29892 0]
              (if (< i_29892 count_29891)
                (let [vec__29893 (.nth ^clojure.lang.Indexed chunk_29890 (int i_29892))
                      a (nth vec__29893 (int 0) nil)
                      u (nth vec__29893 (int 1) nil)]
                  (println "  " a "=" u)
                  (recur seq_29889 chunk_29890 count_29891 (inc i_29892)))
                (let [temp__5804__auto__ (seq seq_29889)]
                  (when temp__5804__auto__
                    (let [seq_29889 temp__5804__auto__]
                      (if (chunked-seq? seq_29889)
                        (let [c__6065__auto__ (chunk-first seq_29889)]
                          (recur
                            (chunk-rest seq_29889)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [vec__29896 (first seq_29889)
                              a (nth vec__29896 (int 0) nil)
                              u (nth vec__29896 (int 1) nil)]
                          (println "  " a "=" u)
                          (recur (next seq_29889) nil 0 0))))))))))))))