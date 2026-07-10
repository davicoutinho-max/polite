/** Strips everything but digits, e.g. "123.456.789-00" -> "12345678900". */
export function digitsOnly(value: string): string {
  return value.replace(/\D/g, '');
}

/** A CPF (individual taxpayer ID) has 11 digits. */
export function isValidCpf(value: string): boolean {
  return digitsOnly(value).length === 11;
}

/** A CNPJ (company/committee taxpayer ID) has 14 digits. */
export function isValidCnpj(value: string): boolean {
  return digitsOnly(value).length === 14;
}
