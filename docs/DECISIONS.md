# Registro de decisões

## ADR-001 - Monólito modular

**Status:** aceito.

Domínio pequeno, regras transacionais fortes e uma equipe provável. Monólito modular simplifica deploy, observabilidade e consistência, preservando limites internos.

## ADR-002 - PostgreSQL + Flyway

**Status:** aceito.

PostgreSQL oferece índice único parcial para a regra de endereço principal. Flyway mantém schema versionado e auditável.

## ADR-003 - JWT stateless

**Status:** aceito.

SPA consome API REST e não requer estado de sessão no servidor. Token inclui somente identidade e papel; usuário ainda é recarregado do banco a cada requisição para refletir revogação/exclusão.

## ADR-004 - Consulta ViaCEP no frontend e backend

**Status:** aceito.

Frontend consulta para UX imediata. Backend consulta novamente e persiste resposta confiável, impedindo manipulação de cidade, estado, bairro ou logradouro pelo cliente.

## ADR-005 - Cache local de CEP

**Status:** aceito para versão atual.

Caffeine reduz latência e chamadas externas sem adicionar serviço. Em escala horizontal, migrar para Redis se taxa de repetição justificar consistência de cache entre réplicas.
