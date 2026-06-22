import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuração do cenário de teste de carga
export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp-up: sobe de 0 a 50 usuários virtuais
    { duration: '1m', target: 50 },   // Carga constante: mantêm 50 usuários ativos
    { duration: '15s', target: 0 },   // Ramp-down: reduz a carga até zero
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],   // Menos de 1% de falhas
    http_req_duration: ['p(95)<200'], // 95% das requisições devem responder em menos de 200ms
  },
};

const BASE_URL = __ENV.API_URL || 'http://localhost:8080/api';

export default function () {
  // Dados fictícios do veículo ID 1 (cadastrado na inicialização)
  const payload = JSON.stringify({
    vehicleId: 1,
    destination: 'Filial Central - Campinas',
    cargoWeightKg: 150.0,
    distanceKm: 85.5,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 1. Criar uma Viagem de Entrega
  const createRes = http.post(`${BASE_URL}/trips`, payload, params);
  
  const tripCreated = check(createRes, {
    'Criado com sucesso (status 201)': (r) => r.status === 201,
    'Contém ID da viagem': (r) => {
      try {
        return JSON.parse(r.body).id !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  if (tripCreated) {
    const tripId = JSON.parse(createRes.body).id;

    // Pequena pausa simulando comportamento do usuário/rede (100ms a 500ms)
    sleep(Math.random() * 0.4 + 0.1);

    // 2. Finalizar/Completar a Viagem de Entrega
    const completeRes = http.post(`${BASE_URL}/trips/${tripId}/complete`, null, params);
    
    check(completeRes, {
      'Finalizado com sucesso (status 200)': (r) => r.status === 200,
      'Status atualizado para COMPLETED': (r) => {
        try {
          return JSON.parse(r.body).status === 'COMPLETED';
        } catch (e) {
          return false;
        }
      },
    });
  }

  // Pausa curta entre iterações do VU
  sleep(1);
}
