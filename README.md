# Desafio Técnico Backend: Sistema ShipFast 🚚

Bem-vindo ao desafio técnico da **ShipFast**! Este é um teste prático projetado para avaliar suas habilidades em desenvolvimento backend utilizando o ecossistema Spring Boot.

O objetivo deste exercício é implementar as camadas de negócio e controle para um sistema de despacho e logística de frotas. O projeto já possui a configuração inicial, as entidades de banco de dados (JPA) e uma suíte completa de testes de integração. **Sua missão é fazer todos os testes passarem.**

---

## 🛠️ O Cenário de Negócio

Você trabalhará com o gerenciamento de viagens de entrega (`DeliveryTrip`), que dependem de veículos (`Vehicle`). Para que o sistema funcione corretamente, duas regras de negócio cruciais e validações precisam ser implementadas:

1. **Validação na Criação da Viagem:**
   - O destino (`destination`) não pode ser nulo ou vazio.
   - A distância (`distanceKm`) e o peso da carga (`cargoWeightKg`) devem ser maiores que zero.
   - Uma viagem **não pode ser criada** se o peso da carga (`cargoWeightKg`) exceder a capacidade máxima do veículo atribuído (`maxCapacityKg`). Caso exceda, o sistema deve retornar um erro que resulte em um HTTP Status `400 Bad Request`.

2. **Cálculo de Consumo na Finalização:**
   - Ao alterar o status de uma viagem para `COMPLETED`, o sistema deve calcular automaticamente o consumo total de combustível em litros com base na fórmula:
     $$ConsumoEmLitros = \frac{DistanciaKm}{EficienciaDoVeiculo}$$
   - O valor calculado deve ser salvo no campo `fuelConsumptionLiters` e o status atualizado no banco.

---

## 🏗️ Estrutura do Projeto & O que Implementar

O projeto segue uma arquitetura em camadas padrão do Spring Boot. Os seguintes componentes **já estão prontos**:
* `pom.xml`: Configurado com Spring Web, JPA, Validation, MySQL e H2 para testes.
* Domínio e Persistência (`Vehicle`, `DeliveryTrip`, e seus respectivos `Repositories`).
* `DeliveryTripIntegrationTest`: Testes automatizados que validam o comportamento esperado da sua API.

### Sua Missão:
Você deve completar o código nos arquivos que contêm marcações `// TODO`:

1. **`DeliveryTripService.java`**: Implemente a lógica das regras de negócio nos métodos `createTrip` e `completeTrip`.
2. **`DeliveryTripController.java`**: Implemente os endpoints REST sob o path `/api/trips` para expor essas operações (garantindo os códigos HTTP corretos como `201 Created` e `200 OK`).

## 🚀 Como Executar o Projeto

### Pré-requisitos
* Java 17 instalado.
* Maven instalado (ou utilize o wrapper `./mvnw`).

### Executando os Testes (O seu balizador de sucesso)
Para rodar a suite de testes e verificar se o seu código atende aos requisitos e às boas práticas, execute o comando:

```bash
mvn clean test

```

> 💡 **Nota:** Os testes de integração utilizam uma base de dados H2 em memória, então você não precisa configurar um banco MySQL local apenas para rodar os testes.

### Rodando a Aplicação em Desenvolvimento

Caso queira subir a aplicação localmente para testar via Postman ou Insomnia, as configurações do banco de dados local estão localizadas em `src/main/resources/application.properties`. Você precisará de uma instância do MySQL rodando.

Para iniciar o servidor:

```bash
mvn spring-boot:run

```

---

## 📬 Entrega

1. Garanta que **todos os testes** estão passando com sucesso (`BUILD SUCCESS`).
2. Certifique-se de que não deixou comentários temporários ou códigos mortos.
3. Submeta o link do seu repositório ou envie o arquivo `.zip` conforme as instruções enviadas pelo seu recrutador.

*Boa sorte! Estamos ansiosos para ver sua solução.* 😊
