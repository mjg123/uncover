(defproject uncoverr "0.0.1"
  :description "Uncoverr: a timed, secure pastebin"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [ring "1.4.0"]
                 [clj-time "0.11.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.xerial/sqlite-jdbc "3.8.11.2"]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-ring "0.9.7"]]

  :source-paths ["src/clj"]

  :uberjar-name "server.jar"

  :cljsbuild {
              :builds {
                       :main {
                              :source-path "src/cljs"
                              :compiler {
                                         :output-to "resources/public/js/uncoverr.js"
                                         :optimizations :simple
                                         :pretty-print true}}}}
  :main uncoverr.server
  :ring {:handler uncoverr.server/app
         :nrepl {:start? true
                 :port 12345}})
