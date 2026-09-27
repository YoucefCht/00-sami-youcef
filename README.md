# Projet Bibliothèque

Fait par Youcef Cheriet et Sami BENABDALLAH.

Petite app de gestion de bibliothèque : on gère les livres, les adhérents et les emprunts.

## Comment ça marche

- `catalog-rpc-server` : le serveur gRPC qui gère les livres et le stock (port 9090)
- `library-rest-service` : l'API REST pour les adhérents et les emprunts, elle appelle le serveur catalogue en gRPC (port 8080)
- `web-client` : une page HTML simple pour tester l'API

Chaque service a sa propre base H2 en mémoire.

Le client web appelle le REST, le REST appelle le RPC quand il faut vérifier ou réserver un livre.

## Lancer le projet

Il faut Java 17 et Maven.

Terminal 1 :
```
cd catalog-rpc-server
mvn spring-boot:run
```

Terminal 2 :
```
cd library-rest-service
mvn spring-boot:run
```

Après ça ouvrir `web-client/index.html` dans le navigateur.

## API REST

Base : `http://localhost:8080/api`

- `GET /books` : liste des livres
- `GET /books/{id}` : un livre
- `POST /books` : ajouter un livre
- `GET /members` : liste des adhérents
- `POST /members` : créer un adhérent
- `GET /loans` : liste des emprunts
- `POST /loans` : emprunter un livre
- `PUT /loans/{id}/return` : rendre un livre

Exemple pour emprunter :
```
curl -X POST http://localhost:8080/api/loans -H "Content-Type: application/json" -d '{"memberId":1,"bookId":3}'
```

## Tests

On a testé avec `mvn test` dans chaque service, ça passe :

- catalog-rpc-server : 4 tests OK
- library-rest-service : 5 tests OK

github :
https://github.com/YoucefCht/00-sami-youcef
