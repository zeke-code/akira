# 📱 **Relazione Laboratorio di Programmazione di Sistemi Mobili - Akira Financial Tracker**

## **Indice dei Contenuti**

1. [Introduzione](#introduzione)
2. [Funzionalità Principali](#funzionalità-principali)
3. [Descrizione della Struttura dei Sorgenti](#descrizione-della-struttura-dei-sorgenti)
4. [Punti di forza](#punti-di-forza)
5. [Possibili Migliorie](#possibili-migliorie)

---

## **Introduzione**

**Akira** è un'applicazione mobile progettata per semplificare la gestione delle finanze personali.  
Il suo obiettivo principale è aiutare gli utenti a monitorare le proprie spese e a mantenere il controllo sul proprio budget mensile.
L'idea per questa applicazione deriva dalla ricerca di qualche applicazione che mi permettesse di tenere traccia delle mie finanze
senza essere invasiva. Le applicazioni sul Play Store richiedono parecchi permessi o account per essere utilizzate, e i progetti open source riguardo applicazioni
del genere spesso vengono abbandonati o sono realizzati con tech stack vecchi.

Al primo avvio, l'utente imposta il budget mensile che desidera non superare. Successivamente, inserendo entrate e spese, l'app tiene traccia del consumo di budget e fornisce una panoramica dettagliata delle transazioni, includendo:

- Data dell'operazione
- Categoria di spesa
- Categoria di transazione (entrata o spesa)
- Descrizione dettagliata

Inoltre, **Akira** offre la possibilità di visualizzare grafici interattivi per analizzare l'andamento finanziario e consente di consultare in tempo reale l'andamento delle azioni di qualsiasi azienda.

---

## **Funzionalità Principali**

- 📊 **Monitoraggio del Budget**: Gestione delle entrate e delle uscite con aggiornamento in tempo reale del budget residuo.
- 📅 **Storico delle Transazioni**: Visualizzazione dettagliata di tutte le operazioni registrate con data, categoria e descrizione.
- 📈 **Grafici e Statistiche**: Analisi visiva delle spese tramite grafici intuitivi e statistiche personalizzate.
- 💹 **Monitoraggio Azionario**: Consultazione dell'andamento delle azioni di qualsiasi azienda direttamente dall'app.

---

## **Descrizione della Struttura dei Sorgenti**

L'applicazione è stata realizzata seguendo l'architettura [MVVM](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel).
Ho scelto di utilizzare **una sola activity**, ovvero `MainActivity` per seguire il principio di Single Page Application e ottimizzare le performance il più possibile.
`MainActivity` si comporta quindi come contenitore di tutte le parti dell'app, includendo la bottom navbar per la navigazione e renderizzando i fragment al suo interno.

```bash
Akira/
├── ui/
│ ├── activities/
│ │ ├── MainActivity.kt
│ │ └── FirstSetupActivity.kt
│ ├── adapters/
│ │ └── TransactionAdapter.kt
│ ├── fragments/
│ │ ├── HomeFragment.kt
│ │ ├── TransactionsFragment.kt
│ │ ├── StatisticsFragment.kt
│ │ └── StocksFragment.kt
│ └── viewmodels/
│ ├── HomeViewModel.kt
│ ├── TransactionsViewModel.kt
│ ├── StatisticsViewModel.kt
└────────└── StocksViewModel.kt
```

Nella parte di UI (pacchetto `ui`) del progetto abbiamo quindi 4 pacchetti principali:

- activities: contiene `MainActivity` e `FirstSetupActivity`.
  - `MainActivity` rappresenta il nostro contenitore principale di UI. Al suo interno aggiorna quindi tutti i fragment, e utilizza la navbar per permettere all'utente di muoversi all'interno dell'applicazione. Si occupa inoltre di notificare all'utente eventi importanti come la richiesta del permesso di notifiche.
  - `FirstSetupActivity` rappresenta la prima Activity che l'utente vede se non ha mai aperto completato il processo di setup, che prevede l'inserimento del proprio nome,
    budget mensile desiderato, tipo di valuta. Una volta completato il setup, l'applicazione salverà nelle _SharedPreferences_ gli input dell'utente.
- adapters: contiene tutti gli adapter dell'applicazione.
  - `CategoryAdapter`: adapter per rappresentare la lista di categorie disponibili per le transazioni all'interno dell'applicazione.
  - `SettingsAdapter`: adapter per rappresentare la lista di impostazioni disponibili alle modifiche all'interno dell'applicazione.
  - `SuggestionsAdapter`: adapter per rappresentare i suggerimenti quando l'utente cerca gli stocks di un'azienda.
  - `TransactionAdapter`: adapter essenziale che rappresenta la lista delle transazioni nel `HomeFragment`.
- fragments: contiene tutti i fragment dell'applicazione.
  - `HomeFragment`: fragment che mostra all'utente la lista delle proprie transazioni, i loro dettagli ed il budget rimanente. Permette inoltre di modificare le transazioni ed eliminarle.
  - `CreateFragment`: fragment che permette di creare nuove transazioni all'interno dell'applicazione.
  - `StatsFragment`: fragment che permette di consultare grafici riguardo le proprie transazioni.
  - `StocksFragment`: fragment che permette di consultare l'andamento delle azioni di un'azienda.
  - `SettingsFragment`: fragment che permette di consultare e modificare le impostazioni dell'applicazione.
  - `SelectCategoryDialogFragment`: fragment che ospita un dialog per la selezione di categorie per le proprie transazioni.
- viewmodels: contiene tutti i viewmodel dell'applicazione, inclusi Fragment e Dialog viewmodels.
  - `MainViewModel`: viewmodel che ospita i dati osservati da `MainActivity` e si occupa di registrare i worker, oltre che effettuare chiamate API a GitHub.
  - `FirstSetupViewModel`: viewmodel che ospita i dati osservati da `FirstSetupActivity` e si occupa di salvare i dati registrati dall'utente.
  - `HomeViewModel`: viewmodel che ospita i dati osservati da `HomeFragment` e che permette di modificare le transazioni.
  - `CreateViewModel`: viewmodel che ospita i dati osservati da `CreateFragment` e che permette di creare nuove transazioni.
  - `StatsViewModel`: viewmodel che ospita i dati osservati da `StatsFragment`.
  - `SettingsViewModel`: viewmodel che ospita i dati osservati da `SettingsFragment` e che permette di modificare le `SharedPreferences` dell'applicazione.
  - `StocksViewModel`: viewmodel che ospita i dati osservati da `StocksFragment` e che effettua chiamate API ad [**AlphaVantage**](https://www.alphavantage.co/).

Per quanto riguarda la gestione dei modelli e dei dati, avendo seguito l'architettura **MVVM**, il progetto dispone di repository che si comportano come
**Single Source Of Truth** per quanto riguarda il recupero di dati, sia riguardante le transazioni, che dati riguardanti l'utente come il nome, il budget mensile,
il tipo di valuta utilizzato, e così via.

Sussistono inoltre i modelli dei tipi di dati recuperati attraverso chiamate API (nel progetto vengono effettuate molteplici chiamate a GitHub e Alpha Vantage)
e i relativi servizi.

```bash
Akira/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── BudgetDao.kt
│   │   │   ├── CategoryDao.kt
│   │   │   ├── EarningDao.kt
│   │   │   └── ExpenseDao.kt
│   │   ├── database/
│   │   │   └── AkiraDatabase.kt
│   │   ├── entities/
│   │   │   ├── BudgetModel.kt
│   │   │   ├── CategoryModel.kt
│   │   │   ├── EarningModel.kt
│   │   │   ├── EarningWithCategory.kt
│   │   │   ├── ExpenseModel.kt
│   │   │   ├── ExpenseWithCategory.kt
│   │   │   ├── SettingItem.kt
│   │   │   └── TransactionModel.kt
│   │   └── repository/
│   │       ├── FinancialRepository.kt
│   │       ├── StocksRepository.kt
│   │       └── UserRepository.kt
│   └── remote/
│       ├── api/
│       │   ├── AlphaVentureService.kt
│       │   └── GithubApiService.kt
│       ├── models/
│          ├── GithubReleaseModel.kt
│          ├── StockQuoteModel.kt
│          └── TimeSeriesDailyModel.kt
```

Nel pacchetto `data` abbiamo quindi:

- local

  - dao: contiene tutti i DAO (Data Access Object) per le entità della nostra applicazione, definendo le interfacce per interagire con il database.
    - `BudgetDao`: contiene l'interfaccia per modificare e aggiornare il proprio budget.
    - `CategoryDao`: contiene l'interfaccia per effettuare operazioni CRUD sulle categorie.
    - `EarningDao`: contiene l'interfaccia per effettuare operazioni CRUD sulle entrate.
    - `ExpenseDao`: contiene l'interfaccia per effettuare operazioni CRUD sulle spese.
  - database: contiene il database dell'applicazione e i dati già pregenerati per le categorie.
  - entities: contiene tutte le entità presenti nel database.
  - repository: contiene tutte le repository del progetto che agiscono come **Unique Source Of Truth** per i dati.

    - **`BudgetModel.kt`**  
       Rappresenta il modello per la gestione del budget mensile impostato dall'utente.  
       Contiene informazioni come l'importo e il mese di riferimento.

    - **`CategoryModel.kt`**  
      Definisce le categorie disponibili per le transazioni (ad esempio, "Cibo", "Trasporti", "Intrattenimento").  
      Ogni categoria ha un nome, un'icona associata e un ID.

    - **`TransactionModel.kt`**  
      Modello generico che rappresenta una transazione, sia essa un'**entrata** che una **spesa**.  
      Permette di gestire in modo uniforme tutte le operazioni finanziarie.

    - **`EarningModel.kt`**  
      Modello per la gestione delle **entrate**.  
      Include dettagli come l'importo, la data di registrazione, la descrizione e la categoria associata.

    - **`EarningWithCategory.kt`**  
      Modello che combina le informazioni di un'**entrata** con la relativa **categoria**.  
      Serve per ottenere dati completi nelle query con join tra entrate e categorie.

    - **`ExpenseModel.kt`**  
      Modello per la gestione delle **spese**.  
      Contiene dettagli come l'importo, la data di registrazione, la descrizione, e la categoria collegata.

    - **`ExpenseWithCategory.kt`**  
      Modello che associa una **spesa** alla sua **categoria**.  
      Utilizzato per semplificare le operazioni di visualizzazione e analisi dei dati.

    - **`SettingItem.kt`**  
      Modello per la gestione delle **impostazioni** dell'app.  
      Memorizza le preferenze utente, come le notifiche attive, la valuta selezionata o altre configurazioni personalizzabili.

    - **`FinancialRepository.kt`**
      Repository che restituisce tutti i dati riguardanti le finanze dell'utente

    - **`StocksRepository.kt`**
      Repository che restituisce tutti i dati riguardanti le azioni delle aziende

    - **`UserRepository.kt`**
      Repository che restituisce tutte le informazioni riguardo l'utente (SharedPreferences, impostazioni di appi)

- remote

  - api: contiene i servizi per l'utilizzo di API remote, definendo i parametri da utilizzare per le chiamate.

    - **`AlphaVentureService.kt`**  
      Servizio che si occupa delle chiamate API verso [**Alpha Vantage**](https://www.alphavantage.co/), una piattaforma di dati finanziari.  
      Consente di ottenere informazioni aggiornate sull'andamento di titoli azionari, come prezzi correnti, variazioni giornaliere e serie temporali dei dati di mercato.

    - **`GithubApiService.kt`**  
      Servizio per l'interazione con le API di **GitHub**.  
      Utilizzato per controllare la presenza di nuove release dell'applicazione o aggiornamenti importanti disponibili sul repository GitHub.

  - models: raccoglie i modelli per leggere correttamente i dati provenienti da remoto.

    - **`GithubReleaseModel.kt`**  
      Modello che rappresenta i dati di una **release** di un progetto su GitHub.  
      Include informazioni come il tag name, gli assets e il link per il download.  
      È usato per notificare all'utente eventuali aggiornamenti disponibili per l'app.

    - **`StockQuoteModel.kt`**  
      Modello che gestisce i **dati finanziari** in tempo reale di un titolo azionario.  
      Contiene informazioni come il prezzo corrente, il volume di scambio e le variazioni percentuali giornaliere.  
      Viene utilizzato nel `StocksFragment` per mostrare i dati aggiornati delle azioni.

    - **`TimeSeriesDailyModel.kt`**  
      Modello per la gestione dei dati storici giornalieri di un titolo azionario.  
      Include una serie temporale con i prezzi di apertura, chiusura, massimo e minimo, oltre al volume giornaliero.  
      Serve per alimentare i grafici di andamento dei titoli azionari e fornire analisi più approfondite.

La gestione delle dipendenze è stata ottimizzata tramite **[Hilt](https://developer.android.com/training/dependency-injection/hilt-android?hl=it)**, una libreria ufficiale di Google per l'iniezione di dipendenze in Android.

L'utilizzo di Hilt ha permesso di semplificare e standardizzare la configurazione delle dipendenze, riducendo il boilerplate code e migliorando la scalabilità dell'applicazione. I moduli definiti nel pacchetto `di` consentono di gestire in modo efficiente l'iniezione di repository, ViewModel e componenti di rete, garantendo coerenzae e testabilità del codice.

Nel pacchetto `utils` sussistono tutti quei moduli che non hanno un posto ben preciso nell'architetture **MVVM**, ma che sono comunque utilizzati comunemente da
parecchi moduli. Un esempio di contenuto di `utils` è il modulo `DateUtils` che predispone di funzione per la conversione di variabili da Unix Epoch Time a date leggibili
dall'essere umano.

All'interno del pacchetto `workers`, predisponiamo di tutti i worker dell'applicazione per attività in background o comunque programmate. Al momento è presente solo un worker
per ricordare all'utente di registrare le proprie transazioni all'interno dell'app.

## **Punti di forza**

- **Privacy:** l'applicazione non richiede permessi invasivi e conserva tutti i dati in locale, garantendo un elevato livello di protezione della privacy.
- **Performance Ottimizzate:** l'utilizzo di una sola Activity ha semplificato la gestione del ciclo di vita dei vari elementi, riducendo il rischio di crash e migliorando le prestazioni generali.
- **Architettura Scalabile:** l'adozione dell'architettura MVVM e della Dependency Injection con Hilt facilita l'aggiunta di nuove funzionalità e la manutenzione del codice.
- **UI moderna e molto semplice:** la UI è molto semplice e intuitiva, permettendo all'utente di fare tutto quello di cui ha bisogno in pochi tap.

## **Possibili Migliorie**

L'applicazione ha avuto uno sviluppo un po' travagliato. All'inizio era stata progettata per essere semplicemente un'applicazione a più view; il passaggio ad applicazione ad Activity singola mi ha rubato un po' di tempo che potrebbe essere stato investito per rifiniture varie e aggiunta di più feature.

Ecco i punti che sarebbe possibile migliorare:

1. L'utilizzo di JetPack Compose avrebbe potuto giovare molto all'applicazione, riducendo boilerplate code e riutilizzando elementi di UI più volte all'interno delle viste (i file XML sono inoltre noiosi da mantenere). La libreria non è stata utilizzata poiché lo sviluppo iniziò con l'approccio delle Views per attenersi di più ai contenuti del corso.
2. La vista delle statistiche può essere migliorata, offrendo più dati e un reload dei dati nei grafici migliori per migliorare le performance.
3. La vista per la creazione di transazioni potrebbe essere più carina e andrebbe riprogettata, poiché al momento molto grezza anche se funzionale.
4. Una feature sicuramente utile sarebbe la creazione personalizzata di categorie, non implementata per mancanza di tempo.
5. L'utilizzo di più worker per eseguire attività in background come generazione di report riguardo allo stato delle proprie finanze e notifiche più significative sarebbe stato sicuramente utile.
