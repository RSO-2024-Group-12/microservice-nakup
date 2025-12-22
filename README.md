# Nakup (microservice-nakup)

Mikrostoritev omogoča izvedbo plačila preko storitve PayPal in pošiljanje računa preko e-pošte.

## Namen

- izvedba plačila z uporabo PayPal
- preverjanje izvedbe plačila 
- pošiljanje računa na e-poštni naslov kupca

## Tehnologije

- Java 21
- Quarkus
- REST
- PayPal SDK
- XHTMLRenderer
- OpenAPI
- Swagger

## Integracije

### Odvisnosti

Spodaj so navedene mikrostoritve, ki jih microservice-izdelki uporablja za svoje delovanje.

| Mikrostoritev          | Komunikacija | Namen                                |
|------------------------|--------------|--------------------------------------|
| microservice-kosarica  | gRPC         | pridobitev košarice uporabnika       |
| microservice-kosarica  | gRPC         | izbris košarice uporabnika po nakupu |
| microservice-skladisce | REST (POST)  | dodajanje novega dogodka v skladišče |
| microservice-narocila  | REST (POST)  | dodajanje novega naročila            |

### Zunanji API

Spodaj so navedeni zunanji API-ji, ki jih microservice-izdelki uporablja za svoje delovanje.

| Zunanji API | Komunikacija | Namen                                     |
|-------------|--------------|-------------------------------------------|
| paypal-api  | REST (POST)  | pridobitev redirect url za izvedbo nakupa |
| paypal-api  | REST (POST)  | preverjanje uspešnosti izvedbe plačila    |

## API

### REST
- `POST /v1/nakup/start` - pridobitev redirect url za plačilo
- `POST /v1/nakup/confirm` - preverjanje plačila in pošiljanje računa preko e-pošte

Podrobna dokumetacija je na voljo preko **OpenAPI (Swagger UI)**.

## Zagon

Zagon v dev načinu.

```shell script
./mvnw quarkus:dev
```