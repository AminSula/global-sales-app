# Pilot – Udhëzime instalimi

Paneli i administrimit dhe marrja e porosive nga telefoni.

## 1. Çfarë përmban dorëzimi

| Pjesa | Ku ndodhet |
|---|---|
| Versioni funksional (një skedar i vetëm) | `backend/target/sales-pilot-0.1.0.jar` |
| Kodi burimor i shërbimit në server (API) | `backend/` |
| Kodi burimor i panelit web dhe i aplikacionit për telefon | `frontend/` |
| Skripti i krijimit të tabelave | `docs/sql/krijimi-i-tabelave.sql` |
| Këto udhëzime | `docs/UDHEZIME-INSTALIMI.md` |

Paneli web dhe aplikacioni i telefonit janë i njëjti aplikacion: administratori sheh panelin,
agjenti sheh ekranet e porosisë. Telefoni komunikon vetëm me shërbimin në server; kredencialet e
SQL-së ndodhen vetëm në server dhe nuk ruhen asnjëherë në telefon.

## 2. Kërkesat

- **Java 17** ose më e re
- **PostgreSQL 14** ose më e re (testuar me versionin 16)
- Një shfletues i kohëve të fundit (Chrome, Edge, Safari, Firefox)

## 3. Instalimi

1. Krijoni një bazë të dhënash bosh:

   ```sql
   CREATE DATABASE sales_pilot;
   ```

2. Vendosni të dhënat e lidhjes si variabla të mjedisit në server:

   | Variabla | Kuptimi | Shembull |
   |---|---|---|
   | `DB_URL` | Adresa e bazës së të dhënave | `jdbc:postgresql://localhost:5432/sales_pilot` |
   | `DB_USER` | Përdoruesi i bazës | `pilot` |
   | `DB_PASSWORD` | Fjalëkalimi i bazës | |
   | `JWT_SECRET` | Çelësi sekret për hyrjet (të paktën 32 karaktere, i rastësishëm) | |
   | `ADMIN_PASSWORD` | Fjalëkalimi fillestar i administratorit | |
   | `AGENT_PASSWORD` | Fjalëkalimi fillestar i dy agjentëve | |

3. Nisni aplikacionin:

   ```
   java -jar sales-pilot-0.1.0.jar
   ```

   Herën e parë aplikacioni krijon vetë tabelat dhe tri llogaritë.

4. Hapni `http://<adresa-e-serverit>:8080` në shfletues.

## 4. Llogaritë

| Përdoruesi | Roli |
|---|---|
| `admin` | Administrator |
| `agent1` | Agjent shitjesh (Agent 1) |
| `agent2` | Agjent shitjesh (Agent 2) |

Fjalëkalimet janë ato të vendosura te `ADMIN_PASSWORD` dhe `AGENT_PASSWORD`. Nëse këto variabla
mungojnë, të tria llogaritë marrin fjalëkalimin provë `123456`, i cili duhet përdorur vetëm në
një kompjuter testimi.

Variablat lexohen vetëm kur krijohen llogaritë. Për të ndryshuar një fjalëkalim më vonë,
ekzekutoni në bazën e të dhënave:

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE app_user
SET password_hash = crypt('fjalekalimi-i-ri', gen_salt('bf', 10))
WHERE username = 'agent1';
```

## 5. Përdorimi në telefon

- Agjenti hap të njëjtën adresë në shfletuesin e telefonit dhe hyn me llogarinë e vet.
- Për ta instaluar si aplikacion ("Shto në ekranin kryesor"), adresa duhet të jetë **HTTPS**.
  Pa HTTPS aplikacioni punon normalisht në shfletues, por nuk instalohet.
- Kur mungon interneti shfaqet një njoftim. Porosia e plotësuar ruhet në telefon dhe dërgohet
  me butonin "Provo ta dërgosh përsëri"; ajo nuk regjistrohet kurrë dy herë.

## 6. Lidhja me të dhënat tuaja

Në këtë fazë produktet dhe klientët lexohen nga dy tabela prove (`erp_products` dhe
`erp_customers`) që krijohen bashkë me aplikacionin. Aplikacioni vetëm i **lexon** këto tabela;
butoni "Rifresko" i kopjon në tabelat e veta.

Për të lexuar nga tabelat tuaja reale, vendosni dy pyetje SQL në konfigurim
(`application.yml`), pa ndryshuar kodin:

```yaml
app:
  source:
    products-query: SELECT KodiArtikullit AS code, Emertimi AS name, Cmimi AS price, Gjendja AS stock FROM Artikujt
    customers-query: SELECT KodiKlientit AS code, Emri AS name, Adresa AS address FROM Klientet
```

Emrat e tabelave dhe të kolonave më sipër janë vetëm shembull. Secila pyetje duhet të kthejë
pikërisht kolonat `code, name, price, stock` dhe `code, name, address`.

Nëse baza juaj nuk është PostgreSQL (p.sh. Microsoft SQL Server), duhen edhe drejtuesi (driver)
përkatës dhe përshtatje të vogla të skriptit të tabelave. Kjo bëhet si hap më vete.

## 7. Tabelat që krijon aplikacioni

Të gjitha janë tabela të reja; asnjë tabelë ekzistuese nuk ndryshohet.

| Tabela | Përmbajtja |
|---|---|
| `sales_order` | Porosia: numri, klienti, agjenti, data, totali, statusi, kush dhe kur e miratoi |
| `sales_order_line` | Rreshtat: produkti, sasia, çmimi, zbritja, oferta, totali i rreshtit |
| `product`, `customer` | Kopja e produkteve dhe klientëve të lexuar nga SQL |
| `visit_assignment` | Klientët e caktuar për çdo agjent sipas ditës dhe rendit të vizitave |
| `offer`, `offer_product`, `offer_customer` | Ofertat (zbritjet në përqindje) |
| `app_user` | Llogaritë |

Skripti i plotë: `docs/sql/krijimi-i-tabelave.sql`.

## 8. Ndërtimi nga kodi burimor

Kërkohen edhe **Node.js 22.22.3** ose më i ri dhe **Maven** (ose IntelliJ IDEA, që e ka të përfshirë).

```
cd backend
mvn -Pfull clean package
```

Rezultati është `backend/target/sales-pilot-0.1.0.jar`, që përmban shërbimin dhe panelin web.

## 9. Para përdorimit real

Ky është një version pilot për mjedis testimi. Para përdorimit me të dhëna operative:

- vendosni `JWT_SECRET`, `ADMIN_PASSWORD` dhe `AGENT_PASSWORD` me vlera të forta;
- shërbejeni aplikacionin vetëm përmes HTTPS;
- planifikoni kopje rezervë (backup) të bazës së të dhënave.

## 10. Çfarë nuk përfshihet

Sipas marrëveshjes për këtë fazë nuk përfshihen: faturimi, fiskalizimi, llogaritja e TVSH-së,
ulja automatike e stokut dhe kampanjat e ndërlikuara. Stoku shfaqet siç lexohet nga SQL dhe nuk
ndryshohet nga porositë.
