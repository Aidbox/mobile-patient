(ns mobile-patient.screen.demographics
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [cljs-time.core :as time]
            [cljs-time.format :as format]))


(defn submit-handler [form-data]
  (if (every? #((complement empty?) %) (vals form-data))
    (rf/dispatch [:do-submit-demographics form-data])
    (ui/alert "" "All fields are required")))

(defn get-birthday [birthday]
  (.then (ui/date-picker.open #js {:mode "spinner"})
         (fn [x]
           (let [action (.-action x)]
             (if-not (= action (.-dismissedAction ui/date-picker))
               (reset! birthday (str (.-year x) "-" (.-month x) "-" (.-day x)))
             )))))

(defn DemographicsScreen [{:keys [navigation]}]
  (let [birthday (r/atom nil)
        sex (r/atom "")
        address (r/atom "")]
    (fn []
      [ui/view {:style {:flex 1 :margin 30}}
       ;;title
       [ui/text {:style {:font-size 30 :margin-bottom 40}}
        "Demographics"]

       ;;birthday
       (if (= (.-OS ui/Platform) "ios")
         [ui/view
          [ui/text " --Birthday--"]
          [ui/date-picker-ios {:date (js/Date. @birthday)
                               :on-date-change #(reset! birthday
                                                        (format/unparse
                                                         (format/formatter "yyyy-MM-dd")
                                                         (time/date-time %)))
                               :mode :date}]]
         [ui/touchable-highlight {:style {:padding 10
                                          :border-radius 5}
                                  :on-press #(get-birthday birthday)}
            [ui/text {:style {:font-weight "bold" :font-size 15}}
            (or @birthday "-- Birthday --")]])


       ;;gender
       [ui/picker {:on-value-change #(reset! sex %)
                   :selected-value @sex}
        [ui/picker-item {:label "-- Sex --" :value ""}]
        [ui/picker-item {:label "Male" :value "male"}]
        [ui/picker-item {:label "Female" :value "female"}]]

       [ui/input {:placeholder "Address"
                  :on-change-text #(reset! address %)
                  :style {:height 40}}]

       [ui/button {:title "Save" :on-press #(submit-handler {:birthday @birthday
                                                             :sex @sex
                                                             :address @address})}]])))
