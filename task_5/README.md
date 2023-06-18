## Aufgabe 5 - Namensdienste

> ❕ message.jar muss als Lib in das Projekt mit eingebunden werden (Docs sind dafür nicht enthalten)

⬜️ Server ([Broker](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_5/src/aqua/blatt3/broker/Broker.java)) <br>
⚪️ Client ([Aqualife](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_5/src/aqua/blatt3/client/Aqualife.java)) <br>
🟢 Poisen-Client ([Poisoner](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_5/src/aqua/blatt3/broker/Poisoner.java))

In dieser Ubung sollen Sie Loesungen zum Lokalisieren von Entitaeten in einem verteilten
System implementieren. Bei den zu lokalisierenden Entitaeten handelt es sich um
die Fische. Ein Aqualife-Klient soll in der Lage sein, seine selbst erzeugten Fische im
verteilten Aquarium lokalisieren zu koennen, auch wenn sie in ein anderes Aquarium
geschwommen sind. Zur Veranschaulichung ist der Menupunkt Tools → Toggle Fish
Color im Klienten gedacht.

### Aufgabe 1: Vorwaertsreferenzen
Eine einfache Methode, die Fische zu lokalisieren, ist mit Hilfe von Vorwaertsreferenzen.
Wenn ein Fisch von einem alten in einen neuen Tank schwimmt, hinterlaesst er dabei eine
Vorwaertsreferenz im alten Tank.
Jeder Tank benoetigt dazu eine Datenstruktur, in der er fuer jeden Fisch, der jemals
in dem Tank war, verzeichnet, ob der Fisch gerade im Tank ist oder ob er den Tank
nach links oder nach rechts verlassen hat. Somit gibt es drei Zustaende der Referenzen:
HERE, LEFT, RIGHT. Diese Datenstrukturen von Referenzen muss immer dann aktualisiert werden, wenn
- ein neuer Fisch erzeugt wird;
- ein Fisch ein Aquarium verlaesst, also ueber einen Hand-Off weitergereicht wird;
- ein Fisch, der aus einem anderen Aquarium kommt, entgegengenommen wird.

Gehen Sie nun zur Implementierung der Lokalisierung von Fischen wie folgt vor:
1. Ersetzen Sie fuer den Menupunkt Tools → Toggle Fish Color den
   NotImplementedYetController durch einen ToggleController, der die zu
   implementierende Methode TankModel.locateFishGlobally aufruft.
   https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/ff86028aa76a492efeba18aa693dadc8f2d4e0e7/task_5/src/aqua/blatt3/client/ToggleController.java#L6-L18
2. In der Methode TankModel.locateFishGlobally wird nachgeschaut, ob sich der
   Fisch (a) im Aquarium befindet, oder ob er (b) nach links oder nach rechts herausgeschwommen ist. Im Fall (a) muessen Sie die Datenstruktur fishies nach dem
   Fisch durchsuchen und dann mit Hilfe der Methode FishModel.toggle markieren.
   Fuer das lokale Suchen und Markieren bietet es sich an, eine private Hilfsmethode locateFishLocally zu schreiben. Im Fall (b) muss die Suche an den linken
   bzw. rechten Nachbarn weitergereicht werden. Dies ist in den folgenden Schritten
   beschrieben.
   https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/ff86028aa76a492efeba18aa693dadc8f2d4e0e7/task_5/src/aqua/blatt3/client/TankModel.java#L276-L297
3. Definieren Sie einen neuen Nachrichtentyp LocationRequest. Wenn ein Fisch,
   wie oben beschrieben, nach links oder nach rechts herausgeschwommen ist, dann
   sendet das suchende Aquarium einen LocationRequest an den entsprechenden
   Nachbarn.
   https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/ff86028aa76a492efeba18aa693dadc8f2d4e0e7/task_5/src/aqua/blatt3/common/msgtypes/LocationRequest.java#L5-L17
4. Empfaengt ein Klient einen LocationRequest, dann ruft er seinerseits die Methode
   locateFishGlobally auf.
5. Auf diese Weise pflanzt sich die Suche entlang der Kette von Vorwaertsreferenzen
   so lange fort, bis der Fisch in seinem aktuellen Aquarium lokalisiert wird.

