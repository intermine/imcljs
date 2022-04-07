(ns imcljs.query-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.env :refer [service]]
            [imcljs.fetch :as fetch]
            [imcljs.query :as query]))

(def normal-query {:from "Gene"
                   :select ["Gene.symbol" "Gene.organism.name" "Gene.proteins.genes.symbol"]
                   :where [{:path "Gene.symbol"
                            :op "="
                            :value "ABRA"
                            :code "A"}]})

(def outer-join-query {:constraintLogic "B and C"
                       :from "Gene"
                       :select ["secondaryIdentifier"
                                "symbol"
                                "proteins.genes.primaryIdentifier"
                                "proteins.genes.symbol"
                                "proteins.length"
                                "proteins.genes.organism.name"
                                "proteins.dataSets.name"
                                "proteins.genes.ontologyAnnotations.ontologyTerm.name"
                                "proteins.genes.ontologyAnnotations.evidence.code.code"]
                       :joins ["proteins.genes.ontologyAnnotations"]
                       :where [{:path "Gene",
                                :op "LOOKUP",
                                :value "ABRA",
                                :code "B"}
                               {:path "proteins.genes.organism.name",
                                :op "=",
                                :value "Plasmodium falciparum 3D7",
                                :code "C"}]})

(deftest outer-join
  (testing "Query support for outer joins"
    (async done
      (go
        (let [{results :results} (<! (fetch/table-rows service outer-join-query))]
          (is (some? (not-empty results)))
          (done))))))

(deftest deconstruct-by-class
  (testing "Should be able to deconstruct a query into its classes"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [result (query/deconstruct-by-class model normal-query)]
            (is (= result
                   {:Gene {"Gene" {:query {:from "Gene", :select ["Gene.id"], :where [{:path "Gene.symbol", :op "=", :value "ABRA", :code "A"} {:path "Gene.organism.id", :op "IS NOT NULL"} {:path "Gene.proteins.genes.id", :op "IS NOT NULL"}]}}, "Gene.proteins.genes" {:query {:from "Gene", :select ["Gene.proteins.genes.id"], :where [{:path "Gene.symbol", :op "=", :value "ABRA", :code "A"} {:path "Gene.id", :op "IS NOT NULL"} {:path "Gene.organism.id", :op "IS NOT NULL"}]}}}, :Organism {"Gene.organism" {:query {:from "Gene", :select ["Gene.organism.id"], :where [{:path "Gene.symbol", :op "=", :value "ABRA", :code "A"} {:path "Gene.id", :op "IS NOT NULL"} {:path "Gene.proteins.genes.id", :op "IS NOT NULL"}]}}}}))
            (done)))))))
