## Uruchomienie projektu
### Konfiguracja agentów
Agenci, w zależności od rodzaju, przyjmują różne argumenty.
1. SupervisorAgent:
   - liczba zamówień, która ma być rozdysponowana
   - liczba inicjalizatorów aukcji, do których mają być przesłane listy atomowych zadań
   - tryb sortowania atomowych zadań 
3. AdvanedResourceAgent:
   - rozmiar płyty drukarki [mm²]
   - numer początkowego filamentu

Przykładowa komenda uruchamiająca program:
```
java
-cp thesis_project
jade.Boot
-gui
-name
the-platform
-agents
supervisor:agents.SupervisorAgent(5,2,size);
printer1:agents.AdvancedResourceAgent(6000,1);
printer2:agents.AdvancedResourceAgent(6000,2);
printer3:agents.AdvancedResourceAgent(6000,3)
```

