(ns closure-api.compiler
  (:use [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server
        [org.httpkit.client :as http]
        [ring.util.response :as r])
  (:require digest))

; TODO - turn off warnings report
(defn js-compile
  [code]
  (let [compiler (new com.google.javascript.jscomp.Compiler)
        simple-optimizations (.. com.google.javascript.jscomp.CompilationLevel SIMPLE_OPTIMIZATIONS)
        options (new com.google.javascript.jscomp.CompilerOptions)
        input (. com.google.javascript.jscomp.SourceFile fromCode "input.js" code)
        extern (. com.google.javascript.jscomp.SourceFile fromCode "extern.js" "")]
    (. simple-optimizations setOptionsForCompilationLevel options)
    (-> compiler (.compile extern input options))
    (-> compiler (.toSource))))

(defn test-script
  []
  "function z(x) { function y(p) { return p + 1; }; alert(x); }")

(defn -main-- [& args]
  (let [compiled (js-compile (test-script))]
    (println "Result: " compiled)))
    ;; (println "success: " (. compiled success))
    ;; (println "errors: " (. compiled errors))
    ;; (println "warnings: " (. compiled warnings))
    ;; (println "variableMap: " (. compiled variableMap))
    ;; (println "propertyMap: " (. compiled propertyMap))
    ;; (println "namedAnonFunctionMap: " (. compiled namedAnonFunctionMap))
    ;; (println "stringMap: " (. compiled stringMap))
    ;; (println "externExport: " (. compiled externExport))
    ;; (println "cssNames: " (. compiled cssNames))
    ;; (println "idGeneratorMap: " (. compiled idGeneratorMap))
    ;; (println "transpiledFiles: " (. compiled transpiledFiles))))
