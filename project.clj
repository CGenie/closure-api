(defproject closure-api "0.1.0-SNAPSHOT"
  :description "A proxy server which transparently compiles JS files using the Closure Compiler."
  :main closure-api.core
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [environ "1.1.0"]
                 [com.google.javascript/closure-compiler "v20181008"]
                 [clj-http "3.9.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.2.0"]
                 [compojure "1.6.1"]])
