# Tesi - Clientside Encryption Driver
Sviluppo di un driver per l'abilitazione della cifratura a livello client per il lavoro di tesi riguardo la Database Encryption.

## Scenario

La **Clientside Encryption**  è una tecnica crittografica che consiste nel cifrare i dati client-side ovvero sul client prima di essere trasmessi ad un server online.


![ClientsideEncryption](./docs/images/ClientSideEnc.png)
## Architettura del software
La seguente figura mostra la struttura del Clientside Encryption Package e i suoi possibili utilizzi da parte di una applicazione client.

![Architettura](./docs/images/Architettura.png)

Il software si comporta come una interfaccia per accedere al database ed è composto da un package denominato Core e da una classe denominata CryptoDatabaseAdapter.

### Package Core
Il package ***Core*** rappresenta il nucleo del sistema ed è composto da diversi package per la gestione del database, keystore, crittografia e logging. 

- Package *Database*: gestisce la comunicazione con il database.
- Package *Crypto*: gestisce le operazioni crittografiche.
- Package *Keystore*: gestisce le operazioni sul keystore.
- Package *Token*: modella un Token (è il dato salvato sul DB).
- Package *Logger*: gestisce il logging su file.
- Package *Exceptions*: contiene tutte le eccezioni utilizzate.
- Package *Examples*: contiene tre piccoli programmi che mostrano l'utilizzo del package Core.

#### Package Database
La seguente immagine mostra il diagramma UML del package:

![DatabaseUML](./src/docs/images/Package_database.png)






