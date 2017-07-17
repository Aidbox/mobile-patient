(ns practitioner.screen.patients
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]))


(defn row-component [{:keys [item]} navigation]
  [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                   :border-width 1
                                   ;;:border-top-width 0
                                   :padding 10
                                   :background-color "#ffffff"}
                           :on-press (fn []
                                       (rf/dispatch [:set-current-patient-id (:id item)])
                                       (navigation.navigate "Patient"
                                                            #js{:patientid (:id item)}))}
   [ui/view {:style {:flex-direction :row :justify-content :space-between}}
    [ui/text {:style {:text-align "left"}} (:id item)]
    [ui/icon {:name "chevron-right" :size 30 :color "#FF485C"}]]])


(defn PatientsScreen [{:keys [navigation]}]
  (let [patients (rf/subscribe [:get-in [:practitioner-patients]])]
    [ui/flat-list {:data (clj->js (vals @patients))
                   :key-extractor #(.-id %)
                   :render-item (fn [row]
                                  (r/as-element [row-component (js->clj row :keywordize-keys true) navigation]))}]))
