import { describe, expect, it } from 'vitest'
import { isValidCpf } from './cpf'

describe('isValidCpf', () => {
  it.each(['529.982.247-25', '11144477735', '123.456.789-09'])('accepts %s', (cpf) => {
    expect(isValidCpf(cpf)).toBe(true)
  })

  it.each(['111.111.111-11', '529.982.247-24', '123'])('rejects %s', (cpf) => {
    expect(isValidCpf(cpf)).toBe(false)
  })
})

