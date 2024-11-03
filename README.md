## Uruchomienie projektu
### Konfiguracja agentów
Agenci, w zależności od rodzaju, przyjmują różne argumenty.
- SupervisorAgent:
  1. liczba zamówień, która ma być rozdysponowana
  2. liczba inicjalizatorów aukcji, do których mają być przesłane listy atomowych zadań
  3. tryb sortowania atomowych zadań (size, deadline, random)
- AdvanedResourceAgent:
  1. szerokość płyty drukarki [mm]
  2. długość płyty drukarki [mm]
  3. maksymalna wysokość drukowanego elementu [mm]
  4. prędkość drukowania [mm/godz]
  5. numer początkowego filamentu

Przykładowa komenda uruchamiająca program:
```
java
-cp thesis_project
jade.Boot
-gui
-name
the-platform
-agents
supervisor:agents.SupervisorAgent(15,2,deadline);
printer1:agents.AdvancedResourceAgent(100,100,200,50,1);
printer2:agents.AdvancedResourceAgent(100,100,200,50,2);
printer3:agents.AdvancedResourceAgent(100,100,200,50,3)
```

