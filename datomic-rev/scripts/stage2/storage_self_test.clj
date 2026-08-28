(ns stage2.storage-self-test
  (:require [datomic.backup :as backup]
            [stage2.storage :as storage])
  (:import [java.nio ByteBuffer]
           [java.nio.file Files LinkOption Path]
           [java.nio.file.attribute FileAttribute]
           [java.util.stream Stream]))

(def ^:private empty-file-attributes
  (make-array FileAttribute 0))

(def ^:private no-follow-link-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- ensure!
  [pred message data]
  (when-not pred
    (throw (ex-info message data))))

(defn- buffer-bytes
  [^ByteBuffer buffer]
  (let [copy (.duplicate buffer)
        result (byte-array (.remaining copy))]
    (.get copy result)
    (vec result)))

(defn- expected-error
  [expected f]
  (try
    (f)
    (throw (ex-info "Expected Stage 2 storage operation to fail"
                    {:expected expected}))
    (catch clojure.lang.ExceptionInfo exception
      (ensure! (= expected (::storage/error (ex-data exception)))
               "Unexpected Stage 2 storage error"
               {:expected expected
                :actual (::storage/error (ex-data exception))
                :data (ex-data exception)}))))

(defn- delete-tree!
  [^Path root]
  ;; This helper receives only the path returned by createTempDirectory below.
  ;; Files/walk does not follow symbolic links.
  (when (Files/exists root no-follow-link-options)
    (with-open [^Stream paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [^Path path (->> (iterator-seq (.iterator paths))
                              (sort (fn [^Path left ^Path right]
                                      (compare (.getNameCount right)
                                               (.getNameCount left)))))]
        (Files/deleteIfExists path)))))

(defn run-self-test
  []
  (let [^Path root (Files/createTempDirectory
                     "datomic-stage2-storage-self-test."
                     empty-file-attributes)]
    (try
      (let [adapter (storage/file-storage root)
            source (ByteBuffer/wrap (byte-array [1 2 3 4]))]
        (.position source 1)
        (.limit source 3)
        (ensure! (= {:k "owner"} (backup/store adapter "owner" source))
                 "store return contract differs"
                 {})
        (ensure! (= 1 (.position source))
                 "store mutated its source ByteBuffer position"
                 {:position (.position source)})
        (ensure! (= [2 3] (buffer-bytes (:v (backup/retrieve adapter "owner"))))
                 "retrieve returned the wrong bytes"
                 {})
        (ensure! (backup/exists? adapter "owner")
                 "stored key does not exist"
                 {})

        (backup/store adapter "values/bb/value-bb" (ByteBuffer/wrap (byte-array [11])))
        (backup/store adapter "roots/100" (ByteBuffer/wrap (byte-array [10])))
        (backup/store adapter "roots/099" (ByteBuffer/wrap (byte-array [9])))
        (ensure! (= {:ks ["roots/099" "roots/100"]}
                    (backup/list-keys adapter "roots/"))
                 "list-keys is not sorted or did not return full keys"
                 {:actual (backup/list-keys adapter "roots/")})

        (let [errors (atom [])
              workers (mapv
                        (fn [n]
                          (Thread.
                            (fn []
                              (try
                                (backup/store
                                  adapter
                                  (str "concurrent/shared/key-" n)
                                  (ByteBuffer/wrap (byte-array [(byte n)])))
                                (catch Throwable throwable
                                  (swap! errors conj throwable))))))
                        (range 16))]
          (run! #(.start ^Thread %) workers)
          (run! #(.join ^Thread %) workers)
          (ensure! (empty? @errors)
                   "concurrent stores sharing a new directory failed"
                   {:exceptions (mapv #(str (class %) ": " (.getMessage ^Throwable %))
                                      @errors)})
          (ensure! (= 16 (count (:ks (backup/list-keys adapter "concurrent/shared/"))))
                   "concurrent stores did not all become visible"
                   {:keys (backup/list-keys adapter "concurrent/shared/")}))

        (expected-error :invalid-key
                        #(backup/store adapter "../escape" (ByteBuffer/wrap (byte-array 0))))
        (expected-error :invalid-key
                        #(backup/retrieve adapter "/absolute"))

        (let [^Path directory (.resolve root "real-directory")
              ^Path link (.resolve root "link-directory")]
          (Files/createDirectory directory empty-file-attributes)
          (Files/createSymbolicLink link directory empty-file-attributes)
          (expected-error
            :symbolic-link
            #(backup/store
               adapter "link-directory/escape" (ByteBuffer/wrap (byte-array [1])))))

        (let [faulty (storage/root-store-failure-storage adapter)]
          (ensure! (= {:k "other"}
                      (backup/store faulty "other" (ByteBuffer/wrap (byte-array [1]))))
                   "root failure wrapper did not delegate ordinary stores"
                   {})
          (expected-error
            :injected-root-store-failure
            #(backup/store faulty "roots/101" (ByteBuffer/wrap (byte-array [1])))))

        (let [missing (storage/missing-retrieve-storage adapter "values/bb/value-bb")]
          (ensure! (nil? (backup/retrieve missing "values/bb/value-bb"))
                   "missing-retrieve wrapper did not inject a miss"
                   {})
          (ensure! (= [2 3] (buffer-bytes (:v (backup/retrieve missing "owner"))))
                   "missing-retrieve wrapper did not delegate other reads"
                   {}))

        (let [uri (storage/stage2-file-uri root)
              reopened (backup/create-storage (str uri))]
          (ensure! (= [2 3] (buffer-bytes (:v (backup/retrieve reopened "owner"))))
                   "stage2-file URI registration did not reopen the storage"
                   {:uri (str uri)}))

        (let [log (storage/operation-log adapter)]
          (ensure! (pos? (get-in log [:counts :store :stored] 0))
                   "operation counters did not record stores"
                   {:log log})
          (ensure! (seq (:events log))
                   "operation log did not record events"
                   {:log log}))

        (println "STAGE2-STORAGE-SELF-TEST-PASS"
                 (pr-str {:root-safety true
                          :symlink-safety true
                          :byte-buffer-duplication true
                          :atomic-store true
                          :sorted-listing true
                          :fault-wrappers true
                          :uri-registration true})))
      (finally
        (delete-tree! root)))))

(defn -main
  [& args]
  (try
    (ensure! (empty? args)
             "Stage 2 storage self-test does not accept arguments"
             {:argument-count (count args)})
    (run-self-test)
    (finally
      (shutdown-agents))))
