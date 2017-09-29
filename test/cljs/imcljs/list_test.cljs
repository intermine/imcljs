(ns imcljs.list-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.path :as path]
            [imcljs.fetch :as fetch]
            [imcljs.save :as save]))

(def service {:root "www.flymine.org/query"
              :model {:name "genomic"}})

(def flymine-query {:from "Gene"
                    :select ["Gene.id"]
                    :where [{:path "Gene.symbol"
                             :op "ONE OF"
                             :values ["eve thor zen"]}]})

(deftest copy-list
  (testing "Should be able to copy a list"
    (async done
      (go
        ; Add a token to our service
        (let [service (assoc service :token (<! (fetch/session service)))]
          (let [ ; Create a new list
                new-list-name    (:listName (<! (save/im-list-from-query service flymine-query "Some List")))
                ; Add a " - Duplicate" string to the end of the original list name
                duplicate-name   (str new-list-name " - Duplicate")
                ; Copy the list and return its name
                copied-list (<! (save/im-list-copy service new-list-name duplicate-name))]
            ; Confirm that the duplicate name string matches the
            (is (and
                  (= duplicate-name (:listName copied-list))
                  (= true (:wasSuccessful copied-list))))
            (done)))))))

(deftest rename-list
  (testing "Should be able to rename a list"
    (async done
      (go
        ; Add a token to our service
        (let [service (assoc service :token (<! (fetch/session service)))]
          (let [ ; Create a new list
                new-list-name    (:listName (<! (save/im-list-from-query service flymine-query "Some List")))
                ; Add a " - Renamed" string to the end of the original list name
                new-name   (str new-list-name " - Renamed")
                ; Copy the list and return its name
                renamed-list (<! (save/im-list-rename service new-list-name new-name))]
            ; Confirm that the renamed list name matches
            (is (and
                  (= new-name (:listName renamed-list))
                  (= true (:wasSuccessful renamed-list))))
            (done)))))))

