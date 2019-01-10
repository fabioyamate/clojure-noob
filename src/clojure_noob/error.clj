(ns clojure-noob.error)

(defn apply-or-error [f [val err]]
  (if (nil? err)
    (f val)
    [nil err]))

(defn clean-address [params]
  "Ensure (params :address) is present"
  (if (empty? (params :address))
    [nil "Please enter your address"]
    [params nil]))

(defn clean-email [params]
  (if (re-find #"\w@\w\.\w" (:email params))
    [params nil]
    [nil "Please enter an email address"]))

(defn clean-phone [params]
  (if (re-find #"\([0-9]{3}\) [0-9]{3}-[0-9]{4}" (params :phone))
    [params nil]
    [nil "Please enter your phone number in (555) 555-5555 format."]))

(defn clean-state [params]
  "Ensure state is one of OR or WA. Cascadians unite!"
  (case (params :state)
    "WA" [params nil]
    "OR" [params nil]
    [nil "We only want people from Oregon or Washington, for some reason."]))

(defn clean-contact [params]
  (->> (clean-email params)
       (apply-or-error clean-address)
       (apply-or-error clean-phone)
       (apply-or-error clean-state)))

(comment
  (clean-contact))
