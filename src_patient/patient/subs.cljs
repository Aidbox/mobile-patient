(ns patient.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe reg-sub reg-sub-raw]]))


(reg-sub
 :chats
 (fn [db _]
   (:chats db)))

(reg-sub
 :contacts
 (fn [db _]
   (let [gen-pract-ids @(subscribe [:get-patients-general-practitioner-ids])]
     (->> (:all-users db)
          (filter #((set gen-pract-ids) (-> % :ref :id)))
          (map #(hash-map :username (:id %)
                          :id (:id %)))
          ))))
