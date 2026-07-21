/** Strips everything but digits, e.g. "123.456.789-00" -> "12345678900". */
export function digitsOnly(value: string): string {
  return value.replace(/\D/g, '');
}

/** Validates a CPF (individual taxpayer ID) by its official check-digit algorithm — not just
 * length, since any 11-digit string (including repeated-digit sequences like "111.111.111-11")
 * would otherwise pass. */
export function isValidCpf(value: string): boolean {
  const cpf = digitsOnly(value);
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
    return false;
  }

  const digits = cpf.split('').map(Number);
  const checkDigit = (length: number): number => {
    let sum = 0;
    for (let i = 0; i < length; i++) {
      sum += digits[i] * (length + 1 - i);
    }
    const remainder = sum % 11;
    return remainder < 2 ? 0 : 11 - remainder;
  };

  return checkDigit(9) === digits[9] && checkDigit(10) === digits[10];
}

/** Progressively masks raw digits into the "000.000.000-00" CPF pattern as the user types. */
export function formatCpf(value: string): string {
  const digits = digitsOnly(value).slice(0, 11);
  const parts = [digits.slice(0, 3), digits.slice(3, 6), digits.slice(6, 9)].filter(Boolean);
  let result = parts.join('.');
  if (digits.length > 9) {
    result += `-${digits.slice(9, 11)}`;
  }
  return result;
}

/** Validates a CNPJ (company/committee taxpayer ID) by its official check-digit algorithm. */
export function isValidCnpj(value: string): boolean {
  const cnpj = digitsOnly(value);
  if (cnpj.length !== 14 || /^(\d)\1{13}$/.test(cnpj)) {
    return false;
  }

  const digits = cnpj.split('').map(Number);
  const checkDigit = (length: number): number => {
    const weights = length === 12 ? [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2] : [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
    let sum = 0;
    for (let i = 0; i < length; i++) {
      sum += digits[i] * weights[i];
    }
    const remainder = sum % 11;
    return remainder < 2 ? 0 : 11 - remainder;
  };

  return checkDigit(12) === digits[12] && checkDigit(13) === digits[13];
}
