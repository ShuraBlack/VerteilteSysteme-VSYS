## Aufgabe 2 - Nebenläufigkeit

> ❕ message.jar muss als Lib in das Projekt mit eingebunden werden (Docs sind dafür nicht enthalten)

⬜️ Server ([Broker](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/broker/Broker.java)) <br>
⚪️ Client ([Aqualife](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/client/Aqualife.java)) <br>
🟢 Poisen-Client ([Poisoner](https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/task_2/src/aqua/blatt2/broker/Poisoner.java))

### 2.1 Nebenlaeufiger Aqualife-Broker

- Erstellen Sie eine (innere) Klasse BrokerTask, die die Verarbeitung und Beantwortung von Nachrichten uebernimmt.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/8daed7546c8caa2197ad19b3fce8c6dd3f19358e/task_2/src/aqua/blatt2/broker/Broker.java#L105-L128

- In der broker-Methode des Brokers erzeugen Sie nun fuer jede eingehende Nachricht
eine neue Instanz von BrokerTask und uebergeben Sie diese dem ExecutorService
zur Ausfuehrung.
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/8daed7546c8caa2197ad19b3fce8c6dd3f19358e/task_2/src/aqua/blatt2/broker/Broker.java#L48-L55

- Beachten Sie, dass die Klasse BrokerTask Zugriff auf die Client-Liste des Brokers
benoetigt, und dass diese konkurrierende Zugriffe synchronisiert werden muessen.
Um parallele Ausfuehrung von Handoff-Requests zu erlauben, verwenden Sie zur
Synchronisation einen ReadWriteLock wie im Laborskript zur Nebenlaeufigkeit
beschrieben
    - Reader
    https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/8daed7546c8caa2197ad19b3fce8c6dd3f19358e/task_2/src/aqua/blatt2/broker/Broker.java#L87-L103
    - Writer
    https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/8daed7546c8caa2197ad19b3fce8c6dd3f19358e/task_2/src/aqua/blatt2/broker/Broker.java#L62-L74
    https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/8daed7546c8caa2197ad19b3fce8c6dd3f19358e/task_2/src/aqua/blatt2/broker/Broker.java#L76-L85

### 2.1 stopRequested

stopRequested-Flag mit JOptionPane#showMessageDialog
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/8daed7546c8caa2197ad19b3fce8c6dd3f19358e/task_2/src/aqua/blatt2/broker/Broker.java#L41-L46

### 2.1 Poison-Pill

Poisoner Class als Client, welcher die Poison-Pill an den Server sendet
https://github.com/ShuraBlack/VerteilteSysteme-VSYS/blob/8daed7546c8caa2197ad19b3fce8c6dd3f19358e/task_2/src/aqua/blatt2/broker/Broker.java#L48-L60
