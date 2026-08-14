# Guia da API Dinix para o frontend

Documento para o app Flutter (ou qualquer cliente HTTP). Base: `http://localhost:8080`.

Swagger interativo: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 1. Regras gerais

| Item | Como usar |
|---|---|
| Prefixo | Todas as rotas de negócio começam com `/api/v1` |
| Formato | JSON (`Content-Type: application/json`) |
| Campos | **snake_case em português** no header, query, body e response |
| Datas | `YYYY-MM-DD` (ex.: `2026-08-13`) |
| Data/hora | ISO-8601 (`2026-08-13T22:30:00`) — timezone `America/Sao_Paulo` |
| IDs | UUID em string (`"3fa85f64-5717-4562-b3fc-2c963f66afa6"`) |
| Valores | Decimal em string ou número (`1500.50`) — prefira enviar com 2 casas |
| Autenticação | Header `autorizacao: Bearer {token}` em **todas** as rotas, menos cadastro e login |
| Usuário | Nunca envie `id_usuario`. A API pega do JWT |

### Header em toda request autenticada

```http
autorizacao: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

Guarde o `token` do login no storage seguro. Se a API responder **401**, limpe o token e mande o usuário para a tela de entrar.

### Exemplo Dio (Flutter)

```dart
final dio = Dio(BaseOptions(baseUrl: 'http://SEU_HOST:8080'));

