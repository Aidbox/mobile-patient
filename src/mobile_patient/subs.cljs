(ns mobile-patient.subs
  (:require [re-frame.core :refer [reg-sub]]))

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
