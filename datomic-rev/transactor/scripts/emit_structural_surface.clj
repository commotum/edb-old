(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn fail! [message data]
  (binding [*out* *err*]
    (println "STRUCTURAL_SURFACE_FAIL" message (pr-str data)))
  (shutdown-agents)
  (System/exit 2))

(defn read-single-edn [surface-text]
  (with-open [reader (java.io.PushbackReader.
                       (java.io.StringReader. surface-text))]
    (let [eof (Object.)
          surface (edn/read {:eof eof} reader)
          trailing (edn/read {:eof eof} reader)]
      (when (identical? eof surface)
        (throw (ex-info "dedicated payload is empty" {})))
      (when-not (identical? eof trailing)
        (throw (ex-info "dedicated payload has a trailing EDN form"
                        {:trailing trailing})))
      surface)))

(let [[peer-emitter namespace-name surface-file & preload-namespaces]
      *command-line-args*]
  (when-not (and peer-emitter namespace-name surface-file)
    (binding [*out* *err*]
      (println "usage: emit_structural_surface.clj PEER_EMITTER NAMESPACE SURFACE_FILE"))
    (System/exit 2))
  ;; Stage 1 deliberately reuses the Peer recovery's checked-in surface
  ;; definition.  Only process isolation and bounded termination are added.
  (binding [*out* (java.io.StringWriter.)]
    (doseq [preload preload-namespaces]
      (require (symbol preload))))
  ;; The Peer emitter uses Clojure's dynamic *out*, while Jetty/AWS logging
  ;; writes directly to process stdout. Capture only the structural payload,
  ;; validate it as one exact-shape EDN record, and publish it separately.
  ;; Process stdout/stderr remain untouched diagnostic evidence.
  (let [surface-buffer (java.io.StringWriter.)]
    (binding [*command-line-args* [namespace-name]
              *out* surface-buffer]
      (load-file (.getCanonicalPath (io/file peer-emitter))))
    (let [surface-text (str surface-buffer)
          nonempty-lines (remove str/blank? (str/split-lines surface-text))
          surface (try
                    (read-single-edn surface-text)
                    (catch Throwable failure
                      (fail! "dedicated payload is not exactly one EDN form"
                             (merge {:namespace namespace-name
                                     :cause (ex-message failure)}
                                    (ex-data failure)))))]
      (when-not (and (= 1 (count nonempty-lines))
                     (map? surface)
                     (= #{:vars :classes} (set (keys surface)))
                     (map? (:vars surface))
                     (map? (:classes surface)))
        (fail! "dedicated payload has an unexpected shape"
               {:namespace namespace-name
                :nonempty-lines (count nonempty-lines)
                :type (some-> surface class .getName)
                :keys (when (map? surface) (set (keys surface)))
                :vars-map (map? (:vars surface))
                :classes-map (map? (:classes surface))}))
      (with-open [writer (io/writer surface-file :encoding "UTF-8")]
        (.write writer surface-text))))
  (shutdown-agents)
  (System/exit 0))