dio.interceptors.add(InterceptorsWrapper(
  onRequest: (options, handler) {
    final token = authStorage.token;
    if (token != null) {
      options.headers['autorizacao'] = 'Bearer $token';
    }
    return handler.next(options);
  },
));
```

---

## 2. Paginação

Rotas que **listam muitos itens** devolvem este envelope (não um array solto):

```json
{
  "itens": [ ],
  "num_pag": 1,
  "max_pag": 3,
  "max_itens": 42,
  "itens_pag": 20
}
```

| Campo | Significado |
|---|---|
| `itens` | Lista da página atual |
| `num_pag` | Página atual (**começa em 1**) |
| `max_pag` | Total de páginas |
| `max_itens` | Total de registros do usuário (nessa consulta) |
| `itens_pag` | Quantidade de itens **nesta** página |

Query:

```http
GET /api/v1/contas?num_pag=1&itens_pag=20
```

- `num_pag` padrão: `1`
- `itens_pag` padrão: `20` (máximo `100`)
- `max_pag == 0` e `max_itens == 0` = lista vazia

Rotas paginadas: contas, cartões, categorias, locais, produtos, compras, receitas, transferências, despesas recorrentes, assinaturas, investimentos, transações do investimento, busca de transações, etiquetas, anexos, orçamentos, metas, alertas, histórico de patrimônio.

---

## 3. Erros

```json
{
  "data_hora": "2026-08-13T22:30:00",
  "status": 400,
  "erro": "erro_validacao",
  "mensagem": "Dados inválidos",
  "caminho": "/api/v1/contas",
  "erros_campos": {
    "nome": "não deve estar em branco"
  }
}
```

| HTTP | `erro` | O que o front faz |
|---|---|---|
| 400 | `erro_validacao` | Mostrar `erros_campos` nos inputs |
| 401 | `nao_autorizado` | Ir para login |
| 403 | `proibido` | Aviso de permissão |
| 404 | `nao_encontrado` | Item não existe / não é do usuário |
| 409 | `conflito` | Ex.: e-mail já cadastrado |
| 422 | `erro_negocio` | Mostrar `mensagem` (regra financeira) |
| 500 | `erro_interno` | Tente novamente |

DELETE bem-sucedido: **204** sem body.

---

## 4. Enums (enviar exatamente estes textos)

**Tipo de conta** `tipo_conta`: `conta_corrente` · `poupanca` · `investimento` · `dinheiro` · `outro`

**Forma de pagamento** `forma_pagamento`: `dinheiro` · `pix` · `cartao_debito` · `cartao_credito` · `transferencia` · `boleto` · `outro`

**Tipo de transação** `tipo`: `receita` · `despesa` · `investimento` · `transferencia`

**Tipo de categoria** `tipo`: `receita` · `despesa` · `ambos`

**Recorrência** `recorrencia`: `mensal` · `anual` · `semanal` · `personalizado`

**Status da parcela** `status`: `pendente` · `pago` · `atrasado` · `cancelado`

**Tipo de investimento** `tipo`: `acao` · `etf` · `fundo` · `renda_fixa` · `cripto` · `poupanca` · `outro`

**Movimento do investimento** `tipo`: `compra` · `venda` · `aporte` · `resgate` · `dividendo` · `juros`

**Status da meta** `status`: `ativa` · `concluida` · `cancelada`

**Tipo de alerta** `tipo`: `conta_vencendo` · `cartao_vencendo` · `assinatura_vencendo` · `orcamento_alerta` · `orcamento_estourado` · `despesa_incomum`

**Evento do calendário** `tipo`: `despesa_recorrente` · `assinatura` · `parcela` · `receita` · `vencimento_cartao` · `investimento`

---

## 5. Regras que o app precisa respeitar

1. **Transferência não é despesa.** Não some no gráfico de gastos. Só move saldo entre contas.
2. **Compra no cartão** não baixa o saldo da conta na hora. O **limite usado** do cartão é a soma das parcelas `pendente` + `atrasado`.
3. **Parcela:** o valor cheio da compra **não** entra em todos os meses. Cada mês conta só a parcela daquele mês.
4. **Pagar parcela** (`.../parcelas/{id}/pagar`) é o que baixa o saldo da conta vinculada ao cartão.
5. Compra à vista (`pix`, `dinheiro`, `cartao_debito`, etc.) precisa de `id_conta`. Compra no `cartao_credito` precisa de `id_cartao_credito`.
6. O saldo da conta só muda via razão interno da API. O front **mostra** `saldo_atual`; não envia saldo no update.

Fluxo sugerido no app:

1. Registrar / entrar → salvar token  
2. Criar pelo menos uma **conta**  
3. (Opcional) cartão, categorias (já vêm padrão no cadastro), locais, produtos  
4. Lançar compras / receitas / transferências  
5. Telas de painel, relatórios, orçamento, metas  

---

## 6. Autenticação e usuário

Rotas **sem** token: registrar e entrar.

### `POST /api/v1/autenticacao/registrar`

Cria o usuário, gera categorias padrão e já devolve o JWT. Use na tela de cadastro.

```json
{
  "nome": "Victor",
  "email": "victor@email.com",
  "senha": "senha1234"
}
```

`senha` mínimo 8 caracteres. Resposta **201**:

```json
{
  "token": "eyJ...",
  "tipo_token": "Bearer",
  "id_usuario": "uuid",
  "nome": "Victor",
  "email": "victor@email.com",
  "expira_em": "2026-08-14T22:30:00"
}
```

E-mail repetido → **409**.

### `POST /api/v1/autenticacao/entrar`

Tela de login.

```json
{
  "email": "victor@email.com",
  "senha": "senha1234"
}
```

Resposta **200**: mesmo formato do registrar. Senha errada → **401**.

### `GET /api/v1/usuarios/eu`

Perfil logado. Use no splash / menu.

```json
{
  "id": "uuid",
  "nome": "Victor",
  "email": "victor@email.com",
  "ativo": true,
  "criado_em": "...",
  "atualizado_em": "..."
}
```

---

## 7. Contas

Tela de carteiras (Nubank, carteira, etc.).

| Método | Rota | Uso |
|---|---|---|
| GET | `/api/v1/contas` | Listar (paginado) |
| GET | `/api/v1/contas/{id}` | Detalhe |
| POST | `/api/v1/contas` | Criar — **201** |
| PUT | `/api/v1/contas/{id}` | Editar nome/banco/tipo/cor (não envia saldo) |
| DELETE | `/api/v1/contas/{id}` | Soft delete — **204** |

**POST**

```json
{
  "nome": "Nubank",
  "nome_banco": "Nubank",
  "tipo_conta": "conta_corrente",
  "saldo_inicial": 1500.00,
  "cor": "#820AD1"
}
```

**Response** inclui `saldo_atual` (use este na UI), `saldo_inicial`, `ativo`, `criado_em`, `atualizado_em`.

**PUT** (sem saldo):

```json
{
  "nome": "Nubank PJ",
  "nome_banco": "Nubank",
  "tipo_conta": "conta_corrente",
  "cor": "#820AD1"
}
```

---

## 8. Cartões de crédito

| Método | Rota | Uso |
|---|---|---|
| GET | `/api/v1/cartoes-de-credito` | Listar |
| GET | `/api/v1/cartoes-de-credito/{id}` | Detalhe + limites |
| POST | `/api/v1/cartoes-de-credito` | Cadastrar |
| PUT | `/api/v1/cartoes-de-credito/{id}` | Editar |
| DELETE | `/api/v1/cartoes-de-credito/{id}` | Remover |

**POST / PUT**

```json
{
  "id_conta": "uuid-da-conta-de-pagamento",
  "nome": "Nubank Roxinho",
  "banco": "Nubank",
  "limite": 5000.00,
  "dia_fechamento": 10,
  "dia_vencimento": 17
}
```

`id_conta` é a conta de onde sairá o pagamento da fatura/parcela. `dia_*` de 1 a 31.

**Response** extra: `limite_usado`, `limite_disponivel`.

Na UI: barra de limite = `limite_usado / limite`.

---

## 9. Categorias, locais, produtos, etiquetas

### Categorias — `/api/v1/categorias`

No registro o usuário já recebe categorias padrão (`padrao_sistema: true`). Dá para criar filhas com `id_categoria_pai`.

**POST / PUT**

```json
{
  "nome": "Mercado",
  "descricao": "Supermercado",
  "icone": "cart",
  "tipo": "despesa",
  "id_categoria_pai": null
}
```

`tipo`: `receita` · `despesa` · `ambos`.

### Locais — `/api/v1/locais`

Onde a pessoa comprou (Mercado Extra, posto, etc.).

```json
{
  "nome": "Extra Moema",
  "descricao": null,
  "endereco": "Av. Ibirapuera, 1000",
  "cidade": "São Paulo",
  "estado": "SP",
  "latitude": -23.60,
  "longitude": -46.66
}
```

### Produtos — `/api/v1/produtos`

Itens reutilizáveis nas compras (leite, gasolina). Response traz `preco_medio`.

```json
{
  "nome": "Leite integral",
  "descricao": null,
  "marca": "Italac",
  "id_categoria": "uuid"
}
```

### Etiquetas — `/api/v1/etiquetas`

Tags livres para filtrar compras. O `#` é prefixado na API se faltar.

