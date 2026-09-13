import { expect, test, type Page } from '@playwright/test'

const adminCpf = process.env.E2E_ADMIN_CPF ?? '52998224725'
const adminPassword = process.env.E2E_ADMIN_PASSWORD ?? 'Admin@123'
const userPassword = 'E2e@12345'
const profilePng = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wl2nqAAAAAASUVORK5CYII=', 'base64')

function generateValidCpf(seed = Date.now()) {
  let base = String(seed).slice(-9).padStart(9, '1')
  if (/^(\d)\1+$/.test(base)) base = '123456789'

  const checkDigit = (value: string, initialWeight: number) => {
    const sum = [...value].reduce((total, digit, index) => total + Number(digit) * (initialWeight - index), 0)
    const result = 11 - (sum % 11)
    return result > 9 ? 0 : result
  }

  const firstDigit = checkDigit(base, 10)
  const secondDigit = checkDigit(`${base}${firstDigit}`, 11)
  return `${base}${firstDigit}${secondDigit}`
}

async function login(page: Page, cpf: string, password: string) {
  await page.goto('/login')
  await page.getByLabel('CPF').fill(cpf)
  await page.getByLabel('Senha').fill(password)
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page).not.toHaveURL(/\/login$/)
}

test('protege rota privada e informa credenciais inválidas', async ({ page }) => {
  await page.goto('/users')
  await expect(page).toHaveURL(/\/login$/)

  await page.getByLabel('CPF').fill(adminCpf)
  await page.getByLabel('Senha').fill('senha-incorreta')
  await page.getByRole('button', { name: 'Entrar' }).click()

  await expect(page.getByRole('alert')).toBeVisible()
  await expect(page).toHaveURL(/\/login$/)
})

