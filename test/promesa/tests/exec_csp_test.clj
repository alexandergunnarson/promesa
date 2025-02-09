(ns promesa.tests.exec-csp-test
  (:require
   [clojure.test :as t]
   [promesa.core :as p]
   [promesa.exec :as px]
   [promesa.exec.csp :as sp]
   [promesa.protocols :as pt]))

(t/deftest chan-factory
  (let [c1 (sp/chan)
        c2 (sp/chan 2)
        c3 (sp/chan 2 (map inc))
        c4 (sp/chan (sp/fixed-buffer 2))
        c5 (sp/chan (sp/sliding-buffer 2))
        c6 (sp/chan (sp/dropping-buffer 2))]

    (t/is (sp/chan? c1))
    (t/is (sp/chan? c2))
    (t/is (sp/chan? c3))
    (t/is (sp/chan? c4))
    (t/is (sp/chan? c5))
    (t/is (sp/chan? c6))
    ))

(t/deftest non-blocking-ops-buffered-chan
  (let [ch (sp/chan 3)]
    (t/is (true? (sp/offer! ch :a)))
    (t/is (true? (sp/offer! ch :b)))
    (t/is (true? (sp/offer! ch :c)))
    (t/is (false? (sp/offer! ch :d)))

    (t/is (= :a (sp/poll! ch)))
    (t/is (= :b (sp/poll! ch)))
    (t/is (= :c (sp/poll! ch)))
    (t/is (= nil (sp/poll! ch)))
    ))

(t/deftest non-blocking-ops-buffered-and-closed-chan
  (let [ch (sp/chan 3)]
    (t/is (true? (sp/offer! ch :a)))
    (t/is (true? (sp/offer! ch :b)))

    (sp/close! ch)

    (t/is (false? (sp/offer! ch :c)))
    (t/is (true? (sp/closed? ch)))

    (t/is (= :a (sp/poll! ch)))
    (t/is (= :b (sp/poll! ch)))
    (t/is (= nil (sp/poll! ch)))
    ))

(t/deftest channel-with-sliding-buffer-and-transducer
  (let [ch (sp/chan (sp/sliding-buffer 2) (map name))]
    (t/is (true? (sp/offer! ch :a)))
    (t/is (true? (sp/offer! ch :b)))
    (t/is (true? (sp/offer! ch :c)))
    (t/is (= "b" (sp/poll! ch)))
    (t/is (= "c" (sp/poll! ch)))
    (t/is (= nil (sp/poll! ch)))))

(t/deftest channel-with-dropping-buffer-and-transducer
  (let [ch (sp/chan (sp/dropping-buffer 2) (map name))]
    (t/is (true? (sp/offer! ch :a)))
    (t/is (true? (sp/offer! ch :b)))
    (t/is (true? (sp/offer! ch :c)))
    (t/is (= "a" (sp/poll! ch)))
    (t/is (= "b" (sp/poll! ch)))
    (t/is (= nil (sp/poll! ch)))))

(t/deftest unbuffered-chan
  (let [ch (sp/chan)
        p1 (sp/go (sp/>! ch :a))
        r1 (sp/take! ch)]
    (t/is (= :a @r1))
    (t/is (true? @p1))))
