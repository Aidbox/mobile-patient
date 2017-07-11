(ns mobile-patient.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub reg-sub-raw]]))

;; -- Subscriptions----------------------------------------------------------
(reg-sub-raw
 :user-id
 (fn [db _] (reaction (get-in @db [:user :id]))))

(reg-sub-raw
 :user-ref
 (fn [db _] (reaction (get-in @db [:user :ref]))))

(reg-sub
 :contacts
 (fn [db _] (:contacts db)))

(reg-sub
 :users
 (fn [db _] (:users db)))

(reg-sub
 :chats
 (fn [db _] (:chats db)))

(reg-sub
 :chat
 (fn [db _] (:chat db)))

(reg-sub
 :messages
 (fn [db _] (:messages db)))

(reg-sub
 :message
 (fn [db _] (:message db)))

(reg-sub
 :active-medication-statements
 (fn [db _] (:active-medication-statements db)))

(reg-sub
 :other-medication-statements
 (fn [db _] (:other-medication-statements db)))

(reg-sub
  :get-greeting
  (fn [db _]
    (:greeting db)))

(reg-sub
 :get-in
 (fn [db [_ path]]
   (get-in db path)))

(reg-sub
 :get-current-screen
 (fn [db _]
   (:current-screen db)))

(defmulti get-observation-data #(:id %))

(defmethod get-observation-data "blood-pressure" [item]
  (for [i [0 1]]
    {:key (str (get item :id) i)
     :title (get-in item [:component i :code :coding 0 :display])
     :value (str (get-in item [:component i :valueQuantity :value]) " "
                 (get-in item [:component i :valueQuantity :unit]))
     :interpretation (get-in item [:component i :interpretation :coding 0 :code])
     }))

(defmethod get-observation-data :default [item]
  [{:key (get item :id)
    :title (get-in item [:code :text])
    :value (str (get-in item [:valueQuantity :value]) " "
                (get-in item [:valueQuantity :unit]))
    :interpretation (get-in item [:interpretation :coding 0 :code])
    }])

(reg-sub
 :get-observations
 (fn [db _]
   (->> (:observations db)
        (map get-observation-data)
        (apply concat)
        )))
