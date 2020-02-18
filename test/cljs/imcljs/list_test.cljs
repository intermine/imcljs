(ns imcljs.list-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.env :refer [service]]
            [imcljs.auth :as auth]
            [imcljs.fetch :as fetch]
            [imcljs.save :as save]
            [imcljs.internal.utils :refer [<<!]]))

(def query {:from "Gene"
            :select ["Gene.id"]
            :where [{:path "Gene.symbol"
                     :op "ONE OF"
                     :values ["VAR" "ABRA" "PCNA" "STEVOR"]}]})

(comment
  "Evaluate this in REPL-connected editor to test query."
  (go
    (let [service (assoc service :token (<! (fetch/session service)))]
      (.log js/console (<! (save/im-list-from-query service "Some List" query)))
      (<! (save/im-list-delete service "Some List")))))

(deftest copy-list
  (testing "Should be able to copy a list"
    (async done
      (go
        ; Add a token to our service
        (let [service (assoc service :token (<! (fetch/session service)))]
          (let [; Create a new list
                new-list-name  (:listName (<! (save/im-list-from-query service "Some List" query)))
                ; Add a " - Duplicate" string to the end of the original list name
                duplicate-name (str new-list-name " - Duplicate")
                ; Copy the list and return its name
                copied-list    (<! (save/im-list-copy service new-list-name duplicate-name))]
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
          (let [; Create a new list
                new-list-name (:listName (<! (save/im-list-from-query service "Some List" query)))
                ; Add a " - Renamed" string to the end of the original list name
                new-name      (str new-list-name " - Renamed")
                ; Copy the list and return its name
                renamed-list  (<! (save/im-list-rename service new-list-name new-name))]
            ; Confirm that the renamed list name matches
            (is (and
                 (= new-name (:listName renamed-list))
                 (= true (:wasSuccessful renamed-list))))
            (done)))))))

(deftest create-list
  (testing "Should be able to create a list from a string of identifiers"
    (async done
      (go
        ; Add a token to our service
        (let [service (assoc service :token (<! (fetch/session service)))]
          (let [list-name "create-list test"
                new-list  (<! (save/im-list service list-name "Gene" "zen mad"))]
            ; Confirm that the renamed list name matches
            (is (and
                 (= list-name (:listName new-list))
                 (= true (:wasSuccessful new-list))))
            (done)))))))

(deftest delete-list
  (testing "Should be able to delete a single list"
    (async done
      (go
        (let [service (assoc service :token (<! (fetch/session service)))]
          (let [delete-status (->>
                               (save/im-list service (gensym) "Gene" "ABRA ENO")
                               <!
                               :listName
                               (save/im-list-delete service)
                               <!
                               :wasSuccessful)]
            (is (true? delete-status))
            (done)))))))

(deftest delete-lists
  (testing "Should be able to delete multiple lists"
    (async done
      (go
        (let [service (assoc service :token (<! (fetch/session service)))]
          (let [delete-status-col (->>
                                   (repeatedly gensym)
                                   (take 3)
                                   (map (fn [name] (save/im-list service name "Gene" "ABRA ENO")))
                                   <<!
                                   <!
                                   (map :listName)
                                   (save/im-list-delete service)
                                   <!
                                   (map :wasSuccessful))]
            (is (and
                 (some? (not-empty delete-status-col))
                 (every? true? delete-status-col)))
            (done)))))))

(deftest list-tag
  (testing "Should be able to add and remove tag for a list"
    (async done
      (go
        (let [login-response  (<! (auth/login service "test_user@mail_account" "secret"))
              token           (get-in login-response [:output :token])
              service+auth    (assoc service :token token)
              _create-list    (<! (save/im-list service+auth "mylist" "Gene" "ABRA ENO"))
              _add-tag        (<! (save/im-list-add-tag service+auth "mylist" "mytag"))
              list-response   (<! (fetch/one-list service+auth "mylist"))
              _remove-tag     (<! (save/im-list-remove-tag service+auth "mylist" "mytag"))
              list-response-2 (<! (fetch/one-list service+auth "mylist"))
              _delete-list    (<! (save/im-list-delete service+auth "mylist"))]
          (is (= ["mytag"] (:tags list-response)))
          (is (empty? (:tags list-response-2)))
          (done))))))

(deftest list-update
  (testing "Should be able to add and remove description for a list"
    (async done
      (go
        (let [login-response      (<! (auth/login service "test_user@mail_account" "secret"))
              token               (get-in login-response [:output :token])
              service+auth        (assoc service :token token)
              _create-list        (<! (save/im-list service+auth "mylist" "Gene" "ABRA ENO"))
              _add-description    (<! (save/im-list-update service+auth "mylist"
                                                           {:newDescription "My description."}))
              list-response       (<! (fetch/one-list service+auth "mylist"))
              _remove-description (<! (save/im-list-update service+auth "mylist"
                                                           {:newDescription ""}))
              list-response-2     (<! (fetch/one-list service+auth "mylist"))
              _delete-list        (<! (save/im-list-delete service+auth "mylist"))]
          (is (= "My description." (:description list-response)))
          (is (= "" (:description list-response-2)))
          (done))))))
