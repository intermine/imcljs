(defproject intermine/imcljs "0.1.35"
  :description "imcljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :deploy-repositories {"clojars" {:sign-releases false}}

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.671"]
                 [org.clojure/core.async "0.3.443"]
                 [cljs-http "0.1.42"]
                 [clj-http "3.6.0"]
                 [cheshire "5.7.1"]
                 [aysylu/loom "1.0.0"]]

  :plugins [[lein-figwheel "0.5.8"]
            [lein-cljsbuild "1.1.6" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.7"]]

  :source-paths ["src/cljc" "src/cljs" "src/clj"]

  :figwheel {:server-port 5003
             :reload-clj-files {:clj true :cljc true}}

  :doo {:build "test"
        :paths {:phantom "phantomjs --web-security=false"}
        :alias {:default [:phantom]}}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljc" "src/cljs"]
                :figwheel {:on-jsload "imcljs.core/on-js-reload"
                           :open-urls ["http://localhost:5003/index.html"]}
                :compiler {:main imcljs.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/imcljs.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src/cljc" "src/cljs"]
                :compiler {:output-to "resources/public/js/compiled/imcljs.js"
                           :main imcljs.core
                           :optimizations :advanced
                           :pretty-print false}}

               {:id "test"
                :source-paths ["src" "test/cljs"]
                :compiler {:output-to "resources/public/js/test/test.js"
                           :output-dir "resources/public/js/test"
                           :main imcljs.runner
                           :optimizations :none}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.4"]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
