# Post-Mortem Arquitetural & Tradeoffs de Engenharia 🚚

Este documento analisa as escolhas técnicas feitas na evolução do sistema **ShipFast**, pesando as vantagens, desvantagens e os caminhos recomendados para escalabilidade em produção.

---

## 📐 1. Decisões de Arquitetura & Tradeoffs

### Monolito Modular vs. Microsserviços
Optamos por manter a aplicação como um **Monolito Modular** utilizando Spring Boot, em vez de dividi-la prematuramente em microsserviços de API e Workers de mensageria.
* **Prós:** 
  - Simplicidade extrema de deploy e menor custo de infraestrutura (IaC simplificada).
  - Facilidade de rodar testes de integração ponta a ponta rápidos (`mvn clean test`).
  - Sem latência de rede para chamadas internas de serviço.
* **Contras:** 
  - O serviço de Polling do Outbox (`OutboxPollerService`) roda na mesma JVM que a API HTTP. Sob carga extrema de requisições web, eles competem diretamente por CPU, Memória e conexões no pool do banco de dados (HikariCP).

### Transactional Outbox com `SKIP LOCKED`
Para garantir que toda viagem criada ou finalizada envie um evento de despacho sem risco de inconsistência (caso o broker RabbitMQ caia), implementamos o padrão **Transactional Outbox**.
* **Prós:** 
  - **Consistência Eventual Garantida:** O evento é salvo na mesma transação e banco de dados que a alteração de status da viagem. Se o banco falhar, nada é persistido. Se o RabbitMQ falhar, o evento continua salvo aguardando reprocessamento.
  - **Concorrência Segura (`SKIP LOCKED`):** Permite escalar múltiplos workers lendo a tabela de outbox concorrentemente sem que um bloqueie o outro ou processe o mesmo evento duplicado.
* **Contras:**
  - **Write Overhead:** Cada ação de escrita na API gera uma query adicional de insert na tabela `outbox_event`.
  - **Banco de Dados como Gargalo:** Fazer polling constante (`@Scheduled`) no banco consome conexões e I/O de disco. Em produção real, o ideal seria utilizar ferramentas de CDC (Change Data Capture) como o **Debezium** para ler os logs binários do MySQL (Binlog) sem onerar a engine relacional.

### Idempotência no Consumidor (`MessageId`)
Configuramos o envio de um ID único de mensagem nos cabeçalhos da fila.
* **Prós:**
  - Proteção total contra duplicidade no processamento caso o RabbitMQ envie a mesma mensagem duas vezes (devido a timeouts de rede ou falha de ack).
* **Contras:**
  - Necessidade de guardar em memória (ou cache distribuído como Redis) a lista de IDs de mensagens já processadas para verificação.

---

## 📈 2. Análise de Carga & Comportamento do Hardware

Durante picos de acesso (conforme simulado no script do k6), as seguintes reações são esperadas nos componentes de hardware e software:

### Esgotamento do Pool de Conexões (HikariCP)
O Spring Boot vem por padrão configurado com apenas 10 conexões no pool de dados. Sob carga concorrente extrema de criação de viagens, as conexões ativas esgotam rapidamente, gerando filas de espera na aplicação (`ConnectionIsuseTimeoutException`) e elevando a latência percebida pelo cliente (`http_req_duration`).
* **Melhoria:** Ajustar `spring.datasource.hikari.maximum-pool-size` com base no limite suportado pelo banco gerenciado (ex: RDS/Cloud SQL) e usar réplicas de leitura para queries de visualização.

### Lock Contention no MySQL
Quando muitas requisições tentam atualizar simultaneamente o status de viagens atreladas aos mesmos veículos, ocorrem travas de linha (Row Locks) no banco de dados. Isso faz com que transações fiquem enfileiradas aguardando a liberação do lock.
* **Melhoria:** Criação de índices específicos nas tabelas (`idx_trip_status`, `idx_outbox_status_created`) e otimização dos escopos de transação do Spring (`@Transactional` o mais curto possível).

---

## 🛡️ 3. Recomendações para Produção (Roadmap)

1. **Change Data Capture (CDC):** Substituir o `@Scheduled` poller pelo Debezium conectado ao binlog do MySQL para publicar no RabbitMQ com latência de milissegundos e sem polling direto no banco.
2. **Cache de Idempotência:** Migrar a verificação de mensagens duplicadas do consumer para o **Redis**, permitindo escalar os consumidores horizontalmente sem perder o estado de controle de IDs.
3. **Escalonamento Horizontal (Kubernetes):** Subir a imagem docker do monolito modular em um cluster K8s e configurar HPA (Horizontal Pod Autoscaler) baseado no tamanho da fila de mensagens no RabbitMQ e no uso de CPU.
4. **Segurança de Borda:** Implementar WAF, CORS restritivo e autenticação baseada em JWT com escopos de permissões explícitas.
