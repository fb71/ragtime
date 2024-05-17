# Beitrag

(Fast) Jeder Text auf der Website ist ein 'Beitrag'. Ein Beitrag hat einen internen **Namen** und einen **Text**. Es können auch **Bilder** angehangen/zugeordnet werden.

### Topic

Jeder Beitrag ist einem Topic zugeordnet. Das Topic entscheidet darüber wo (und auch wie) der Beitrag in der Website angezeigt wird. Topics können zusätzlich mit anderen Kanälen verbunden sein, z.B. Instagram. Beiträge in diesem Topic werden dann automatisch auf Instagram gepostet.

### Name

Der Name eines Beitrags wird verwendet, um den Beitrag zu identifizieren, wird aber nicht als Überschrift verwendet. Wenn der Beitrag eine Überschrift haben soll, dann wird diese in den Text geschrieben (siehe unten).

### Bilder

Zu einem Beitrag können Bilder oder andere Medien gehören. Alle Bilder werden dabei in einer zentralen Bobliothek gespeichert, so dass ein Bild in mehreren Beiträgen verwendet werden kann.

## Formatierung

Um den Text zu formatieren kann man einfach spezielle Zeichen im Text verwenden. Diese Art der Formatierung heißt [Markdown](https://de.wikipedia.org/wiki/Markdown#Auszeichnungsbeispiele).

### Überschiften

Überschriften werden mit '#' am Enfang der Zeile erzeugt.

* \# Große Überschrift
* \#\# Kleinere Überschrift
* \#\#\# Noch kleinere Überschrift

### Text und Absätze

Ein **Absatz** wird einfach mit einer Leerzeile erzeugt.

Ein **Zeilenumbruch** im laufenden Text (ohne Absatz) wird durch ein '\\' am Ende der Zeile erzeugt.

Im laufenden Text gehen folgende Formatierungen:

* **\*\*fett\*\***
* *\*kursiv\**
* ***\*\*\*fett und kursiv\*\*\****

### Aufzählungen

<pre>
* ein Punkt in einer Liste
  * ein Unterpunkt
  - es funktioniert auch '-'

1. ein Punkt in einer Aufzählung
2. ein weiterer Punkt
</pre>

### Links

Links werden in der folgenden Form geschrieben: \[*Text*\](*Ziel des Links*)

Für **interne** Ziele, also zum Beispiel Beiträge, kann beim Bearbeiten des Textes eine **Hilfe** aufgerufen werden. Dazu benutzt man die Tastenkombination:

* Strg + Space

#### Link Beispiele

* \[Hier geht es zu Google\](https:\\\\google.de) -> [Hier geht es zu Google](https:\\\\google.de)
* ...