test('executa jornada de usuário e endereço com autorização por perfil', async ({ page }) => {
  const runId = Date.now()
  const userName = `Usuário E2E ${runId}`
  const adminEditedName = `${userName} editado pelo admin`
  const selfEditedName = `${userName} editado pelo usuário`
  const userCpf = generateValidCpf(runId)
  const addressNumber = String(runId).slice(-5)

  await login(page, adminCpf, adminPassword)
  await expect(page.getByRole('heading', { name: 'Usuários', level: 1 })).toBeVisible()

  await page.getByRole('button', { name: 'Novo usuário' }).click()
  const createUserDialog = page.getByRole('dialog', { name: 'Novo usuário' })
  await createUserDialog.getByLabel('Nome completo').fill(userName)
  await createUserDialog.getByLabel('CPF').fill(userCpf)
  await createUserDialog.getByLabel('Data de nascimento').fill('1995-05-20')
  await createUserDialog.getByLabel('Senha inicial').fill(userPassword)
  await createUserDialog.getByLabel('Perfil').selectOption('USER')
  await createUserDialog.getByRole('button', { name: 'Criar usuário' }).click()
  await expect(createUserDialog).toBeHidden()

  const createdUserRow = page.getByRole('button', { name: new RegExp(userName) })
  await expect(createdUserRow).toBeVisible()
  await createdUserRow.click()
  await expect(page).toHaveURL(/\/users\/[0-9a-f-]{36}$/)
  const userUrl = page.url()
  await expect(page.getByRole('heading', { name: userName, level: 1 })).toBeVisible()

  await page.getByRole('button', { name: 'Editar dados' }).click()
  const adminEditDialog = page.getByRole('dialog', { name: 'Editar usuário' })
  await expect(adminEditDialog.getByLabel('Perfil')).toBeVisible()
  await adminEditDialog.getByLabel('Nome completo').fill(adminEditedName)
  await adminEditDialog.getByRole('button', { name: 'Salvar alterações' }).click()
  await expect(adminEditDialog).toBeHidden()
  await expect(page.getByRole('heading', { name: adminEditedName, level: 1 })).toBeVisible()

  await expect(page.getByRole('button', { name: 'Alterar foto' })).toHaveCount(0)

  await page.getByRole('button', { name: 'Novo endereço' }).click()
  const newAddressDialog = page.getByRole('dialog', { name: 'Novo endereço' })
  await newAddressDialog.getByLabel('CEP').fill('20040020')
  await newAddressDialog.getByLabel('CEP').press('Tab')
  await expect(newAddressDialog.locator('.postal-preview')).toContainText('Rio de Janeiro')
  await newAddressDialog.getByLabel('Número').fill(addressNumber)
  await newAddressDialog.getByLabel('Complemento').fill('Sala E2E')
  await newAddressDialog.getByRole('button', { name: 'Salvar endereço' }).click()
  await expect(newAddressDialog).toBeHidden()

  let addressCard = page.locator('article.address-card').filter({ hasText: addressNumber })
  await expect(addressCard).toContainText('Sala E2E')
  await addressCard.getByRole('button', { name: 'Ações do endereço' }).click()
  await page.getByRole('button', { name: 'Editar', exact: true }).click()
  const editAddressDialog = page.getByRole('dialog', { name: 'Editar endereço' })
  await editAddressDialog.getByLabel('Complemento').fill('Sala E2E atualizada')
  await editAddressDialog.getByRole('button', { name: 'Salvar endereço' }).click()
  await expect(editAddressDialog).toBeHidden()
  await expect(addressCard).toContainText('Sala E2E atualizada')

  await page.getByRole('button', { name: 'Sair' }).click()
  await login(page, userCpf, userPassword)
  await expect(page).toHaveURL(userUrl)
  await expect(page.getByRole('heading', { name: adminEditedName, level: 1 })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Novo usuário' })).toHaveCount(0)

  await page.getByRole('button', { name: 'Editar dados' }).click()
  const selfEditDialog = page.getByRole('dialog', { name: 'Editar usuário' })
  await expect(selfEditDialog.getByLabel('Perfil')).toHaveCount(0)
  await selfEditDialog.getByLabel('Nome completo').fill(selfEditedName)
  await selfEditDialog.getByRole('button', { name: 'Salvar alterações' }).click()
  await expect(selfEditDialog).toBeHidden()
  await expect(page.getByRole('heading', { name: selfEditedName, level: 1 })).toBeVisible()

  await page.getByRole('button', { name: 'Alterar foto' }).click()
  const selfPhotoDialog = page.getByRole('dialog', { name: 'Foto de perfil' })
  await selfPhotoDialog.getByLabel('Escolher foto').setInputFiles({
    name: 'perfil-usuario.png',
    mimeType: 'image/png',
    buffer: profilePng,
  })
  await selfPhotoDialog.getByRole('button', { name: 'Salvar foto' }).click()
  await expect(selfPhotoDialog).toBeHidden()
  await expect(page.getByRole('img', { name: `Foto de ${selfEditedName}` }).first()).toBeVisible()

  const roleEscalationStatus = await page.evaluate(async ({ currentUserUrl, name, cpf }) => {
    const userId = new URL(currentUserUrl).pathname.split('/').pop()
    const token = localStorage.getItem('solution.access-token')
    const response = await fetch(`/api/users/${userId}`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name, cpf, birthDate: '1995-05-20', role: 'ADMIN' }),
    })
    return response.status
  }, { currentUserUrl: userUrl, name: selfEditedName, cpf: userCpf })
  expect(roleEscalationStatus).toBe(403)

  await page.goto('/users')
  await expect(page).toHaveURL(userUrl)
  addressCard = page.locator('article.address-card').filter({ hasText: addressNumber })
  await addressCard.getByRole('button', { name: 'Ações do endereço' }).click()
  await page.getByRole('button', { name: 'Excluir', exact: true }).click()

  const deleteDialog = page.getByRole('alertdialog', { name: 'Excluir endereço?' })
  await expect(deleteDialog.getByRole('button', { name: 'Cancelar' })).toBeFocused()
  await deleteDialog.getByRole('button', { name: 'Cancelar' }).click()
  await expect(addressCard).toBeVisible()

  await addressCard.getByRole('button', { name: 'Ações do endereço' }).click()
  await page.getByRole('button', { name: 'Excluir', exact: true }).click()
  await page.getByRole('alertdialog', { name: 'Excluir endereço?' })
    .getByRole('button', { name: 'Excluir endereço' })
    .click()

  await expect(addressCard).toHaveCount(0)
  await expect(page.getByText('Nenhum endereço cadastrado')).toBeVisible()

  await page.getByRole('button', { name: 'Sair' }).click()
  await login(page, adminCpf, adminPassword)
  await page.getByLabel('Buscar por nome ou CPF').fill(selfEditedName)
  await page.getByRole('button', { name: new RegExp(selfEditedName) }).click()

  await page.getByRole('button', { name: 'Desativar conta' }).click()
  const deactivateDialog = page.getByRole('alertdialog', { name: 'Desativar conta?' })
  await expect(deactivateDialog.getByRole('button', { name: 'Cancelar' })).toBeFocused()
  await deactivateDialog.getByRole('button', { name: 'Desativar conta' }).click()
  await expect(deactivateDialog).toBeHidden()
  await expect(page.getByText('Conta desativada', { exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Editar dados' })).toHaveCount(0)

  await page.getByRole('button', { name: 'Sair' }).click()
  await page.getByLabel('CPF').fill(userCpf)
  await page.getByLabel('Senha').fill(userPassword)
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByRole('alert')).toContainText('Conta desativada')
  await expect(page).toHaveURL(/\/login$/)

  await login(page, adminCpf, adminPassword)
  await page.getByLabel('Status').selectOption('INACTIVE')
  await page.getByLabel('Buscar por nome ou CPF').fill(selfEditedName)
  await page.getByRole('button', { name: new RegExp(selfEditedName) }).click()
  await page.getByRole('button', { name: 'Reativar conta' }).click()
  const reactivateDialog = page.getByRole('alertdialog', { name: 'Reativar conta?' })
  await reactivateDialog.getByRole('button', { name: 'Reativar conta' }).click()
  await expect(reactivateDialog).toBeHidden()
  await expect(page.getByRole('button', { name: 'Editar dados' })).toBeVisible()

  await page.getByRole('button', { name: 'Sair' }).click()
  await login(page, userCpf, userPassword)
  await expect(page.getByRole('heading', { name: selfEditedName, level: 1 })).toBeVisible()
})
