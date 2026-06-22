# Testes de Carga com k6 🚀

Este diretório contém o script de teste de carga (`k6-performance.js`) para avaliar a capacidade da API sob concorrência e mapear potenciais gargalos no sistema.

## 🛠️ Como Executar

### 1. Pré-requisitos
Instale o **k6** de acordo com o seu sistema operacional:
- **Windows (Chocolatey):** `choco install k6`
- **macOS (Homebrew):** `brew install k6`
- **Linux (Deb/Ubuntu):**
  ```bash
  sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5D5E67500AC7577
  echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
  sudo apt-get update
  sudo apt-get install k6
  ```

### 2. Execução Local
Para rodar o teste de carga contra o seu ambiente local (com a aplicação iniciada em `http://localhost:8080`):
```bash
k6 run k6-performance.js
```

Se desejar alterar a URL base do endpoint da API para apontar para um IP de staging/produção obtido via Terraform:
```bash
k6 run -e API_URL=http://<IP_PUBLICO>:8080/api k6-performance.js
```

---

## 📈 Interpretação de Gargalos

### 1. Camada de Aplicação (Java / Tomcat)
- **Pool de Threads (Tomcat):** Se o número de conexões ativas do Tomcat atingir o limite (`server.tomcat.threads.max`), novas requisições sofrerão enfileiramento, elevando drasticamente a métrica `http_req_duration`.
- **Garbage Collection (JVM):** Picos abruptos de latência durante o teste podem indicar pausas longas de "Stop-the-World" causadas por GC excessivo na JVM. Acompanhe a memória Heap no Prometheus.

### 2. Camada de Infraestrutura e Rede
- **Largura de Banda e Latência:** Um throughput (`http_reqs`) limitado, mesmo com CPU/Memória baixos, pode apontar para problemas de rede (egress/ingress limits) na VPS/Droplet ou má configuração dos Security Groups/Firewalls.
- **Mensageria (RabbitMQ):** Se o worker consumidor for lento para processar eventos do Outbox, a fila do RabbitMQ crescerá indefinidamente. Acompanhe o tamanho das filas com `rabbitmqctl list_queues`.

### 3. Camada de Banco de Dados (MySQL)
- **Pool de Conexões (HikariCP):** Latências altas no banco geram timeout de conexão. Monitore as métricas do pool do HikariCP expostas no endpoint `/actuator/prometheus`.
- **Skip Locked / Lock contention:** A concorrência elevada em atualizações de status de viagens pode causar Lock Contention no MySQL. O uso estratégico de queries sem Lock excessivo (`SKIP LOCKED`) otimiza a vazão.

### 4. Camada de Hardware (CPU & Memory)
- **Estrangulamento de CPU:** Se a CPU da VPS chegar a 100%, o tempo de processamento de criptografia (SSL/TLS), serialização JSON e processamento interno sobe.
- **Memory Leaks:** Acompanhe se o consumo de memória RAM do container da aplicação cresce de forma contínua e linear sem retornar ao patamar base após o término do teste de carga.
