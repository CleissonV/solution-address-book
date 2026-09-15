import { digits } from './utils';

export function isValidCpf(value: string) {
  const cpf = digits(value);
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;
  const calculate = (length: number, weight: number) => {
    const sum = cpf
      .slice(0, length)
      .split('')
      .reduce((total, digit, index) => total + Number(digit) * (weight - index), 0);
    const result = 11 - (sum % 11);
    return result >= 10 ? 0 : result;
  };
  return calculate(9, 10) === Number(cpf[9]) && calculate(10, 11) === Number(cpf[10]);
}