```json
{ "nome": "viagem" }
```

GET paginado. POST cria. Não há PUT/DELETE nesta versão.

---

## 10. Compras (despesas)

Tela principal de lançar gasto.

| Método | Rota | Uso |
|---|---|---|
| GET | `/api/v1/compras` | Histórico |
| GET | `/api/v1/compras/{id}` | Detalhe com itens, parcelas e etiquetas |
| POST | `/api/v1/compras` | Lançar compra |
| PUT | `/api/v1/compras/{id}` | Editar descrição/data/categoria/local/notas/tags (**não** muda valor nem parcelas) |
| DELETE | `/api/v1/compras/{id}` | Estornar / remover |
| POST | `/api/v1/compras/parcelas/{id}/pagar` | Dar baixa em **uma** parcela |

**POST à vista (Pix)**

```json
{
  "descricao": "Mercado Extra",
  "data_compra": "2026-08-13",
  "valor_total": 320.40,
  "id_categoria": "uuid",
  "id_local": "uuid",
  "forma_pagamento": "pix",
  "id_conta": "uuid",
  "id_cartao_credito": null,
  "observacoes": null,
  "qtd_parcelas": 1,
  "data_primeira_parcela": null,
  "ids_etiquetas": [],
  "itens": [
    {
      "id_produto": "uuid",
      "quantidade": 2,
      "preco_unitario": 6.50
    }
  ]
}
```

`itens` é opcional. Se enviar, `quantidade` e `preco_unitario` são obrigatórios.

**POST no cartão (3x)**

```json
{
  "descricao": "Notebook",
  "data_compra": "2026-08-13",
  "valor_total": 3000.00,
  "id_categoria": "uuid",
  "id_local": null,
  "forma_pagamento": "cartao_credito",
  "id_conta": null,
  "id_cartao_credito": "uuid",
  "observacoes": "Dell",
  "qtd_parcelas": 3,
  "data_primeira_parcela": "2026-09-17",
  "ids_etiquetas": [],
  "itens": []
}
```

