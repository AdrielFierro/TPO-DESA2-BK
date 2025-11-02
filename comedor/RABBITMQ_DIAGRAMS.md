# 📊 Diagramas del Sistema de Eventos

## 🔄 Flujo Completo: Crear una Reserva

```
┌─────────────┐
│   USUARIO   │
│  (Postman)  │
└──────┬──────┘
       │
       │ POST /reservations
       │ {userId: 10, locationId: 1, mealTime: "ALMUERZO", ...}
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│              SERVICIO COMEDOR (Spring Boot)                 │
│                                                             │
│  ┌───────────────────────┐                                 │
│  │ ReservationController │                                 │
│  └───────────┬───────────┘                                 │
│              │                                             │
│              │ createReservation(request)                  │
│              ▼                                             │
│  ┌───────────────────────┐                                 │
│  │  ReservationService   │                                 │
│  │                       │                                 │
│  │ 1️⃣ Validar horario    │                                 │
│  │    (debe ser inicio    │                                 │
│  │     de slot: 12:00,    │                                 │
│  │     13:00, no 12:30)   │                                 │
│  │                       │                                 │
│  │ 2️⃣ Validar capacidad  │                                 │
│  │    (max 10/hora)       │                                 │
│  │                       │                                 │
│  │ 3️⃣ Guardar en BD      │────────────────┐                │
│  │    reservation.save() │                │                │
│  │                       │                ▼                │
│  │ 4️⃣ Construir payload  │       ┌────────────────┐       │
│  │    ReservationUpdated  │       │   MySQL DB     │       │
│  │    Payload            │       │                │       │
│  │                       │       │ • reservations │       │
│  │ 5️⃣ Publicar evento    │       │ • locations    │       │
│  │    eventPublisher.    │       │ • ...          │       │
│  │    publishReservation │       └────────────────┘       │
│  │    Updated()          │                                 │
│  └───────────┬───────────┘                                 │
│              │                                             │
│              ▼                                             │
│  ┌────────────────────────────────────────┐               │
│  │        EventPublisher                  │               │
│  │                                        │               │
│  │ 1. Construir EventEnvelope             │               │
│  │    • eventId: UUID.random()            │               │
│  │    • eventType: "reservation.updated"  │               │
│  │    • occurredAt: now()                 │               │
│  │    • tenant: {UADE, CENTRO}            │               │
│  │    • actor: {userId, role}             │               │
│  │    • payload: {...}                    │               │
│  │                                        │               │
│  │ 2. rabbitTemplate.convertAndSend()     │               │
│  │    ├─ exchange: "comedor.events"       │               │
│  │    ├─ routingKey: "comedor.            │               │
│  │    │              reservation.updated" │               │
│  │    └─ message: EventEnvelope (JSON)    │               │
│  │                                        │               │
│  │ 3. Log resultado                       │               │
│  │    ✅ "Evento publicado exitosamente"   │               │
│  └────────────────┬───────────────────────┘               │
│                   │                                       │
└───────────────────┼───────────────────────────────────────┘
                    │
                    │ JSON sobre AMQP protocol
                    │ (puerto 5672)
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    RABBITMQ BROKER                          │
│                                                             │
│  ┌─────────────────────────────────────────┐               │
│  │  Exchange: comedor.events (Topic)       │               │
│  │                                         │               │
│  │  Recibe mensaje con routing key:       │               │
│  │  "comedor.reservation.updated"          │               │
│  │                                         │               │
│  │  Busca bindings que coincidan...        │               │
│  └─────────────────┬───────────────────────┘               │
│                    │                                       │
│                    │ Routing por pattern matching          │
│                    │                                       │
│  ┌─────────────────▼───────────────────────┐               │
│  │  Queue: comedor.reservation.updated     │               │
│  │                                         │               │
│  │  ┌───────────────────────────────────┐  │               │
│  │  │  Message 1 (JSON)                 │  │               │
│  │  │  {                                │  │               │
│  │  │    "eventId": "d6703...",         │  │               │
│  │  │    "eventType": "reservation...", │  │               │
│  │  │    "payload": {                   │  │               │
│  │  │      "reservationId": "123",      │  │               │
│  │  │      "action": "created",         │  │               │
│  │  │      ...                          │  │               │
│  │  │    }                              │  │               │
│  │  │  }                                │  │               │
│  │  └───────────────────────────────────┘  │               │
│  │                                         │               │
│  │  Estado: Ready (listo para consumir)    │               │
│  └─────────────────┬───────────────────────┘               │
│                    │                                       │
└────────────────────┼───────────────────────────────────────┘
                     │
                     │ Poll (consulta periódica)
                     │ o Push (notificación)
                     │
        ┌────────────┴────────────┬──────────────────┐
        │                         │                  │
        ▼                         ▼                  ▼
┌────────────────┐    ┌────────────────┐    ┌────────────────┐
│   CONSUMIDOR   │    │   CONSUMIDOR   │    │   CONSUMIDOR   │
│   LOCAL        │    │   EXTERNO 1    │    │   EXTERNO 2    │
│                │    │                │    │                │
│ EventListener  │    │  Servicio de   │    │  Servicio de   │
│   (mismo       │    │ Notificaciones │    │   Analytics    │
│   proyecto)    │    │   (Python)     │    │   (Node.js)    │
│                │    │                │    │                │
│ @RabbitListener│    │ • Lee mensaje  │    │ • Lee mensaje  │
│                │    │ • Envía email  │    │ • Actualiza    │
│ 1. Recibe msg  │    │ • ACK          │    │   métricas     │
│ 2. Deserializa │    │                │    │ • ACK          │
│ 3. Procesa     │    │                │    │                │
│ 4. ACK         │    │                │    │                │
│                │    │                │    │                │
│ Log:           │    │ Log:           │    │ Log:           │
│ 🔔 Evento      │    │ 📧 Email       │    │ 📊 Stats       │
│    recibido... │    │    enviado...  │    │    updated...  │
│ ✨ Nueva       │    │                │    │                │
│    reserva...  │    │                │    │                │
│ ✅ Procesado   │    │                │    │                │
└────────────────┘    └────────────────┘    └────────────────┘
```

