(ns mobile-patient.ui
  (:require [reagent.core :as r]))


(def ReactNative (js/require "react-native"))
(def react-datepicker (js/require "react-native-datepicker"))

(def view (r/adapt-react-class (.-View ReactNative)))
(def modal (r/adapt-react-class (.-Modal ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def input (r/adapt-react-class (.-TextInput ReactNative)))
(def button (r/adapt-react-class (.-Button ReactNative)))
(def date-picker (r/adapt-react-class (aget react-datepicker "default")))
(def list-view (r/adapt-react-class (.-ListView ReactNative)))
(def picker (r/adapt-react-class (.-Picker ReactNative)))
(def picker-item (r/adapt-react-class (.-Picker.Item ReactNative)))
(def scroll-view (r/adapt-react-class (.-ScrollView ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(defn alert [title msg]
  (.alert (.-Alert ReactNative) title msg))
