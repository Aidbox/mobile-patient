(ns patient.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe reg-sub reg-sub-raw]]
            [mobile-patient.model.patient :as patient-model]))


(reg-sub
 :user-name
 (fn [db _]
   (print db)
   (patient-model/get-official-name @(subscribe [:patient]))))

(reg-sub
 :user-picture
 (fn [db _]
   (patient-model/get-photo @(subscribe [:patient]))))

(reg-sub
 :chats
 (fn [db _]
   (:chats db)))

(reg-sub
 :contacts
 (fn [db _]
   (let [gen-pract-ids @(subscribe [:get-patients-general-practitioner-ids])]
     (->> (:asers db)
     (->> (:users db)
          (filter #((set gen-pract-ids) (-> % :ref :id)))
          (map #(hash-map :username (:id %)
                          :id (:id %)))
          ))))