## 🎯 Desglose de Componentes

### 1. EventEnvelope (El "sobre")

```
┌─────────────────────────────────────────────────┐
│             EventEnvelope<T>                    │
├─────────────────────────────────────────────────┤
│ Metadata (común a TODOS los eventos):          │
│                                                 │
│ • eventId        → "d6703cc8-..."   (tracking)  │
│ • eventType      → "reservation.updated"        │
│ • occurredAt     → 2025-11-01T21:00:00         │
│ • emittedAt      → 2025-11-01T21:00:01         │
│ • sourceModule   → "cafeteria-service"          │
│ • tenant {                                      │
│     orgId        → "UADE"                       │
│     campusId     → "CENTRO"                     │
│   }                                             │
│ • actor {                                       │
│     userId       → "10"                         │
│     role         → "student"                    │
│   }                                             │
│ • version        → "1.0"                        │
│                                                 │
├─────────────────────────────────────────────────┤
│ Payload (específico del tipo de evento):       │
│                                                 │
│ • ReservationUpdatedPayload                     │
│   ├─ reservationId                              │
│   ├─ startDateTime                              │
│   ├─ endDateTime                                │
│   ├─ action (created/confirmed/cancelled)       │
│   └─ locationId                                 │
│                                                 │
│        O                                        │
│                                                 │
│ • BillCreatedPayload                            │
│   ├─ id                                         │
│   ├─ date                                       │
│   ├─ subtotal                                   │
│   ├─ products []                                │
│   └─ reservationId (nullable)                   │
└─────────────────────────────────────────────────┘
```

### 2. RabbitMQ Exchange + Queue + Binding

```
                    EXCHANGE
          ┌───────────────────────┐
          │   comedor.events      │
          │   (Topic Exchange)    │
          └───────────┬───────────┘
                      │
         ┌────────────┴────────────┐
         │                         │
    Routing Key:             Routing Key:
 "comedor.reservation.    "comedor.bill.
       updated"                created"
         │                         │
         ▼                         ▼
    ┌─────────┐              ┌─────────┐
    │ BINDING │              │ BINDING │
    └────┬────┘              └────┬────┘
         │                         │
         ▼                         ▼
    ┌─────────────────┐      ┌─────────────────┐
    │     QUEUE       │      │     QUEUE       │
    │   reservation.  │      │      bill.      │
    │     updated     │      │     created     │
    └─────────────────┘      └─────────────────┘
         │                         │
         │                         │
    Consumidores             Consumidores
```

### 3. Flujo Temporal

