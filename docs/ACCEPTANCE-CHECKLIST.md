# Acceptance checklist

Every point of the client's brief, how to show it in the running app, and what should happen.
Run it once before the presentation; it takes about 20 minutes and doubles as the demo script.

## Before you start

1. Start PostgreSQL, the backend (`PilotApplication` in IntelliJ) and the frontend (`npm start`).
2. Run `backend/sql/demo-data.sql` in pgAdmin **on the day of the demo**. It is safe to re-run,
   and it creates the visit lists for *today*; lists from an earlier day do not carry over.
3. Open two browser windows: one as `admin`, one (phone width, or a real phone) as `agent1`.

## 1. Data from SQL

| # | Check | Expected |
|---|---|---|
| 1.1 | Admin → Produktet | About 20 products with code, name, price and stock |
| 1.2 | Admin → Klientët | About 10 customers with code, name and address |
| 1.3 | In pgAdmin: `UPDATE erp_products SET price = 400 WHERE code = 'P001';` then press **Rifresko** | Message says 1 changed; P001 shows 400.00 |

## 2. Web panel

| # | Check | Expected |
|---|---|---|
| 2.1 | Log in as `admin`, `agent1`, `agent2` | One administrator and two agents; each lands on their own area |
| 2.2 | Caktimet → pick Agent 1 and today → add three customers, reorder with the arrows, **Ruaj** | List saved in that order |
| 2.3 | Switch to Agent 2 on the same day | Those three customers are greyed out as "Caktuar te Agent 1" |
| 2.4 | Ofertat → new offer: 10%, one product, all customers | Offer listed as active; the product shows the offer on Produktet |
| 2.5 | As `agent1`, open a customer and find that product | Original price struck through, discounted price, "−10%" and the offer name |

## 3. Order from the phone

| # | Check | Expected |
|---|---|---|
| 3.1 | Agent → Sot | Only Agent 1's customers for today, in the saved visit order |
| 3.2 | Open a customer, add products with the + buttons | Stock, price, discount and a running total |
| 3.3 | **Shiko porosinë** | Each line shows quantity × price, the discount amount and the line total; then subtotal, discounts and total |
| 3.4 | **Dërgo porosinë** | "Porosia u dërgua" with an order number and status "Në pritje" |

## 4. Saved in SQL, and approval

| # | Check | Expected |
|---|---|---|
| 4.1 | In pgAdmin: `SELECT * FROM sales_order ORDER BY id DESC LIMIT 1;` and `SELECT * FROM sales_order_line WHERE order_id = <that id>;` | The order and its lines: number, customer, agent, date, products, quantities, prices, discounts, total, status |
| 4.2 | Admin → Porositë | The new order is at the top as "Në pritje" |
| 4.3 | Open it → **Shëno si të miratuar** → confirm | Status "E miratuar", with who approved and when |
| 4.4 | Agent → Porositë → **Rifresko** | The order now shows "E miratuar" |

## 5. Basic controls

| # | Check | Expected |
|---|---|---|
| 5.1 | Totals: compare the review screen with a calculator for one discounted line | quantity × price × (100 − discount) ÷ 100, rounded to two decimals |
| 5.2 | Double tap: on the review screen tap **Dërgo porosinë** several times quickly | One order only (check Porositë, or count rows in `sales_order`) |
| 5.3 | Own data only: log in as `agent2` | Different customers; none of Agent 1's orders |
| 5.4 | Own data only (direct address): as `agent2`, open `/agent/order/<id of one of Agent 1's customers>` | "Ky klient nuk të është caktuar për sot." |
| 5.5 | Survives closing: add products to an order, close the tab, open the app again | Still logged in; the customer shows "Porosi e papërfunduar" and the quantities are back |
| 5.6 | No internet: in DevTools → Network choose **Offline**, then send | Offline banner; message that the order is kept on the phone; nothing lost |
| 5.7 | Back online: switch Offline off → **Provo ta dërgosh përsëri** | The order is sent once |
| 5.8 | Old orders keep their price: note an order's line price and total, change that product's price in `erp_products`, press **Rifresko**, reopen the order | The old order is unchanged; a new order uses the new price |
| 5.9 | Price changed mid-order: start an order as agent, change a product's price and Rifresko as admin, then send | "Çmimet ose ofertat kanë ndryshuar…"; the new total appears; send again to confirm |

## 6. Environment and security

| # | Check | Expected |
|---|---|---|
| 6.1 | Phone talks only to the API | In DevTools → Network every request goes to `/api/...`; there is no database address or password anywhere in the phone's storage |
| 6.2 | No operational data touched | The app writes only to its own tables (list in `docs/UDHEZIME-INSTALIMI.md`, section 7) |

## 7. Single file and installable phone app

1. Stop the backend in IntelliJ and stop `npm start`.
2. IntelliJ → Maven tool window → **Profiles** → tick `full` → **Lifecycle** → run `clean`, then `package`.
   The first run downloads Node and takes a few minutes.
3. Run `java -jar backend\target\sales-pilot-0.1.0.jar` and open http://localhost:8080.
4. For the phone: `cloudflared tunnel --url http://localhost:8080` (or `ngrok http 8080`) gives an
   HTTPS address. Open it on the phone, then use the browser menu → "Add to Home screen".

| # | Check | Expected |
|---|---|---|
| 7.1 | http://localhost:8080 | The whole app from one jar, no `npm start` needed |
| 7.2 | Reload the page on `/admin/orders` | The page reloads normally (no 404) |
| 7.3 | Phone, HTTPS address → Add to Home screen | A "Pilot" icon; it opens full screen without the browser bar |
| 7.4 | Installed app with the phone in flight mode | The app still opens and shows the offline banner |

## Extras beyond the brief

- **Përmbledhje:** today's numbers, pending approvals, progress per agent.
- **Raportet:** totals for a period, value per day, by agent, by offer, top products and customers.
- **Ofertat:** offers for chosen customers, with dates and pause, in addition to the simple discount.
- **Albanian and English**, light and dark theme.
