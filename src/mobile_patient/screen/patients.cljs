(ns mobile-patient.screen.patients
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]))


(defn row-component [{:keys [item index]} navigation]
  [ui/touchable-highlight
   {:style {:border-color "#E9E9EF"
            :border-width 1
            :border-top-width (if (zero? index) 0 1)
            :border-bottom-width 0
            :padding 10
            :background-color "#ffffff"}
    :on-press (fn []
                (print (get-in item [:id]))
                (rf/dispatch-sync [:set-current-patient (get-in item [:id])])
                (rf/dispatch-sync [:do-load-medication-statements])
                (navigation.navigate "Medications"))}
   [ui/view {:style {:flex-direction :row :justify-content :space-between}}
    [ui/text {:style {:text-align "left"
                      :font-size 20}} (-> item :username)]
    [ui/icon {:name "chevron-right" :size 30 :color "#FF485C"}]]])


(defn PatientsScreen [{:keys [navigation]}]
  (let [patients (rf/subscribe [:get-in [:practitioner-patients]])]
    [ui/flat-list {:style {:background-color :white}
                   :data (clj->js (vals @patients))
                   :key-extractor #(.-id %)
                   :render-item (fn [row]
                                  (r/as-element [row-component (js->clj row :keywordize-keys true) navigation]))}]))
