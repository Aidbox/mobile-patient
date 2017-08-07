(ns mobile-patient.screen.patients
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.model.patient :as patient-model]))


(defn row-component [{:keys [item index]} navigation]
  [ui/touchable-highlight
   {:style {:border-color "#E9E9EF"
            :border-width 1
            :border-top-width 0 ;;(if (zero? index) 0 0)
            :border-bottom-width 1 ;;0
            :padding 10
            :background-color "#ffffff"}
    :on-press (fn []
                (rf/dispatch-sync [:set-current-patient (get-in item [:id])])
                (rf/dispatch-sync [:do-load-medication-statements])
                (navigation.navigate "Medications"))}
   [ui/view {:style {:flex-direction :row
                     :flex 1
                     :padding-left 15
                     :padding-right 10
                     }}
    [ui/avatar (str "data:image/png;base64," (patient-model/get-photo item))
               20]
    [ui/text {:style {:flex 1
                      :margin-left 15
                      :align-self :center
                      :font-size 15
                      :color :black
                      :font-weight :bold}}
             (patient-model/get-official-name item)]

    [ui/icon {:name "chevron-right"
              :size 30
              :color "#FF485C"}]]])


(defn PatientsScreen [{:keys [navigation]}]
  (let [patients (rf/subscribe [:get-in [:patients]])]
    [ui/flat-list {:style {:background-color :white}
                   :data (clj->js (vals @patients))
                   :key-extractor #(.-id %)
                   :render-item (fn [row]
                                  (r/as-element [row-component (js->clj row :keywordize-keys true) navigation]))}]))
