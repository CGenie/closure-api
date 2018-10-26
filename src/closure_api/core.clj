(ns closure-api.core
  (:require [clj-http.client :as http]
            [org.httpkit.server :as server]
            [environ.core :refer [env]])
  (:use [closure-api.compiler-cache :only [js-compile-cached]]
        [ring.middleware.params :only [wrap-params]]))

; Useful read: https://exupero.org/hazard/post/clojure-proxy/

; NOTE fetch from env
(def default-host (env :default-host))  ;"http://odoo:8069"
(def default-passthru (env :default-passthru))

(defn build-url [host path query-string]
  (let [url (.toString (java.net.URL. (java.net.URL. host) path))]
    (if (not-empty query-string)
      (str url "?" query-string)
      url)))

(defn build-url-from-req [req]
  (let [{:keys [host uri query-string]
         :or {host default-host}} req]
    (build-url host uri query-string)))

; The middleware that we will use
(defn proxy-handler [req]
  (let [{:keys [request-method body headers]} req]
    (http/request {:url (build-url-from-req req)
                   :method request-method
                   :body body
                   :headers (dissoc headers "content-length")
                   :throw-exceptions false
                   :decompress-body false})))
                                        ;:as :stream}))))

(defn wrap-compile [handler]
  (fn [req]
    (let [res (handler req)
          uri (:uri req)
          params (:params req)
          compile-off (or (params "_passthru") default-passthru)
          status (:status res)]
      (println "res-headers" (:headers res))
      (println "params " params)
      (println "compile-off " compile-off)
      (println "uri " uri)
      (println "status " status)
      (let [do-compile (and
                        (not compile-off)
                        (= status 200)
                        (some? (re-matches #"^.*\.js$" uri)))]
        (println "do-compile " do-compile)
        (if do-compile
          (let [body (:body res)
                url (build-url-from-req req)
                ;compiled-code (js-compile body)]
                compiled-code (js-compile-cached url body)]
            (-> res
                (assoc :body compiled-code)))
          res)))))

(defn wrap-body-length [handler]
  (fn [req]
    (let [res (handler req)
          body (-> res :body)]
      (if body
          (-> res
              (assoc-in [:headers "Content-Length"]
                        (-> body (.getBytes "UTF-8") count str)))
          res))))

; App with all middleware layers applied
(def app
  (-> #'proxy-handler
      wrap-compile
      wrap-body-length
      wrap-params))  ; injects :params, :query-params, :form-params (https://github.com/ring-clojure/ring/wiki/Parameters)

(defn -main [& args]
  (let [port 7070]
    (println "closure-api server listening on port " (. String valueOf port))
    (server/run-server #'app {:port port})))
