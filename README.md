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

![DatabaseUML](./docs/images/Package_database.png)

##### DatabaseManager class
Si tratta di una classe dove il costruttore richiede tre stringhe ovvero l'url del database, username e password per accedere al database. Offre i seguenti metodi:
- connect(): permette la connessione al DB con i parametri specificati nel costruttore.
- runImmutableQuery(Query): esegue una query di tipo SELECT.
- runMutableQuery(Query): esegue una query non di tipo SELECT ma che modifica il database (ad esempio UPDATE).

##### Query class
Modella una query da inviare al database. Il costruttore richiede una stringa che corrisponde ad una query parametrizzata dove il parametro è segnato con un '?' (ad esempio SELECT + FROM TABELLA WHERE id=?). Offre il seguente metodo:
- setParameter(int,String): permette di settare una stringa come parametro alla posizione specificata.

##### Tuple class
Modella una tupla ritornata dal database. Viene usata per ritornare i risultati di na SELECT al client. Offre i seguenti metodi:
- setColumn(String, String): permette di inserire il mapping tra il nome della colonna e l'attributo (ad esempio ID: 1).
- toString(): ritorna l'oggetto Tuple in formato stringa che corrisponde ad una mappa in formato stringa.

#### Package Crypto
La seguente immagine mostra il diagramma UML del package:

![CryptoUML](./docs/images/Package_crypto.png)

È composto da una classe static che offre il supporto alle operazioni crittografiche. Gli algoritmi supportati sono quelli dell'enum Algorithm e sono AES128, AES192 e AES256 tutti in modalità GCM, preferito per i vantaggi dell'authenticated encryption (non solo confidenzialità ma anche verifica dell'integrità e autenticazione).
Offre i seguenti metodi:
- createKeyFromPassword(Algorithm, char[], byte[]): permette di creare una chiave segreta partendo da una password ed applicando una Key Derivation Function che prende come input il sale e la password.
- CreateSymKey(Algorithm): permette di creare una chiave simmetrica per l'algoritmo specificato.
- decryptData(SecretKey,byte[],byte[]): permette di decrifrare dei dati utilizzando la chiave simmetrica e l'iv passati come parametri.
- decryptDataWithPrefixIV(SecretKey, byte[]): permette di estrarre l'iv dai dati cifrati ed infine decifrarli.
- encryptData(SecretKy, byte[], byte[]): permette di cifrare dei dati utilizzando la chiave simmetrica e l'iv passati come parametri.
- encryptDataWithPrefixIV(SecretKey, byte[]): permette di cifrare i dati ed inserire l'iv in essi.
- getRandomNonce(int): ritorna un nonce della dimensione specificata.

#### Package Keystore
La seguente immagine mostra il diagramma UML del package:

![KeystoreUML](./docs/images/Package_keystore.png)

##### KeystoreInfo class
Si tratta di una classe che modella le informazioni del keystore ovvero il keystore stesso (un file pkcs12) e la password che lo protegge. Il costruttore riceve come parametri un oggetto di tipo Keystore e una stringa che rappresenta la password.

##### KeystoreUtils class
Si tratta una classe static che offre il supporto alla gestione del keystore. Il tipo di keystore supportato è un file .p12.
Offre i seguenti metodi:
- createKeystore(String): permette di creare un oggetto di tipo Keystore e proteggerlo con la password passata come parametro.
- deleteKeystore(String): permette di eliminare il keystore salvato al path del filesystem specificato.
- deleteKey(KeystoreInfo, String): permette di eliminare la chiave con il nome specificato dal keystore passato come parametro.
- existKey(KeystoreInfo, String): permette di verificare se uno specifico keystore contiene una chiave dove il nome è passato come parametro.
- existKeystore(String, String): permette di verificare se uno specifico keystore esiste nel filesystem.
- getKey(KeystoreInfo, String): ritorna la chiave specificata dal keystore.
- insertKey(KeystoreInfo, SecretKey, String): permette di inserire una chiave nel keystore protetto dalla password specificata.
- loadKeystore(String, String): permette di caricare il keystore in memoria dal filesystem.
- saveKeystore(KeystoreInfo, String): permette di salvere il keystore nel filesystem.

#### Package Token
La seguente immagine mostra il diagramma UML del package:

![TokenUML](./docs/images/Package_token.png)