A API cria 3 parcelas. No painel de agosto **não** entra R$ 3.000; entra só a parcela do mês.

**Response da compra** (campos principais): `id`, `descricao`, `data_compra`, `valor_total`, `id_categoria`, `id_local`, `forma_pagamento`, `id_conta`, `id_cartao_credito`, `qtd_parcelas`, `valor_parcela`, `itens`, `parcelas`, `etiquetas`.

Cada parcela: `id`, `numero_parcela`, `total_parcelas`, `valor`, `data_vencimento`, `status`, `pago_em`.

**Pagar parcela:** `POST /api/v1/compras/parcelas/{id_da_parcela}/pagar` — sem body. Devolve a parcela com `status: "pago"`.

**PUT** (não manda valor):

```json
{
  "descricao": "Mercado Extra — Moema",
  "data_compra": "2026-08-13",
  "id_categoria": "uuid",
  "id_local": "uuid",
  "forma_pagamento": "pix",
  "observacoes": null,
  "ids_etiquetas": []
}
```

---

## 11. Receitas

Salário, freelance, etc. Entra no mês e **aumenta** o saldo da conta.

| Método | Rota |
|---|---|
| GET | `/api/v1/receitas` |
| GET | `/api/v1/receitas/{id}` |
| POST | `/api/v1/receitas` |
| PUT | `/api/v1/receitas/{id}` |
| DELETE | `/api/v1/receitas/{id}` |

```json
{
  "descricao": "Salário",
  "valor": 8500.00,
  "id_categoria": "uuid",
  "id_conta": "uuid",
  "data_recebimento": "2026-08-05",
  "recorrente": true,
  "observacoes": null
}
```

PUT usa o mesmo body. `recorrente: true` faz a receita aparecer no calendário/previsão nos meses seguintes.

---

## 12. Transferências

Mover dinheiro entre contas. **Não** conta como despesa nem receita.

| Método | Rota |
|---|---|
| GET | `/api/v1/transferencias` |
| POST | `/api/v1/transferencias` |
| DELETE | `/api/v1/transferencias/{id}` |

Não há GET por id nem PUT. DELETE desfaz o movimento.

```json
{
  "id_conta_origem": "uuid",
  "id_conta_destino": "uuid",
  "valor": 200.00,
  "data_transferencia": "2026-08-13",
  "descricao": "Para poupança"
}
```

Contas iguais ou saldo insuficiente → **422**.

---

## 13. Despesas recorrentes

Contas fixas (aluguel, energia) que não são assinatura de streaming.

CRUD em `/api/v1/despesas-recorrentes`. POST e PUT:

```json
{
  "nome": "Aluguel",
  "descricao": null,
  "valor": 2200.00,
  "id_categoria": "uuid",
  "id_conta": "uuid",
  "dia_vencimento": 10,
  "data_inicio": "2026-01-01",
  "data_fim": null,
  "recorrencia": "mensal"
}
```

Aparecem no calendário e na previsão. Não geram lançamento automático de compra; o front pode usar o calendário para lembrar o usuário de pagar.

---

## 14. Assinaturas

Netflix, academia, etc.

| Método | Rota | Uso |
|---|---|---|
| GET | `/api/v1/assinaturas` | Lista |
| GET | `/api/v1/assinaturas/resumo` | Totais mensal/anual + próximos pagamentos |
| GET | `/api/v1/assinaturas/{id}` | Detalhe |
| POST | `/api/v1/assinaturas` | Criar |
| PUT | `/api/v1/assinaturas/{id}` | Editar (mesmo body do POST) |
| DELETE | `/api/v1/assinaturas/{id}` | Cancela (não apaga o histórico) |

```json
{
  "nome": "Netflix",
  "descricao": "Família",
  "valor": 55.90,
  "id_categoria": "uuid",
  "forma_pagamento": "cartao_credito",
  "id_conta": null,
  "id_cartao_credito": "uuid",
  "dia_cobranca": 15,
  "data_inicio": "2026-01-15",
  "recorrencia": "mensal"
}
```

**Resumo:**

```json
{
  "total_mensal": 120.80,
  "total_anual": 1449.60,
  "proximos_pagamentos": [
    {
      "id_assinatura": "uuid",
      "nome": "Netflix",
      "valor": 55.90,
      "data": "2026-09-15"
    }
  ]
}
```

