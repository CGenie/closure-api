(ns closure-api.compiler-cache
  (:require digest
            [clojure.java.jdbc :as j]
            [honeysql.core :as sql]
            [honeysql.helpers :refer :all :as helpers])
  (:use [closure-api.compiler :only [js-compile]]))

; Useful read: https://adambard.com/blog/clojure-sql-libs-compared/

(def db-path
  (str (System/getenv "HOME") "/compiler-cache.db"))

(def db-spec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     db-path})

(def timestamp-formatter
  (clj-time.format/formatter "yyyy-MM-dd HH:mm:ss"))

(defn migrate []
  (let [compiler-cache-table
        (j/create-table-ddl :compiler_cache
                            [[:md5 :text "NOT NULL"]
                             [:uri :text "NOT NULL"]
                             [:compiled :text "NOT NULL"]
                             [:timestamp "Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"]])]
    (cond
      (not (.exists (clojure.java.io/as-file db-path)))
      (do
        (j/db-do-commands db-spec [compiler-cache-table])))))

(migrate)

(defn find-uri-code [uri code]
  (let [md5 (digest/md5 code)
        q (-> (select :compiled)
              (from :compiler_cache)
              (where [:= :md5 md5]
                     [:= :uri uri])
              (limit 1)
              sql/format)]
      (-> db-spec (j/query q) first :compiled)))

(defn set-uri-compiled [uri code compiled]
  (let [md5 (digest/md5 code)
        q (-> (insert-into :compiler_cache)
              (columns :md5 :uri :compiled)
              (values
                [[md5 uri compiled]])
              sql/format)]
    (j/execute! db-spec q)))

(defn js-compile-cached
  [uri code]
  (let [result (find-uri-code uri code)]
    (if result
      (do
        (println "compiled result for " uri " found in cache")
        result)
      (let [compiled (js-compile code)]
        (set-uri-compiled uri code compiled)
        compiled))))
