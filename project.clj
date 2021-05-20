(defproject org.intermine/imcljs "1.4.4"
  :description "imcljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.8.1"

  :deploy-repositories {"clojars" {:sign-releases false}}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/core.async "0.4.500"]
                 [cljs-http "0.1.46"]
                 [clj-http "3.10.0"]
                 [cheshire "5.8.1"]
                 [aysylu/loom "1.0.2"]]
  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [org.clojure/core.unify "0.5.7"]
            [lein-cljfmt "0.6.1"]
            [lein-codox "0.10.7"]]
  :aliases {"format" ["cljfmt" "fix"]}

  :source-paths ["src/cljc" "src/cljs" "src/clj" "test/clj" "test/cljs"]

  :codox {:language :clojurescript
          :source-paths ["src"]}

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

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                                  [figwheel-sidecar "0.5.19"]
                                  [cider/piggieback "0.4.1"]]
                   :plugins [[lein-figwheel "0.5.19"]
                             [lein-doo "0.1.10"]]}
             :repl {:source-paths ["dev"]}
             :java9 {:jvm-opts ["--add-modules" "java.xml.bind"]}}

  :cljfmt {:indents {async [[:inner 0]]}})