Response do item traz `data_proxima_cobranca` e `cancelado_em`.

---

## 15. Investimentos

| Método | Rota | Uso |
|---|---|---|
| GET | `/api/v1/investimentos` | Carteira |
| GET | `/api/v1/investimentos/{id}` | Detalhe com `valor_atual`, `lucro_prejuizo`, `percentual_rentabilidade` |
| POST | `/api/v1/investimentos` | Cadastrar ativo |
| DELETE | `/api/v1/investimentos/{id}` | Remover |
| GET | `/api/v1/investimentos/{id}/transacoes` | Extrato do ativo (paginado) |
| POST | `/api/v1/investimentos/{id}/transacoes` | Aporte / compra / venda / etc. |

**Criar ativo**

```json
{
  "nome": "Tesouro Selic",
  "instituicao": "NuInvest",
  "tipo": "renda_fixa",
  "ticker": "SELIC"
}
```

**Lançar movimento**

```json
{
  "tipo": "aporte",
  "valor": 1000.00,
  "quantidade": null,
  "preco": null,
  "id_conta": "uuid",
  "data_transacao": "2026-08-13",
  "observacoes": null
}
```

`aporte` / `compra` saem da conta. `resgate` / `venda` voltam para a conta. O patrimônio soma `valor_atual` dos investimentos.

---

## 16. Busca no razão e anexos

O razão (`FinancialTransaction`) é a fonte do extrato unificado.

### `GET /api/v1/transacoes/busca`

Tela de extrato / pesquisa.

| Query | Uso |
|---|---|
| `busca` | Texto na descrição |
| `tipo` | `receita` · `despesa` · `investimento` · `transferencia` |
| `id_categoria` | Filtrar categoria |
| `id_conta` | Filtrar conta |
| `id_cartao_credito` | Filtrar cartão |
| `data_inicio` / `data_fim` | Período (`YYYY-MM-DD`) |
| `valor_min` / `valor_max` | Faixa de valor |
| `num_pag` / `itens_pag` | Paginação |

Response de cada item: `id`, `tipo`, `valor`, `data_transacao`, `descricao`, `id_conta`, `id_cartao_credito`, `id_categoria`, `id_compra`, `id_parcela`, `id_receita`, `id_transferencia`, `conta_no_resultado_mensal`, `altera_saldo_conta`.

Use `id_compra` / `id_receita` / `id_transferencia` para abrir o detalhe na tela certa.

### Anexos (comprovante)

A API **não faz upload de arquivo**. O app sobe a imagem (Firebase/S3) e grava a URL.

**POST `/api/v1/anexos`**

```json
{
  "id_transacao": "uuid-da-transacao-do-razao",
  "nome_arquivo": "nota.jpg",
  "url_arquivo": "https://...",
  "tipo_conteudo": "image/jpeg"
}
```

**GET `/api/v1/anexos?id_transacao={uuid}`** — lista paginada dos comprovantes daquela transação.

---

## 17. Painel, resumo, calendário e previsão

Query comum: `?mes=8&ano=2026`. Se omitir, a API usa o mês/ano atuais (previsão usa o **próximo** mês).

### `GET /api/v1/painel`

Home do app.

```json
{
  "mes": 8,
  "ano": 2026,
  "receitas": { "total": 8500, "quantidade": 1, "total_anterior": 8500, "percentual_variacao": 0 },
  "despesas": { "total": 2100, "quantidade": 12, "total_anterior": 1800, "percentual_variacao": 16.66 },
  "investimentos": 1000,
  "disponivel": 5400,
  "despesas_por_categoria": [{ "categoria": "Mercado", "valor": 800, "percentual": 38.09 }],
  "proximos_pagamentos": [{ "tipo": "parcela", "descricao": "Notebook 1/3", "valor": 1000, "data_vencimento": "2026-08-17" }],
  "cartoes_credito": [ ],
  "assinaturas": [ ]
}
```

`tipo` em `proximos_pagamentos`: `parcela` · `assinatura` · `despesa_recorrente`.

### `GET /api/v1/resumo/mensal`

Tela “resumo do mês”: totais, % gasto vs receita, comparativo, top locais e produtos.

