(ns mobile-patient.screen.demographics
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]))



(defn submit-handler [age sex address dispatch]
  (if (or (empty? age)
          (empty? sex)
          (empty? address))
    (ui/alert "" "All fields are required")
    ;; fix submit
    ;; (dispatch nav/tabs)
    (rf/dispatch [:set-current-screen :main])
    ))


(defn DemographicsScreen [{:keys [navigation]}]
  (let [age (atom "")
        sex (r/atom "")
        address (atom "")]
    (fn []
      [ui/view {:style {:flex 1 :margin 30}}
       [ui/input {:placeholder "Age"
                  :on-change-text #(reset! age %)
                  :keyboard-type :numeric}]

       [ui/picker {:on-value-change #(reset! sex %)
                   :selected-value @sex}
        [ui/picker-item {:label "-- Sex --" :value ""}]
        [ui/picker-item {:label "Male" :value "male"}]
        [ui/picker-item {:label "Female" :value "female"}]]

       [ui/input {:placeholder "Address"
                  :on-change-text #(reset! address %)}]

       [ui/button {:title "Save" :on-press #(submit-handler @age
                                                            @sex
                                                            @address
                                                            nil
                                                            ;; navigation.dispatch
                                                            )}]])))
