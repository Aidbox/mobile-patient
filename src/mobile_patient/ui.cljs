(ns mobile-patient.ui
  (:require [reagent.core :as r]
            [mobile-patient.color :as color]))


(def ReactNative (js/require "react-native"))
(def react-datepicker (js/require "react-native-datepicker"))

;; (def picker (r/adapt-react-class (.-Picker ReactNative)))
;; (def picker-item (r/adapt-react-class (.. ReactNative -Picker -Item)))

(def view (r/adapt-react-class (.-View ReactNative)))
(def modal (r/adapt-react-class (.-Modal ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def input (r/adapt-react-class (.-TextInput ReactNative)))
(def Switch (.-Switch ReactNative))
(def switch (r/adapt-react-class (.-Switch ReactNative)))
(def rn-button (r/adapt-react-class (.-Button ReactNative)))
(def date-picker (r/adapt-react-class (aget react-datepicker "default")))
(def list-view (r/adapt-react-class (.-ListView ReactNative)))
(def virtualized-list (r/adapt-react-class (.-VirtualizedList ReactNative)))
(def flat-list (r/adapt-react-class (.-FlatList ReactNative)))
(def section-list (r/adapt-react-class (.-SectionList ReactNative)))
(def scroll-view (r/adapt-react-class (.-ScrollView ReactNative)))
(def activity-indicator (r/adapt-react-class (.-ActivityIndicator ReactNative)))

(def picker (r/adapt-react-class (.-Picker ReactNative)))
(def picker-item (r/adapt-react-class (.-Picker.Item ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(defn alert [title msg]
  (.alert (.-Alert ReactNative) title msg   #js[#js{:text "Close"}]))

(def Icon (js/require "react-native-vector-icons/MaterialIcons"))
(def icon (r/adapt-react-class (aget Icon "default")))

(def ReactNativeTableviewSimple (js/require "react-native-tableview-simple"))
(def table-view (r/adapt-react-class (.-TableView ReactNativeTableviewSimple)))
(def section (r/adapt-react-class (.-Section ReactNativeTableviewSimple)))
(def cell (r/adapt-react-class (.-Cell ReactNativeTableviewSimple)))

(defn button [{:keys [title on-press] :as props}]
  [touchable-highlight {:style {:background-color color/light-grey
                                :border-width 1
                                :border-color color/grey
                                :padding 10
                                :margin-top 20
                                }
                        :on-press on-press}
   [text {:style {:color color/grey
                  :font-weight :bold
                  :text-align :center}}
    title]])

(defn link [{:keys [title on-press] :as props}]
  [touchable-highlight {:style {:padding 10}
                        :on-press on-press}
   [text {:style {:color color/grey
                  ;;:font-weight :bold
                  :text-align :center}}
    title]])