Campos: `total_receitas`, `total_despesas`, `total_investimentos`, `total_transferencias`, `saldo_disponivel`, `percentual_despesas`, `percentual_investimentos`, `comparativo_despesas`, `despesas_por_categoria`, `principais_locais`, `principais_produtos`.

### `GET /api/v1/calendario?mes=&ano=`

Agenda do mês. Monte um calendar widget com `dias`.

```json
{
  "dias": [
    {
      "data": "2026-08-10",
      "eventos": [
        { "tipo": "despesa_recorrente", "descricao": "Aluguel", "valor": 2200 }
      ]
    }
  ]
}
```

### `GET /api/v1/previsao?mes=&ano=`

“O que vem no próximo mês” se não passar query.

```json
{
  "mes": 9,
  "ano": 2026,
  "receitas_previstas": 8500,
  "despesas_comprometidas": 3255.90,
  "investimentos_previstos": 0,
  "disponivel_previsto": 5244.10
}
```

---

## 18. Relatórios, estatísticas e patrimônio

Relatórios pedem `mes` e `ano` (anual só `ano`).

| Rota | Tela |
|---|---|
| `GET /api/v1/relatorios/mensal?mes=8&ano=2026` | Relatório do mês |
| `GET /api/v1/relatorios/anual?ano=2026` | Relatório do ano (`mes` vem 0) |
| `GET /api/v1/relatorios/categorias?mes=&ano=` | Pizza de categorias |
| `GET /api/v1/relatorios/receitas?mes=&ano=` | Só receitas |
| `GET /api/v1/relatorios/despesas?mes=&ano=` | Só despesas |
| `GET /api/v1/relatorios/investimentos?mes=&ano=` | Só investimentos |
| `GET /api/v1/relatorios/patrimonio` | Igual a `/patrimonio` |
| `GET /api/v1/patrimonio` | Patrimônio líquido agora |
| `GET /api/v1/patrimonio/historico` | Evolução (paginado) |
| `GET /api/v1/estatisticas/locais?mes=&ano=` | Onde mais gasta |
| `GET /api/v1/estatisticas/produtos?mes=&ano=` | O que mais compra |

**Relatório**

```json
{
  "titulo": "Relatório mensal",
  "mes": 8,
  "ano": 2026,
  "total": 2100.00,
  "quantidade": 12,
  "detalhamento": [{ "nome": "Mercado", "valor": 800 }]
}
```

**Patrimônio**

```json
{
  "saldo_contas": 4300.00,
  "valor_investimentos": 12000.00,
  "dividas": 1500.00,
  "patrimonio": 14800.00
}
```

`dividas` = limite usado dos cartões (parcelas em aberto). Histórico inclui `mes` e `ano` em cada ponto.

**Produto (estatística):** `id_produto`, `nome`, `quantidade`, `total_gasto`, `preco_medio`, `data_ultima_compra`.

---

## 19. Orçamentos, metas e alertas

### Orçamentos — `/api/v1/orcamentos`

Limite por categoria no mês. GET aceita `mes`, `ano` + paginação.

**POST**

```json
{
  "id_categoria": "uuid",
  "valor_limite": 800.00,
  "mes": 8,
  "ano": 2026
}
```

Já existir orçamento da mesma categoria no mês → **409**.

**PUT** só o limite:

```json
{ "valor_limite": 1000.00 }
```

**Response:** `gasto`, `restante`, `percentual_usado`, `nome_categoria`. Barra da UI = `percentual_usado`.

### Metas — `/api/v1/metas`

Viagem, reserva de emergência, etc.

**POST**

```json
{
  "nome": "Viagem Chile",
  "descricao": null,
  "valor_alvo": 8000.00,
  "valor_atual": 1200.00,
  "data_alvo": "2026-12-01"
}
```

**PUT** (pode mudar status):

```json
{
  "nome": "Viagem Chile",
  "descricao": null,
  "valor_alvo": 8000.00,
  "valor_atual": 2500.00,
  "data_alvo": "2026-12-01",
  "status": "ativa"
}
```

Response extra: `valor_restante`, `percentual_concluido`, `mensal_necessario`, `previsao_conclusao`.

### Alertas — `/api/v1/alertas`

GET paginado (contas vencendo, orçamento estourado, etc.).

