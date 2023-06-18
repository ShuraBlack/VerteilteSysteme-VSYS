## Aufgabe 4 - Synchronisation II

> ❕ message.jar muss als Lib in das Projekt mit eingebunden werden (Docs sind dafür nicht enthalten)

⬜️ Server ([Broker](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/broker/Broker.java)) <br>
⚪️ Client ([Aqualife](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/client/Aqualife.java)) <br>
🟢 Poisen-Client ([Poisoner](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/broker/Poisoner.java))

In dieser Aufgabe geht es darum, den Algorithmus von Chandy und Lamport zum Erstellen eines verteilten Schnappschusses in die Aqualife-Anwendung zu integrieren. Die
notwendigen Programmerweiterungen betreffen nahliegenderweise nur den Klienten, den
bisherigen Broker koennen Sie unveraendert uebernehmen.

Der lokale Schnappschuss jedes Klienten soll einfach aus der Anzahl der lokal vorhandener Fische bestehen. Der globale Schnappschuss ergibt sich damit als die Gesamtpopulation aller im Verbund enthaltenen Fische. Beachten Sie, dass es in Aqualife zwei
Stufen von ”im Transit” befindlichen Fischen gibt:

1. Fische, die per Hand-off an den Nachbarn uebergeben werden sollen, wobei die
   Handoff–Nachrichten im Netz unterwegs sind. Um die konsistente Beruecksichtigung
   solcher Nachrichten kuemmert sich der Lamport-Algorithmus mit Hilfe der Kanalaufzeichnungen und der Markierer-Nachrichten.
2. Scheidende Fische, die bereits an den Nachbarn uebergeben wurden, deren Hinausschwimmen aber noch korrekt zu Ende gezeichnet wird. Darauf, dass diese Fische
   nur in einem Aquarium gezaehlt werden, muessen Sie beim Erstellen der lokalen
   Schnappschuesse achten.

Gehen Sie zur Erstellung eines globalen Schnappschusses wie folgt vor:

- Programmieren Sie eine Methode tankModel.initiateSnapshot(), die die Erstellung eines globalen Schnappschusses anstoeßt. Die Methode muss dazu die folgenden
Aufgaben erfuellen:
    - speichere den lokalen Zustand;
    - starte Aufzeichnungsmodus fuer alle Eingangskanaele;
    - sende Markierungen in alle Ausgangskanaele.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/7901fa7d601ada52ed4051a3648c883c9885fb3c/task_4/src/aqua/blatt3/client/TankModel.java#L63-L75

- Da jeder Klient nur mit seinen linken und rechten Nachbarn kommuniziert, hat
er logisch betrachtet zwei Eingangs- und zwei Ausgangskanaele. Der Aufzeichnungsmodus kann deshalb einen der Zustaende IDLE, LEFT, RIGHT oder BOTH
annehmen, in Abhaengigkeit davon, welche Eingangskanaele gerade aufgezeichnet werden. Fuer die Modellierung des Aufzeichnungsmodus bietet sich eine JavaEnumeration an.


- Befindet sich ein Kanal im Aufzeichnungsmodus, so muessen auf diesem Kanal
ankommende Fische dem lokalen Zustand hinzugefuegt werden. Passen Sie hierfuer
die Methode TankModel.receiveFish() entsprechend an.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/7901fa7d601ada52ed4051a3648c883c9885fb3c/task_4/src/aqua/blatt3/client/TankModel.java#L177-L183

- Markierungen werden durch eine neue Nachrichtentyp, den SnapshotMarker,
repraesentiert. Dieser Marker benoetigt keine Funktionalitaet. Die Klassen
ClientForwarder und ClientReceiver muessen so angepasst werden, dass sie
den SnapshotMarker senden bzw. empfangen koennen.


- Empfaengt ein Klient einen SnapshotMarker, dann agiert er entsprechend dem
Algorithmus von Lamport zum Ermitteln seinen lokalen Schnappschusses.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/7901fa7d601ada52ed4051a3648c883c9885fb3c/task_4/src/aqua/blatt3/client/TankModel.java#L87-L136

- Ersetzen Sie in der Klasse AquaGui im Menuepunkt Global Snapshot den
NotImplementedYetController durch einen SnapshotController, der die
Methode TankModel.initiateSnapshot() aufruft.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/7901fa7d601ada52ed4051a3648c883c9885fb3c/task_4/src/aqua/blatt3/client/SnapshotController.java#L6-L18

- Zum Einsammeln der lokalen Schnappschuesse soll ein zusaetzliches Token verwendet
werden (definieren Sie dazu eine geeignete neue Nachrichtenklasse), das vom Initiator des globalen Schnappschusses erzeugt und einmal durch den Ring geschickt
wird. Dazu sendet der Initiator, sobald er seinen lokalen Schnappschuss erstellt
hat, das Token an seinen linken Nachbarn. Jeder Klient, der das Token bekommt,
haelt es so lange, bis der lokale Schnappschuss vorliegt, addiert seinen Schnappschuss auf das Zwischenergebnis des Tokens und gibt dieses dann weiter im Ring
bis es wieder beim Initiator angelangt.
- Erweitern Sie die Klasse TankView so, dass ein im TankModel vorliegender globaler Schnappschuss angezeigt wird. Fuer die Anzeige koennen Sie beispielsweise die
Methode JOptionPane.showMessageDialog() verwenden.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/7901fa7d601ada52ed4051a3648c883c9885fb3c/task_4/src/aqua/blatt3/common/msgtypes/SnapshotToken.java#L5-L17
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/7901fa7d601ada52ed4051a3648c883c9885fb3c/task_4/src/aqua/blatt3/client/TankModel.java#L138-L163