### Aufgabe 2: Heimatgestuetzter Ansatz
Die Loesung mit Vorwaertsreferenzen kann in einem großen verteilten System zu sehr
langen Ketten fuehren oder auch dazu, dass beim Ausfall eines Knotens die Kette reisst.
In dieser Aufgabe soll deshalb ein heimatgestuetzter Ansatz programmiert werden, in
dem jeder Fisch ein Heimataquarium besitzt – das, in dem er erzeugt wurde –, das stets
ueber seinen aktuellen Standort informiert wird.

Um dies zu erreichen, wird der Broker um einen Namensdienst erweitert, der TankIDs auf Socketadressen abbildet. Damit kann ein Aquarium, das einen fremden Fisch
empfaengt, beim Namensdienst das Heimataquarium des Fisches erfragen und dieses dann
ueber den neuen Standort informieren. Implementieren Sie den Namensdienst im Broker
wie folgt:
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/ff86028aa76a492efeba18aa693dadc8f2d4e0e7/task_5/src/aqua/blatt3/broker/NameService.java#L12-L33

- Erstellen Sie einen neuen Nachrichtentyp NameResolutionRequest. Dieser transportiert die Tank-ID des Aquariums, dessen Adresse gefunden werden soll, sowie
eine Request-ID, die dem anfragenden Klienten hilft, Anfrage und Antwort einander zuzuordnen.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/ff86028aa76a492efeba18aa693dadc8f2d4e0e7/task_5/src/aqua/blatt3/common/msgtypes/NameResolutionRequest.java#L6-L23
- Implementieren Sie einen neuen Nachrichtentyp NameResolutionResponse, der
die Adresse des angefragten Aquariums sowie die unveraenderte Request-ID der
Anfrage enthaelt.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/ff86028aa76a492efeba18aa693dadc8f2d4e0e7/task_5/src/aqua/blatt3/common/msgtypes/NameResolutionResponse.java#L6-L30
- Erhaelt der Broker einen NameResolutionRequest, ermittelt er die zur Tank-ID
gehoerende Adresse und schickt sie in einer NameResolutionResponse zurueck an
den Sender.
- Klientenseitig muss die Lokalisierungsfunktionalitaet wie folgt angepasst werden:

- Ersetzen Sie die Datenstruktur der Vorwaertsreferenzen aus Aufgabe 1 durch eine
Datenstruktur homeAgent, in der fuer jeden Fisch, der in diesem Aquarium beheimatet ist, die Adresse seines aktuellen Standorts enthalten ist. Der homeAgent wird
bei zwei Gelegenheiten aktualisiert: (a) Wenn ein neuer Fisch erzeugt wird, wird er
mit dem aktuellen Standort (beispielsweise dem Wert null, um anzuzeigen, dass
er sich im Heimataquarium befindet) eingetragen. (b) Wenn ein Fisch aus einem
anderen Aquarium entgegengenommen wird, wird sein Heimataquarium ueber den
neuen Standort informiert. Dieser Vorgang ist im folgenden Schritt beschrieben.
- Schwimmt ein Fisch in einen neuen Tank, ist zu unterscheiden, ob der Fisch (a)
urspruenglich in diesem Aquarium beheimatet ist oder (b) nicht. Im Fall (a)
muss der eigene homeAgent entsprechend aktualisiert werden. Im Fall (b) muss
der Klient das Heimataquarium des Fisches informieren. Dazu sendet er einen
NameResolutionRequest an den Broker. Die dazu benoetigte Tank-ID bekommt
der Klient vom Fisch selbst (FishModel.getTankId). Als Request-ID verwenden Sie die Fisch-ID. Sobald – zu einem spaeteren Zeitpunkt 
– die Antwort eintrifft, wird das Heimataquarium mit Hilfe des neu zu erstellenden Nachrichtentyps
LocationUpdate ueber den neuen Standort des Fisches informiert.
- Passen Sie die Methode locateFishGlobally so an, dass sie nun den homeAgent
verwendet und die Suche ggf. an das aktuelle Aquarium des Fisches weiterleitet.
- Wenn ein Klient einen LocationRequest empfaengt, kann er nun direkt die Methode locateFishLocally aufrufen.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/ff86028aa76a492efeba18aa693dadc8f2d4e0e7/task_5/src/aqua/blatt3/client/TankModel.java#L252-L273
