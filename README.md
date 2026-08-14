# Dinix API

API REST de controle financeiro pessoal, pronta para ser consumida por um aplicativo Flutter.

**Guia para o frontend (rotas, bodies, paginação e exemplos):** [docs/guia-frontend.md](docs/guia-frontend.md)

```text
Flutter App
    ↓
REST API (Spring Boot)
    ↓
MySQL
```

## Stack

- Java 17
- Spring Boot 4.1
- Spring Web, Data JPA, Security, Validation
- MySQL + Flyway
- JWT (stateless) + BCrypt
- OpenAPI / Swagger
- Maven

O pacote base existente (`br.net.convertix.dinix`) foi preservado.

## Arquitetura

Controllers recebem a requisição, validam o payload e chamam services. Regras de negócio ficam nos services. Repositories acessam o banco. Entidades JPA nunca são expostas: a API usa DTOs.

```text
src/main/java/br/net/convertix/dinix/
├── config/
├── controller/
├── dto/request|response
├── entity/
├── enums/
├── exception/
├── mapper/
├── repository/
├── security/
├── service/
└── util/
```

O usuário autenticado sempre vem do JWT. A API nunca confia em `userId` enviado pelo cliente.

## Modelo financeiro

A entidade `FinancialTransaction` é o **razão contábil**. Cálculos mensais (dashboard, relatórios, orçamento) leem apenas esse ledger, com as flags:

| Tipo | Conta no mês? | Altera saldo da conta? |
|---|---|---|
| Receita (`INCOME`) | sim | sim (entra) |
| Despesa à vista | sim | sim (sai) |
| Despesa no cartão / parcela | sim, no mês do vencimento | não, até pagar a fatura |
| Transferência | **não** | sim (sai na origem, entra no destino) |
| Investimento (aporte) | sim, como investimento | sim (sai da conta) |

Decisões importantes:

1. **Compra parcelada é uma única compra.** As parcelas distribuem o valor no tempo. Agosto registra só a parcela de agosto, nunca o total em todos os meses.
2. **Transferência não é despesa.** Gera dois lançamentos do tipo `TRANSFER` com `countsInMonthlyResult = false`.
3. **Saldo da conta** começa em `initialBalance` e só muda via ledger. O usuário não edita `currentBalance` direto.
4. **Limite do cartão utilizado** = soma das parcelas `PENDING` e `OVERDUE`.
5. **Contas recorrentes e assinaturas** são compromissos. Entram em calendário, previsão e alertas. O realizado do mês usa só transações postadas.
6. **Soft delete** (`active = false`) nas entidades financeiras relevantes.
7. **Valores** usam `BigDecimal`. Datas de competência usam `LocalDate`; auditoria usa `LocalDateTime`.
8. A tabela `idempotency_keys` está pronta para a chave `Idempotency-Key` em operações financeiras.

## Como executar localmente

O banco é **MySQL local**, sem Docker.

### 1. MySQL

Crie o schema `dinix` (a API também tenta criar sozinha no profile `dev`):

```sql
CREATE DATABASE IF NOT EXISTS dinix
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Padrão de conexão:

- host: `localhost`
- porta: `3306`
- banco: `dinix`
- usuário: `root`
- senha: vazia (altere se o seu MySQL tiver senha)

### 2. Variáveis de ambiente

Copie `.env.example` e ajuste:

| Variável | Descrição |
|---|---|
| `DB_HOST` | Host do MySQL (`localhost`) |
| `DB_PORT` | Porta (`3306`) |
| `DB_NAME` | Nome do banco (`dinix`) |
| `DB_USERNAME` | Usuário (`root`) |
| `DB_PASSWORD` | Senha do MySQL |
| `JWT_SECRET` | Segredo HS256 com pelo menos 32 caracteres |
| `SPRING_PROFILES_ACTIVE` | `dev` ou `prod` |
| `APP_TIMEZONE` | Padrão `America/Sao_Paulo` |

No Windows, você pode exportar no PowerShell antes de subir a API:

```powershell
$env:DB_PASSWORD="sua_senha"
```

### 3. Subir a API

```bash
mvnw.cmd spring-boot:run
```

O Flyway aplica as migrations automaticamente na subida.

O Swagger abre sozinho no navegador ao iniciar no profile `dev`.

Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Use **Authorize** no Swagger com o JWT retornado no login.

## Testes

```bash
./mvnw test
```

Os testes usam o profile `test` com H2 em memória (`ddl-auto=create-drop`, Flyway desligado).

Prioridade coberta:

- autenticação
- transferência não vira despesa
- parcela não lança o valor cheio em todos os meses
- saldo da conta em despesa à vista vs cartão

## Principais endpoints

Prefixo: `/api/v1`

### Autenticação
- `POST /api/v1/autenticacao/registrar`
- `POST /api/v1/autenticacao/entrar`
- `GET /api/v1/usuarios/eu`

### Cadastros
- `/api/v1/contas` `/api/v1/cartoes-de-credito` `/api/v1/categorias` `/api/v1/locais` `/api/v1/produtos` `/api/v1/etiquetas`

### Movimentações
- `/api/v1/compras` `/api/v1/compras/parcelas/{id}/pagar`
- `/api/v1/receitas` `/api/v1/transferencias`
- `/api/v1/despesas-recorrentes` `/api/v1/assinaturas` `/api/v1/assinaturas/resumo`
- `/api/v1/investimentos` `/api/v1/investimentos/{id}/transacoes`
- `GET /api/v1/transacoes/busca`

### Visão
- `GET /api/v1/painel?mes=8&ano=2026`
- `GET /api/v1/resumo/mensal?mes=8&ano=2026`
- `GET /api/v1/relatorios/mensal|anual|categorias|receitas|despesas|investimentos|patrimonio`
- `GET /api/v1/estatisticas/locais` `GET /api/v1/estatisticas/produtos`
- `GET /api/v1/patrimonio` `GET /api/v1/patrimonio/historico`
- `GET /api/v1/calendario` `GET /api/v1/previsao`
- `/api/v1/orcamentos` `/api/v1/metas` `/api/v1/alertas` `/api/v1/anexos`

Autenticação: header `autorizacao: Bearer {token}`.

Campos de body, query e response em **snake_case português** (`nome`, `senha`, `id_conta`, `valor_total`, ...).

Paginação das listagens (`num_pag` começa em 1, `itens_pag` padrão 20, máximo 100):

```http
GET /api/v1/contas?num_pag=1&itens_pag=20
```

```json
{
  "itens": [],
  "num_pag": 1,
  "max_pag": 3,
  "max_itens": 42,
  "itens_pag": 20
}
```

## Relacionamento das entidades

```text
User
 ├── FinancialAccount
 ├── CreditCard ──► FinancialAccount
 ├── Category (árvore via parentCategory)
 ├── PurchaseLocation
 ├── Product ──► Category
 ├── Purchase
 │    ├── PurchaseItem ──► Product
 │    ├── Installment
 │    └── Tag (N:N)
 ├── Income ──► Account, Category
 ├── Transfer ──► Account origem / destino
 ├── RecurringExpense
 ├── Subscription
 ├── Investment
 │    └── InvestmentTransaction
 ├── Budget ──► Category
 ├── FinancialGoal
 ├── FinancialAlert
 └── FinancialTransaction  (razão)
      ├── Account / CreditCard / Category
      ├── Purchase / Installment / Income
      ├── Transfer / InvestmentTransaction
      └── Attachment
```

## Profiles

- `application.yml` — base
- `application-dev.yml` — desenvolvimento local
- `application-prod.yml` — produção (credenciais só por env)
- `application-test.yml` — testes
