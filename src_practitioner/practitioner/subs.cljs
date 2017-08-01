(ns practitioner.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub reg-sub-raw]]))


(reg-sub
 :chats
 (fn [db _]
   (let [get-participants-ids #(set (map :id %))
         doctor-username (get-in db [:user :id])
         patient-username (get-in db [:patient-data :username])]
     (->> (:chats db)
          (filter (fn [{:keys [participants]}]
                    (clojure.set/subset? #{doctor-username patient-username}
                                         (get-participants-ids participants))))))))

(reg-sub
 :contacts
 (fn [db _]
   (-> db
       (get :practitioner-patients)
       vals)))

(reg-sub
 :user-name
 (fn [db _]
   (->> (get-in db [:practitioner-data :name])
        (filter #(= (:use %) "official"))
        first
        :text)))

(reg-sub
 :user-picture
 (fn [db _]
   (get-in db [:practitioner-data :photo 0 :data])))
