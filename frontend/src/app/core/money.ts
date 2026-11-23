/**
 * Money maths for what the agent SEES on the phone. It uses whole cents so there are no
 * floating-point surprises, and it is the same formula the server uses when it saves the order:
 *
 *   line total = quantity x unit price x (100 - discount %) / 100, rounded to 2 decimals (half up)
 *
 * The server always recalculates; if its total differs from the one shown here, it refuses the
 * order and the phone reloads the prices.
 */
export function toCents(amount: number): number {
  return Math.round(amount * 100);
}

export function fromCents(cents: number): number {
  return cents / 100;
}

/** quantity must be a whole number; discountPercent may have up to 2 decimals. */
export function lineTotalCents(quantity: number, unitPrice: number, discountPercent: number): number {
  const percentHundredths = Math.round(discountPercent * 100);
  return Math.round((quantity * toCents(unitPrice) * (10000 - percentHundredths)) / 10000);
}