**PATCH `/api/v1/alertas/{id}/lido`** — marca como lido. Sem body.

Campos: `tipo`, `titulo`, `mensagem`, `valor`, `data_vencimento`, `lido`.

Badge do sino = itens com `"lido": false`.

---

## 20. Tabela rápida (todas as rotas)

| Método | Rota | Auth | Pagina |
|---|---|---|---|
| POST | `/api/v1/autenticacao/registrar` | não | — |
| POST | `/api/v1/autenticacao/entrar` | não | — |
| GET | `/api/v1/usuarios/eu` | sim | — |
| GET/POST | `/api/v1/contas` | sim | GET |
| GET/PUT/DELETE | `/api/v1/contas/{id}` | sim | — |
| GET/POST | `/api/v1/cartoes-de-credito` | sim | GET |
| GET/PUT/DELETE | `/api/v1/cartoes-de-credito/{id}` | sim | — |
| GET/POST | `/api/v1/categorias` | sim | GET |
| GET/PUT/DELETE | `/api/v1/categorias/{id}` | sim | — |
| GET/POST | `/api/v1/locais` | sim | GET |
| GET/PUT/DELETE | `/api/v1/locais/{id}` | sim | — |
| GET/POST | `/api/v1/produtos` | sim | GET |
| GET/PUT/DELETE | `/api/v1/produtos/{id}` | sim | — |
| GET/POST | `/api/v1/compras` | sim | GET |
| GET/PUT/DELETE | `/api/v1/compras/{id}` | sim | — |
| POST | `/api/v1/compras/parcelas/{id}/pagar` | sim | — |
| GET/POST | `/api/v1/receitas` | sim | GET |
| GET/PUT/DELETE | `/api/v1/receitas/{id}` | sim | — |
| GET/POST | `/api/v1/transferencias` | sim | GET |
| DELETE | `/api/v1/transferencias/{id}` | sim | — |
| GET/POST | `/api/v1/despesas-recorrentes` | sim | GET |
| GET/PUT/DELETE | `/api/v1/despesas-recorrentes/{id}` | sim | — |
| GET/POST | `/api/v1/assinaturas` | sim | GET |
| GET | `/api/v1/assinaturas/resumo` | sim | — |
| GET/PUT/DELETE | `/api/v1/assinaturas/{id}` | sim | — |
| GET/POST | `/api/v1/investimentos` | sim | GET |
| GET/DELETE | `/api/v1/investimentos/{id}` | sim | — |
| GET/POST | `/api/v1/investimentos/{id}/transacoes` | sim | GET |
| GET | `/api/v1/transacoes/busca` | sim | sim |
| GET/POST | `/api/v1/etiquetas` | sim | GET |
| GET/POST | `/api/v1/anexos` | sim | GET |
| GET | `/api/v1/painel` | sim | — |
| GET | `/api/v1/resumo/mensal` | sim | — |
| GET | `/api/v1/relatorios/mensal` | sim | — |
| GET | `/api/v1/relatorios/anual` | sim | — |
| GET | `/api/v1/relatorios/categorias` | sim | — |
| GET | `/api/v1/relatorios/receitas` | sim | — |
| GET | `/api/v1/relatorios/despesas` | sim | — |
| GET | `/api/v1/relatorios/investimentos` | sim | — |
| GET | `/api/v1/relatorios/patrimonio` | sim | — |
| GET | `/api/v1/patrimonio` | sim | — |
| GET | `/api/v1/patrimonio/historico` | sim | sim |
| GET | `/api/v1/estatisticas/locais` | sim | — |
| GET | `/api/v1/estatisticas/produtos` | sim | — |
| GET | `/api/v1/calendario` | sim | — |
| GET | `/api/v1/previsao` | sim | — |
| GET/POST | `/api/v1/orcamentos` | sim | GET |
| PUT/DELETE | `/api/v1/orcamentos/{id}` | sim | — |
| GET/POST | `/api/v1/metas` | sim | GET |
| GET/PUT/DELETE | `/api/v1/metas/{id}` | sim | — |
| GET | `/api/v1/alertas` | sim | sim |
| PATCH | `/api/v1/alertas/{id}/lido` | sim | — |

No Swagger, em **Authorize**, cole `Bearer {token}` no campo `autorizacao`.
