## Aufgabe 6 - Replikation und Konsistenz

> ❕ message.jar muss als Lib in das Projekt mit eingebunden werden (Docs sind dafür nicht enthalten)

⬜️ Server ([Broker](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_6/src/aqua/blatt3/broker/Broker.java)) <br>
⚪️ Client ([Aqualife](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_6/src/aqua/blatt3/client/Aqualife.java)) <br>
🟢 Poisen-Client ([Poisoner](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_6/src/aqua/blatt3/broker/Poisoner.java))

In dieser Ubung sollen Sie die bisherige, dauerhafte Registrierung der Klienten beim
Aqualife-Broker durch eine Lease-basierte, temporaere Registrierung ersetzen. Leases
wurden in der Vorlesung im Zusammenhang mit dem Propagieren von Updates behandelt
und werden jetzt hier verwendet, um durch eine Soft–State–Registrierung eine hoehere
Robustheit im Falle abgestuerzter oder fehlerhafter Klienten zu erreichen. Gehen Sie zur
Loesung der Aufgabe wie folgt vor:

- Erweitern Sie den Nachrichtentyp RegisterResponse um einen Integerwert, der
die Dauer des Leases angibt, mit dem der Klient beim Broker registriert bleibt.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/643587f9447f2e583be04ea4b6641969953f0ecd/task_6/src/aqua/blatt3/common/msgtypes/RegisterResponse.java#L5-L23

- Aendern Sie den Aqualife-Klienten so ab, dass beim Empfangen einer RegisterResponse–Nachricht ein Timer gestartet wird, der innerhalb der Lease-Dauer eine
Reregistrierung beim Broker durchfuehrt. Verwenden Sie dazu die Klasse TimerTask,
die Sie bereits fuer das Ubungsblatt 3 benutzt haben.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/643587f9447f2e583be04ea4b6641969953f0ecd/task_6/src/aqua/blatt3/client/TankModel.java#L81-L93

- Im Aqualife-Broker muessen Sie folgende Anpassungen durchfuehren:
  - Erweitern Sie ClientCollection, die Datenstruktur zur Verwaltung aller
  Aqualife-Klienten, um einen Zeitstempel, an dem sich der betreffende Klient
  angemeldet hat.
  https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/643587f9447f2e583be04ea4b6641969953f0ecd/task_6/src/aqua/blatt3/broker/ClientCollection.java#L14-L25
  - Wenn sich ein Klient registriert, muessen Sie ueberpruefen, ob er bereits bekannt
  ist oder nicht. Neue Klienten werden mit Zeitstempel eingetragen, fuer bereits
  bekannte Klienten wird lediglich der Zeitstempel aktualisiert. Senden Sie in
  der RegisterResponse eine Lease-Dauer mit. Sie koennen einfach eine fixe
  Lease-Dauer Ihrer Wahl verwenden, oder aber die Leasedauer z.B. von der
  Menge der registrierten Klienten abhaengig machen.
  https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/643587f9447f2e583be04ea4b6641969953f0ecd/task_6/src/aqua/blatt3/broker/Broker.java#L81-L91
  - Verwenden Sie ebenfalls die Klasse TimerTask, um in regelmaeßigen Abstaenden
  die ClientCollection auf veraltete Eintraege zu durchsuchen. Fuehren Sie
  fuer Klienten, deren Lease abgelaufen ist, dieselbe Operation durch wie beim
  regulaeren Deregistrieren.
  https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/643587f9447f2e583be04ea4b6641969953f0ecd/task_6/src/aqua/blatt3/broker/Broker.java#L41-L58
  https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/643587f9447f2e583be04ea4b6641969953f0ecd/task_6/src/aqua/blatt3/broker/ClientCollection.java#L43-L50
