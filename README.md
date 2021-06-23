# Projet TinyPet WebAndCloud M1 MIAGE

<strong>Frontend code :</strong> https://github.com/PERRETPY/petition-front

<strong>Link for web application :</strong> https://tinypet-317220.appspot.com/

<strong>Link for backend explorer :</strong> https://petition-305717.appspot.com/

<strong>Membres du projet :</strong> Perret Pierre-Yves et Augereau Yan


# Endpoints

| Nom                                     | Methode | URL                                    | Connexion requise | Commentaire |
|--------------------------------------------|---------|----------------------------------------|-------------------|-------------|
| Voir les pétitions les plus signées        | GET     | /petitions/top100                      | ❌ | Les pétitions les plus signées triées par nombre de signature, date de création descendant|
| Afficher une pétition précise             | GET     | /petition?id={id}                      | ❌ |
| Signer une pétition                        | PUT   | /signPetition?id={id}                      | ✔️  |
| Créer une pétition                         | POST    | /petitions                             | ✔️  |
| Voir les pétitions que j'ai créé           | GET     | /myPetition                          | ✔️  |
| Voir les pétitions que j'ai signé          | GET     | /petitionSigned                         | ✔️  |
| Chercher des pétitions          | GET     | /search?query={query}                         | ✔️  | Cherche les pétitions sur le titre et les tags |

# Schema des pétitions dans le Datatstore

## Petition
| Field      | Type       | Usage                                          |
|------------|------------|------------------------------------------------|
| key        | String     | {reversed_creation_timestamp}:{owner}          |
| title       | String     | Nom de la pétition                             |
| owner      | String     | Adresse mail du créateur                       |
| nbSignature  | Integer    | Nombre de vote pour cette pétition             |
| description    | String     | Description du sujet de la pétition            |
| tags       | StringList | Ensemble des tags pour cette pétition          |


## Signature
| Field   | Type       | Usage                                |
|---------|------------|--------------------------------------|
| key     | String     | {user.key}             |
| petitions  | StringList | Liste des pétitions signées par user |


## User
| Field   | Type       | Usage                                |
|---------|------------|--------------------------------------|
| key     | String     | adresse mail             |
| firstName  | StringList | prénom |
| lastName  | StringList | nom |



## Index

```yaml
indexes:

indexes:
  - kind: Petition
    properties:
      - name: titre
      - name: tag

  - kind: Petition
    properties:
      - name: nbSignature
        direction: desc
      
  - kind: Petition
    properties:
      - name: signatories
      - name: titre
  
  - kind: Signature
    properties:
      - name: petitions
        direction: asc

```
