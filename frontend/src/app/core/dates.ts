/** Turns a date on this device into yyyy-MM-dd (the format the API and <input type="date"> use). */
export function toIsoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

/** Today's date on this device as yyyy-MM-dd. */
export function todayIso(): string {
  return toIsoDate(new Date());
}

/** The date that many days before today, as yyyy-MM-dd (0 = today). */
export function daysAgoIso(days: number): string {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return toIsoDate(date);
}

/** The first day of the current month, as yyyy-MM-dd. */
export function monthStartIso(): string {
  const now = new Date();
  return toIsoDate(new Date(now.getFullYear(), now.getMonth(), 1));
}