```
T0: Usuario hace POST
     │
     ▼
T1: Controller recibe
     │
     ▼
T2: Service valida
     │
     ▼
T3: Se guarda en BD
     │
     ▼
T4: Se construye evento
     │
     ▼
T5: Se publica a RabbitMQ    ─────┐
     │                             │ ~1-5ms
     ▼                             │
T6: RabbitMQ recibe y encola  ◄───┘
     │
     │ (mensaje queda esperando)
     │
     ▼
T7: Listener 1 consume         ─────┐
     │                               │ asyncrónico
T8: Listener 2 consume         ◄────┤ (cada uno
     │                               │ a su ritmo)
T9: Listener 3 consume         ◄────┘
     │
     ▼
T10: Todos los listeners
     completaron
```

## 🔐 Garantías de Entrega

### Escenario 1: Consumer caído

```
[Reservation creada] → [RabbitMQ] → [Queue con 1 mensaje]
                                           │
                                           │ Esperando...
                                           │
[Consumer se inicia] ──────────────────────┘
                                           │
                                           ▼
                                    [Procesa mensaje]
                                           │
                                           ▼
                                      [Envía ACK]
```

**Resultado**: El mensaje esperó en la cola hasta que el consumer se recuperó.

### Escenario 2: Consumer falla al procesar

```
[Mensaje] → [Consumer]
                 │
                 ▼
            [Procesa...]
                 │
                 ▼
            [ERROR! 💥]
                 │
                 ▼
            [No envía ACK]
                 │
                 ▼
         [RabbitMQ detecta]
                 │
                 ▼
         [Reencola mensaje]
                 │
                 ▼
            [Reintenta]
```

**Resultado**: RabbitMQ reintenta hasta que tenga éxito o se alcance el límite de reintentos.

### Escenario 3: RabbitMQ caído

```
[Reservation creada] → [EventPublisher] → [❌ RabbitMQ DOWN]
                                                  │
                                                  ▼
                                          [Logea error]
                                                  │
                                                  ▼
                                    [Reserva guardada OK]
                                    [Evento NO enviado]
```

**Problema**: Se pierde el evento 😢

**Solución**: Implementar **Outbox Pattern**:
```
1. Guardar evento en tabla "event_outbox" en MISMA transacción
2. Scheduler lee tabla periódicamente
3. Reintenta enviar a RabbitMQ
4. Marca como "sent" cuando tiene éxito
```

## 📈 Patrones de Escalabilidad

### Multiple Consumers (Balanceo de Carga)

```
                    [Queue]
                       │
         ┌─────────────┼─────────────┐
         │             │             │
         ▼             ▼             ▼
    [Consumer 1]  [Consumer 2]  [Consumer 3]
         │             │             │
    Msg 1, 4, 7   Msg 2, 5, 8   Msg 3, 6, 9

    = Round-robin distribution
```

### Fanout (Broadcast)

```
[Exchange: fanout]
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
  [Q1] [Q2] [Q3]
    │    │    │
    ▼    ▼    ▼
  [C1] [C2] [C3]

= Mismo mensaje a TODOS
```

### Topic (Pattern Matching)

```
[Exchange: topic "comedor.*"]
                │
    ┌───────────┼───────────┐
    │           │           │
  "comedor.    "comedor.  "comedor.
reservation.    bill.      *"
  updated"     created"
    │           │           │
    ▼           ▼           ▼
  [Q1]        [Q2]        [Q3]
    │           │           │
Solo          Solo      TODOS los
reservas      bills     eventos
```

---

## 🎨 Ejemplo Visual del JSON Completo

```json
{
  // ═══════════════════════════════════════
  //         METADATA (EventEnvelope)
  // ═══════════════════════════════════════
  "eventId": "d6703cc8-9e79-415d-ac03-a4dc7f6ab43c",
  "eventType": "reservation.updated",
  "occurredAt": "2025-11-01T21:00:00",
  "emittedAt": "2025-11-01T21:00:01",
  "sourceModule": "cafeteria-service",
  
  "tenant": {
    "orgId": "UADE",
    "campusId": "CENTRO"
  },
  
  "actor": {
    "userId": "10",
    "role": "student"
  },
  
  "version": "1.0",
  
  // ═══════════════════════════════════════
  //    PAYLOAD (ReservationUpdatedPayload)
  // ═══════════════════════════════════════
  "payload": {
    "reservationId": "123",
    "startDateTime": "2025-11-05T12:00:00",
    "endDateTime": "2025-11-05T13:00:00",
    "title": "Reserva de comedor",
    "description": "Reserva de almuerzo creada en Norte",
    "action": "created",
    "locationId": "Norte"
  }
}
```

**Total tamaño**: ~500 bytes por evento

---

¡Espero que estos diagramas te ayuden a visualizar cómo funciona todo! 🎉
