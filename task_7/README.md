## Aufgabe 7 - Sicherheit

> ❕ message.jar muss als Lib in das Projekt mit eingebunden werden (Docs sind dafür nicht enthalten)

⬜️ Server ([Broker](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_7/src/aqua/broker/Broker.java)) <br>
⚪️ Client ([Aqualife](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_7/src/aqua/client/Aqualife.java)) <br>
🟢 Poisen-Client ([Poisoner](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_7/src/aqua/broker/Poisoner.java))

https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/19dc9f5b2f2a57f55ffa355a7e9029f9ef362c40/task_7/src/aqua/common/endpoint/SerializableUtil.java#L5-L21

### Aufgabe 1 – symmetrische Verschluesselung

Zunaechst ist jede Kommunikation zwischen Aqualife-Clients und zwischen Clients und
dem Broker mit Hilfe symmetrischer Schluessel zu verschluesseln. Um fuer diese Aufgabe
den Schluesselaustausch zu vermeiden, sollen alle Clients sowie der Broker denselben
gemeinsamen Schluessel verwenden. Gehen Sie zur Loesung der Aufgabe konkret wie folgt
vor:

- Die von Ihnen zu erstellende Klasse SecureEndpoint muss eine Subklasse von
  Endpoint sein, um diese ersetzen zu koennen. Intern soll SecureEndpoint einen
  eingekapselten, herkoemmlichen Endpoint fuer die eigentliche Kommunikation verwenden


- Der SecureEndpoint muss den symmetrischen Schluessel erzeugen und verwalten (speichern). Nutzen Sie zum Erzeugen des Schluessels die Plattform-Klasse
  SecretKeySpec. Verwenden Sie als ”Key-Material” fuer den Konstruktor die
  Bytefolge, die Sie aus dem String ”CAFEBABECAFEBABE” erzeugen. Damit
  ist sichergestellt, dass alle Endpunkte denselben Schluessel erzeugen. Als Verschluesselungsalgorithmus geben Sie "AES" an.


- Der SecureEndpoint benoetigt außerdem zwei Cipher-Objekte, eines zum Verschluesseln und eines zum Entschluesseln. Verwenden Sie zum Erzeugen der Objekte
  die Fabrikmethode Cipher.getInstance(String transformation), und geben Sie als Transformation wiederum den Verschluesselungsalgorithmus "AES" an. 
  Anschließend muss eines der Objekte zum Verschluesseln und eines zum Entschluesseln initialisiert werden.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/19dc9f5b2f2a57f55ffa355a7e9029f9ef362c40/task_7/src/aqua/common/endpoint/SecureEndpointSymmetric.java#L20-L49

- Die send-Methode der Klasse SecureEndpoint soll nun zunaechst den PayloadTeil der zu versendenden Nachricht mit Hilfe des Verschluesseler verschluesseln und
  danach die verschluesselte Nachricht ueber den internen normalen Endpoint an den
  gewuenschten Empfaenger verschicken.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/19dc9f5b2f2a57f55ffa355a7e9029f9ef362c40/task_7/src/aqua/common/endpoint/SecureEndpointSymmetric.java#L51-L63

- Analog sollen die beiden receive-Methoden ankommende Nachrichten von ihrem
  internen Endpunkt entgegennehmen, die Payloads mit Hilfe des entsprechenden
  Ciphers entschluesseln und anschließend an den Aufrufer zurueckgeben.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/19dc9f5b2f2a57f55ffa355a7e9029f9ef362c40/task_7/src/aqua/common/endpoint/SecureEndpointSymmetric.java#L65-L80

- Ersetzen Sie schließlich im Broker und im Client den normalen Endpoint durch
  den neuen SecureEndpoint, so dass alle Kommunikation verschluesselt ablaeuft.

### Aufgabe 2 –  asymmetrische Verschluesselung und Schluesseltausch

Verwenden Sie in dieser Aufgabe statt eines gemeinsamen symmetrischen Schluessels fuer
alle Clients (und den Broker) individuelle asymmetrische Schluesselpaare. Dazu muessen
zwei Kommunikationspartner, die zum ersten Mal miteinander kommunizieren, vor dem
eigentlichen Nachrichtenaustausch zuerst ihre oeffentlichen Schluessel austauschen. In
einem realistischen Szenario wuerde dies mittels Zertifikaten geschehen; in dieser Aufgabe werden einfach die unsignierten oeffentlichen Schluessel versendet. Gehen Sie zur
Loesung dieser Aufgabe wie folgt vor:

- Statt eines symmetrischen Schluessels erzeugt die Klasse SecureEndpoint nun ein
  asymmetrisches Schluesselpaar mit Hilfe der Java-Klasse KeyPairGenerator. Verwenden Sie als Algorithmus "RSA".


- Die Klasse SecureEndpoint benoetigt nun eine Datenstruktur, in der sie sich
  fuer jeden Kommunikationspartner, mit dem bereits kommuniziert wurde, dessen
  oeffentlichen Schluessel merkt.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/19dc9f5b2f2a57f55ffa355a7e9029f9ef362c40/task_7/src/aqua/common/endpoint/SecureEndpointAsymmetric.java#L19-L57
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/19dc9f5b2f2a57f55ffa355a7e9029f9ef362c40/task_7/src/aqua/common/endpoint/SecureEndpointAsymmetric.java#L109-L118

- Wenn in Folge eines send-Aufrufs eine Nachricht versandt werden soll, wird erst
  ueberprueft, ob der oeffentliche Schluessel des Empfaengers bereits bekannt ist. Falls
  ja, wird die Nachricht entsprechend verschluesselt. Falls nein, muss zuerst ein
  Schluesselaustausch stattfinden. Definieren Sie dazu einen neuen Nachrichtentyp
  KeyExchangeMessage. Beachten Sie, dass diese Nachrichten auf der empfangenden
  Seite nicht nach oben gelangen duerfen, sondern nur zur Kommunikation zwischen
  zwei SecureEndpoints verwendet werden duerfen.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/19dc9f5b2f2a57f55ffa355a7e9029f9ef362c40/task_7/src/aqua/common/endpoint/SecureEndpointAsymmetric.java#L59-L80

- In den receive-Methoden muessen Sie zum einen pruefen, ob es sich bei der empfangenen Nachricht um eine KeyExchange-Nachricht handelt und zum anderen alle
  anderen Nachrichten korrekt entschluesseln, bevor sie Sie nach oben reichen.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/19dc9f5b2f2a57f55ffa355a7e9029f9ef362c40/task_7/src/aqua/common/endpoint/SecureEndpointAsymmetric.java#L82-L107

- Verwenden Sie zum Ver- und Entschluesseln, aehnlich wie in Aufgabe 1, geeignete
  Cipher-Objekte.
