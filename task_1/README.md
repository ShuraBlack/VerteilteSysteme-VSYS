## Aufgabe 1 - Broker

> ❕message.jar muss als Lib in das Projekt mit eingebunden werden

⬜️ Server ([Broker](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_1/src/aqua/blatt1/broker/Broker.java)) <br>
⚪️ Client ([Aqualife](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_1/src/aqua/blatt1/client/Aqualife.java)) <br>

- Der Broker benoetigt einen Endpoint, der auf Port 4711 hoert.

- Der Broker muss eine Liste verfuegbarer Clients fuehren. Verwenden Sie dazu die
vorgegebene Klasse ClientCollection.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/2b97b136e297bcd5972ffa6b1e525ed21909b196/task_1/src/aqua/blatt1/broker/Broker.java#L26-L29

- In der broker-Methode soll in einer Endlosschleife blockierend auf Nachrichten
gewartet werden. Ankommende Nachrichten muessen dekodiert und die im folgenden beschriebenen Methoden aufgerufen werden.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/2b97b136e297bcd5972ffa6b1e525ed21909b196/task_1/src/aqua/blatt1/broker/Broker.java#L35-L52

- Die register-Methode wird aufgerufen bei einem RegisterRequest. Der
Broker vergibt eine neue ID, beispielweise "tank1" fuer den ersten Client, "tank2" fuer den zweiten, usw., traegt den neuen Client in die Client-Liste ein und antwortet
ihm mit einer RegisterResponse-Nachricht.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/2b97b136e297bcd5972ffa6b1e525ed21909b196/task_1/src/aqua/blatt1/broker/Broker.java#L65-L75

- Die deregister-Methode wird aufgerufen bei einem DeregisterRequest. Der
Broker entfernt den Client aus der Client-Liste.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/2b97b136e297bcd5972ffa6b1e525ed21909b196/task_1/src/aqua/blatt1/broker/Broker.java#L82-L89

- Die Methode handoffFish wird aufgerufen bei einem HandoffRequest. Der
Broker ermittelt den betroffenen Nachbarn und gibt den HandoffRequest an
diesen weiter.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/2b97b136e297bcd5972ffa6b1e525ed21909b196/task_1/src/aqua/blatt1/broker/Broker.java#L98-L112

- Die main-Methode instantiiert einen neuen Broker und startet die brokerMethode.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/2b97b136e297bcd5972ffa6b1e525ed21909b196/task_1/src/aqua/blatt1/broker/Broker.java#L114-L117
