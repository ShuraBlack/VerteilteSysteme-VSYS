## Aufgabe 3 - Synchronisation 1/2

> ❕ message.jar muss als Lib in das Projekt mit eingebunden werden (Docs sind dafür nicht enthalten)

⬜️ Server ([Broker](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/broker/Broker.java)) <br>
⚪️ Client ([Aqualife](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/client/Aqualife.java)) <br>
🟢 Poisen-Client ([Poisoner](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/broker/Poisoner.java))

### 3.1 Ring-Topologie

- Erstellen Sie einen neuen Nachrichtentyp NeighborUpdate, mit dem der Broker
einem Klienten die InetSocketAddress eines neuen linken oder rechten Nachbarn
mitteilen kann.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/af42443368be8c5fbf1fa0d3f72845697d8812ee/src/aqua/blatt3/common/msgtypes/NeighborUpdate.java#L6-L24

- Erweitern Sie die Methode register des Brokers so, dass alle Klienten, die von
der Anmeldung des neuen Klienten betroffen sind, die Adressen ihrer linken und
rechten Nachbarn gesendet bekommen. Das sind der neue Klient selbst, sowie
dessen neuer linker und rechter Nachbar. Beachten Sie, dass der erste Klient in
der verteilten Umgebung sein eigener linker und rechter Nachbar ist.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/af42443368be8c5fbf1fa0d3f72845697d8812ee/src/aqua/blatt3/broker/Broker.java#L71-L88

- Erweitern Sie analog die Methode deregister des Brokers. Hier sind die betroffenen Klienten der linken und der rechte Nachbar des zu loeschenden Klienten.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/af42443368be8c5fbf1fa0d3f72845697d8812ee/src/aqua/blatt3/broker/Broker.java#L103-L109

- Die Klasse TankModel muss die InetSocketAddressen der Nachbarn halten.


- Erweitern Sie die Klasse ClientCommunicator.ClientReceiver so, dass auch
NeighborUpdate–Nachrichten entgegen genommen und verarbeitet werden.


- Aendern Sie die Klasse Der ClientCommunicator.ClientForwarder so, dass beim
Hand-Off Fische direkt an die richtigen Nachbarn geschickt werden.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/af42443368be8c5fbf1fa0d3f72845697d8812ee/src/aqua/blatt3/client/TankModel.java#L100-L104

### 3.2 Token-Ring

- Erstellen Sie den neuen Nachrichtentyp Token, der das durch den Ring laufende
Token repraesentiert.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/af42443368be8c5fbf1fa0d3f72845697d8812ee/src/aqua/blatt3/common/msgtypes/Token.java#L5-L7

- Zum Verwalten des Tokens im TankModel benoetigen Sie eine boolsche Instanzvariable sowie einen Timer (java.util.Timer), dem Sie Aufgaben fuer die zukuenftige
Ausfuehrung uebergeben koennen.


- Das TankModel muss Methoden zum Empfangen (receiveToken) und zum Abfragen des Tokens (hasToken) bereitstellen. Empfaengt das TankModel das Token,
wird die boolsche Variable gesetzt und dem Timer eine TimerTask zur Ausfuehrung
nach z.B. 2 Sekunden uebergeben. In der TimerTask wird die boolsche Variable
zurueckgesetzt und das Token an den linken Nachbarn weitergeschickt.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/af42443368be8c5fbf1fa0d3f72845697d8812ee/src/aqua/blatt3/client/TankModel.java#L35-L48

- Das TankModel darf nur dann Fische an Nachbarn schicken, wenn es gerade das
Token haelt. Wenn ein Fisch an den Rand des Aquariums stoeßt, solange das Aquarium das Token nicht haelt, aendert es seine Schwimmrichtung. Verwenden Sie dazu
die Methode FishModel.reverse.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/af42443368be8c5fbf1fa0d3f72845697d8812ee/src/aqua/blatt3/client/TankModel.java#L95-L105

- Die Klassen ClientForwarder und ClientReceiver muessen so veraendert werden,
dass sie mit Tokens umgehen koennen.


- Der Broker gibt das Token an den ersten Client aus, der sich registriert.


- Visualisieren Sie das Wandern des Tokens, indem Sie in der Klasse TankView
Aquariumsgrenzen zeichnen, solange das Token nicht da ist. Verwenden Sie dazu
die Methode TankView.drawBorders.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/af42443368be8c5fbf1fa0d3f72845697d8812ee/src/aqua/blatt3/client/TankView.java#L43-L61
