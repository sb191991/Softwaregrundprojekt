Das Softwaregrundprofekt ging über 2 Semester, wobei wir im ersten Teil anhand eines Lastenheftes in einem Scrum-Team 
die Architektur und die Unterlagen Schritt für Schritt vorbereiten mussten. Im zweiten Teil musste jedes Team für sich 
die Entwicklungsumgebung bestimmen. Wir haben uns serverseitig für Node.js entschieden und den Client haben wir mit Verwendung 
des Java-Frameworks LibGDX entwickelt. Für die Kommunikation zwischen den Komponenten wird die Dateiformat JSON verwendet und das
WebSocket Protokoll für eine bidirektionale Verbindung zwischen Client und Server herzustellen.


verwendete Technologien bei der Entwicklung der Software:
https://github.com/sb191991/Softwaregrundprojekt/blob/master/Dokumentation/Auflistung%20verwendeter%20Anwendungen.docx

Server von der Console starten: 
node server --config-charset "./characters.json" --config-match "./exampleMatch.json" --config-scenario "./exampleScenario.json" --x '{\"npcCount\":3}' --verbose
