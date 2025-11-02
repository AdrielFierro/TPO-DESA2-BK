# 📚 Guía Completa de RabbitMQ - Sistema de Eventos del Comedor

## 📖 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Conceptos Fundamentales](#conceptos-fundamentales)
3. [Arquitectura del Sistema](#arquitectura-del-sistema)
4. [Componentes del Código](#componentes-del-código)
5. [Flujo de Eventos](#flujo-de-eventos)
6. [Consumir Eventos desde Otros Servicios](#consumir-eventos-desde-otros-servicios)
7. [Patrones Avanzados](#patrones-avanzados)
8. [Troubleshooting](#troubleshooting)

---

## 🎯 Introducción

### ¿Qué es RabbitMQ?

RabbitMQ es un **message broker** (intermediario de mensajes) que facilita la comunicación entre diferentes servicios de manera asíncrona.

**Analogía simple**: Imagina RabbitMQ como una oficina de correos:
- Los **productores** son personas que envían cartas
- El **exchange** es el buzón central que recibe las cartas
- Las **queues** son los casilleros donde se almacenan las cartas
- Los **consumidores** son las personas que recogen las cartas de sus casilleros

### ¿Por qué usarlo?

✅ **Desacoplamiento**: Los servicios no necesitan conocerse entre sí
✅ **Escalabilidad**: Múltiples consumidores pueden procesar mensajes en paralelo
✅ **Fiabilidad**: Si un servicio cae, los mensajes se quedan en la cola esperando
✅ **Asincronía**: Tu API no se bloquea esperando que otros servicios procesen
✅ **Flexibilidad**: Puedes agregar nuevos consumidores sin modificar el productor

### ¿Cuándo NO usarlo?

❌ Necesitas respuesta inmediata (usa API REST síncrona)
❌ El orden estricto es crítico (usa Kafka o colas FIFO)
❌ Comunicación en tiempo real (usa WebSockets)
❌ Sistema muy simple con 1-2 servicios (puede ser overkill)

---

## 🧩 Conceptos Fundamentales

### 1. Producer (Productor)

El servicio que **envía** mensajes a RabbitMQ.

```java
// En nuestro caso: EventPublisher
eventPublisher.publishReservationUpdated(payload, userId, role);
```

**Responsabilidades**:
- Crear el mensaje con el formato correcto
- Enviarlo al exchange con el routing key adecuado
- Manejar errores de conexión

### 2. Exchange (Intercambiador)

El "router" que decide a qué cola(s) enviar cada mensaje.

**Tipos de Exchange**:

#### Topic Exchange (el que usamos)
Permite routing patterns flexibles con wildcards:
```
comedor.reservation.updated  → Cola de reservas
comedor.bill.created         → Cola de facturas
comedor.*                    → Todas las colas del comedor
*.reservation.*              → Todas las reservas de todos los módulos
```

#### Direct Exchange
Routing exacto, sin wildcards:
```
error   → Cola de errores
warning → Cola de warnings
```

#### Fanout Exchange
Broadcast: envía a TODAS las colas conectadas (ignora routing key)

#### Headers Exchange
Routing basado en headers del mensaje, no en routing key

**Nuestro exchange**:
```java
// En RabbitMQConfig.java
public static final String COMEDOR_EXCHANGE = "comedor.events";
```

### 3. Queue (Cola)

Donde se **almacenan** los mensajes esperando ser procesados.

**Propiedades importantes**:

```java
// durable = true → La cola sobrevive a reinicios de RabbitMQ
Queue queue = new Queue("comedor.reservation.updated", true);
```

**Características**:
- **FIFO**: First In, First Out (el primero en entrar es el primero en salir)
- **Persistente**: Los mensajes se guardan en disco
- **Multiple consumers**: Varios consumidores pueden leer de la misma cola (balanceo de carga)

**Nuestras colas**:
```java
comedor.reservation.updated  // Eventos de reservas
comedor.bill.created         // Eventos de facturas
```

### 4. Binding (Enlace)

La "regla" que conecta un exchange con una queue.

```java
// "Si un mensaje llega al exchange 'comedor.events' con routing key 
// 'comedor.reservation.updated', envíalo a la cola 'reservation.updated'"
BindingBuilder
    .bind(reservationUpdatedQueue)
    .to(comedorExchange)
    .with(RESERVATION_UPDATED_ROUTING_KEY);
```

### 5. Consumer (Consumidor)

El servicio que **lee y procesa** mensajes de una cola.

```java
@RabbitListener(queues = "comedor.reservation.updated")
public void handleReservationUpdated(EventEnvelope<ReservationUpdatedPayload> event) {
    // Procesar el evento
}
```

**Workflow del consumidor**:
1. Se conecta a RabbitMQ
2. Se suscribe a una cola
3. Espera mensajes (polling o push)
4. Cuando llega un mensaje, lo procesa
5. Envía ACK (acknowledgement) si todo salió bien
6. Si falla, puede reintentarse o ir a DLQ (Dead Letter Queue)

### 6. Routing Key

La "etiqueta" que lleva cada mensaje para que el exchange sepa a dónde enviarlo.

**Convención de nombres** (la que usamos):
```
modulo.entidad.accion
└─────┘ └─────┘ └────┘
   │       │       └─── Qué pasó (created, updated, deleted)
   │       └────────── Sobre qué (reservation, bill, product)
   └─────────────────── De qué servicio (comedor, inventario, users)

Ejemplos:
comedor.reservation.updated
comedor.bill.created
inventario.product.stock-low
users.user.registered
```

---

## 🏗️ Arquitectura del Sistema

### Vista de Alto Nivel

```
┌─────────────────────────────────────────────────────────┐
│                 MÓDULO COMEDOR                          │
│                                                         │
│  ┌──────────────┐                ┌─────────────────┐   │
│  │ Reservation  │──────event───→ │ EventPublisher  │   │
│  │   Service    │                └────────┬────────┘   │
│  └──────────────┘                         │            │
│                                            │            │
│  ┌──────────────┐                         │            │
│  │    Bill      │──────event───→          │            │
│  │   Service    │                         │            │
│  └──────────────┘                         ▼            │
│                                  ┌──────────────────┐   │
│                                  │  RabbitTemplate  │   │
│                                  └────────┬─────────┘   │
└──────────────────────────────────────────┼─────────────┘
                                            │
                                   JSON/AMQP
                                            │
                    ┌───────────────────────▼────────────────────────┐
                    │          RABBITMQ BROKER                       │
                    │                                                │
                    │  ┌──────────────────┐                         │
                    │  │  comedor.events  │ (Exchange)              │
                    │  └────────┬─────────┘                         │
                    │           │                                   │
                    │  ┌────────┴────────┐                          │
                    │  │                 │                          │
                    │  ▼                 ▼                          │
                    │ [Queue 1]       [Queue 2]                     │
                    │ reservation     bill                          │
                    │  .updated      .created                       │
                    └──┬────────────────┬──────────────────────────┘
                       │                │
        ┌──────────────┴────────┬───────┴──────────┬──────────────┐
        │                       │                  │              │
        ▼                       ▼                  ▼              ▼
┌───────────────┐    ┌────────────────┐   ┌──────────────┐  ┌─────────┐
│ EventListener │    │   Servicio de  │   │  Servicio de │  │  Otro   │
│  (local)      │    │ Notificaciones │   │  Analytics   │  │Servicio │
│               │    │   (Python)     │   │  (Node.js)   │  │         │
│ • Logs        │    │ • Envía emails │   │ • Métricas   │  │  ...    │
│ • Testing     │    │ • SMS          │   │ • Dashboards │  │         │
└───────────────┘    └────────────────┘   └──────────────┘  └─────────┘
```

### Flujo de Datos Detallado

**Paso 1**: Usuario crea una reserva
```
POST /reservations
↓
ReservationController
↓
ReservationService.createReservation()
```

**Paso 2**: Se guarda en la base de datos
```sql
INSERT INTO reservations (...) VALUES (...);
```

**Paso 3**: Se construye el evento
```java
ReservationUpdatedPayload payload = ReservationUpdatedPayload.builder()
    .reservationId("123")
    .action(created)
    .startDateTime(...)
    .build();
```

**Paso 4**: Se envuelve en EventEnvelope
```java
EventEnvelope envelope = EventEnvelope.builder()
    .eventId(UUID.randomUUID())
    .eventType("reservation.updated")
    .payload(payload)
    .tenant(...)
    .actor(...)
    .build();
```

**Paso 5**: Se publica a RabbitMQ
```java
rabbitTemplate.convertAndSend(
    "comedor.events",                    // Exchange
    "comedor.reservation.updated",       // Routing key
    envelope                             // Mensaje (se convierte a JSON)
);
```

**Paso 6**: RabbitMQ procesa
```
1. Recibe el mensaje en el exchange "comedor.events"
2. Busca bindings que coincidan con "comedor.reservation.updated"
3. Encuentra la queue "comedor.reservation.updated"
4. Guarda el mensaje en la queue
```

**Paso 7**: Consumidores procesan
```
Listener 1 (local): Log del evento
Listener 2 (Python): Envía email de confirmación
Listener 3 (Node.js): Actualiza métricas en dashboard
```

---

## 💻 Componentes del Código

### 1. EventEnvelope.java

**Propósito**: El "sobre" que envuelve TODOS los eventos

**Estructura**:
```java
{
  // Metadata de tracking
  "eventId": "d6703cc8-...",           // UUID único
  "eventType": "reservation.updated",   // Tipo de evento
  "occurredAt": "2025-11-01T21:00:00", // Cuándo ocurrió
  "emittedAt": "2025-11-01T21:00:01",  // Cuándo se publicó
  
  // Contexto organizacional
  "tenant": {
    "orgId": "UADE",
    "campusId": "CENTRO"
  },
  
  // Quién lo hizo
  "actor": {
    "userId": "10",
    "role": "student"
  },
  
  // Versionado
  "version": "1.0",
  
  // Los datos específicos del evento
  "payload": { ... }
}
```

**Ventajas**:
- ✅ Auditoría completa (quién, cuándo, dónde)
- ✅ Multi-tenancy (mismo RabbitMQ para múltiples organizaciones)
- ✅ Versionado (evolucionar el schema sin romper consumers antiguos)
- ✅ Debugging (eventId para rastrear un evento específico)

### 2. ReservationUpdatedPayload.java

**Propósito**: Datos específicos de eventos de reservas

**Campos**:
```java
{
  "reservationId": "123",
  "startDateTime": "2025-11-05T12:00:00",
  "endDateTime": "2025-11-05T13:00:00",
  "title": "Reserva de comedor",
  "description": "Reserva de almuerzo creada en Norte",
  "action": "created",  // created, updated, confirmed, cancelled, deleted
  "locationId": "Norte"
}
```

**¿Por qué incluir startDateTime/endDateTime?**
Para que los consumidores puedan:
- Crear eventos de calendario automáticamente
- Enviar recordatorios 1 hora antes
- Validar conflictos de horario
- Mostrar en dashboards de ocupación

### 3. BillCreatedPayload.java

**Propósito**: Datos específicos de eventos de facturas

**Campos**:
```java
{
  "id": "bill-123",
  "date": "2025-11-01T14:15:00",
  "subtotal": 15000.00,
  "reservationId": "resv-456",  // Puede ser null
  "products": [
    {
      "id": 1,
      "name": "Pastel de papa",
      "price": 10000,
      "productType": "PLATO",
      "image": "https://..."
    },
    ...
  ]
}
```

**Usos**:
- Sistema contable: registrar ingresos
- Inventario: decrementar stock de productos vendidos
- Analytics: métricas de ventas por producto/ubicación/hora
- Marketing: identificar productos más vendidos

### 4. RabbitMQConfig.java

**Propósito**: Configuración de toda la infraestructura de mensajería

**Componentes que declara**:

#### Exchange
```java
@Bean
public TopicExchange comedorExchange() {
    return new TopicExchange("comedor.events");
}
```

#### Queues
```java
@Bean
public Queue reservationUpdatedQueue() {
    return new Queue("comedor.reservation.updated", true);
    //                                               ↑
    //                                            durable
}
```

#### Bindings
```java
@Bean
public Binding reservationUpdatedBinding(
    Queue reservationUpdatedQueue, 
    TopicExchange comedorExchange
) {
    return BindingBuilder
        .bind(reservationUpdatedQueue)
        .to(comedorExchange)
        .with("comedor.reservation.updated");
}
```

#### RabbitTemplate
```java
@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    // Configuración adicional si es necesaria
    return template;
}
```

#### ObjectMapper
```java
@Bean
public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());  // Para LocalDateTime
    return mapper;
}
```

**Startup**:
Cuando la aplicación inicia, Spring:
1. Conecta a RabbitMQ (usando config de application.properties)
2. Declara el exchange "comedor.events" si no existe
3. Declara las queues si no existen
4. Crea los bindings si no existen

### 5. EventPublisher.java

**Propósito**: Servicio centralizado para publicar eventos

**Métodos principales**:

#### publishReservationUpdated()
```java
public <T> void publishReservationUpdated(T payload, String userId, String userRole) {
    EventEnvelope<T> envelope = buildEventEnvelope(
        "reservation.updated", payload, userId, userRole
    );
    
    try {
        rabbitTemplate.convertAndSend(
            COMEDOR_EXCHANGE,                    // Dónde
            RESERVATION_UPDATED_ROUTING_KEY,     // Con qué etiqueta
            envelope                             // Qué
        );
        logger.info("✅ Evento publicado");
    } catch (Exception e) {
        logger.error("❌ Error: {}", e.getMessage());
    }
}
```

#### buildEventEnvelope()
```java
private <T> EventEnvelope<T> buildEventEnvelope(...) {
    return EventEnvelope.<T>builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(eventType)
        .occurredAt(LocalDateTime.now())
        .emittedAt(LocalDateTime.now())
        .sourceModule("cafeteria-service")
        .tenant(...)
        .actor(...)
        .version("1.0")
        .payload(payload)
        .build();
}
```

**¿Por qué centralizar?**
- ✅ Consistencia: todos los eventos tienen el mismo formato
- ✅ Mantenibilidad: un solo lugar para cambiar la lógica
- ✅ Testing: fácil de mockear
- ✅ Logging: un solo lugar para auditar

### 6. EventListener.java

**Propósito**: Consumidor local para testing y procesamiento interno

**Características**:

#### @RabbitListener
```java
@RabbitListener(queues = "comedor.reservation.updated")
public void handleReservationUpdated(EventEnvelope<ReservationUpdatedPayload> event) {
    // Spring automáticamente:
    // 1. Deserializa el JSON a EventEnvelope
    // 2. Llama a este método
    // 3. Si termina sin excepción, envía ACK
    // 4. Si lanza excepción, reintenta o envía a DLQ
}
```

#### Deserialización automática
Spring Boot + Jackson automáticamente:
```
JSON String → EventEnvelope<ReservationUpdatedPayload>
```

Gracias a:
- `ObjectMapper` con `JavaTimeModule`
- Anotaciones `@Data` y `@Builder` de Lombok

#### Manejo de errores
```java
@RabbitListener(queues = "...")
public void handleEvent(EventEnvelope event) {
    try {
        processEvent(event);
        // Si llega aquí → ACK automático
    } catch (BusinessException e) {
        // Loggear y continuar → ACK (no reintentar)
        logger.error("Error de negocio: {}", e);
    } catch (Exception e) {
        // Lanzar excepción → NACK (reintentar)
        throw new RuntimeException("Error crítico", e);
    }
}
```

### 7. Integración en Services

#### ReservationService.java

**Antes** (sin eventos):
```java
public Reservation createReservation(...) {
    // Validar
    // Guardar
    return reservation;
}
```

**Después** (con eventos):
```java
public Reservation createReservation(...) {
    // Validar
    // Guardar
    Reservation saved = repository.save(reservation);
    
    // 🎉 NUEVO: Publicar evento
    publishReservationEvent(saved, location, slot, ReservationAction.created);
    
    return saved;
}

private void publishReservationEvent(...) {
    ReservationUpdatedPayload payload = ReservationUpdatedPayload.builder()
        .reservationId(reservation.getId().toString())
        .startDateTime(...)
        .endDateTime(...)
        .action(action)
        .locationId(location.getName())
        .build();
    
    eventPublisher.publishReservationUpdated(
        payload, 
        reservation.getUserId().toString(), 
        "student"
    );
}
```

**Beneficios**:
- Otros servicios se enteran automáticamente
- No bloqueas la respuesta HTTP esperando emails/notificaciones
- Escalable: agregar nuevos consumidores sin tocar este código

#### BillService.java

Similar a ReservationService:
```java
public Bill createBillFromCart(Cart cart) {
    // Crear factura
    Bill saved = repository.save(bill);
    
    // 🎉 Publicar evento
    publishBillCreatedEvent(saved);
    
    return saved;
}
```

---

## 🔄 Flujo de Eventos

### Ejemplo Completo: Usuario Crea una Reserva

**T0: Request HTTP**
```bash
curl -X POST http://localhost:4002/reservations \
  -d '{"userId": 10, "mealTime": "ALMUERZO", ...}'
```

**T1: Controller (0ms)**
```java
@PostMapping("/reservations")
public Reservation create(@RequestBody CreateReservationRequest req) {
    return reservationService.createReservation(req);
}
```

**T2: Service - Validación (5ms)**
```java
// Validar horario exacto (12:00, no 12:30)
// Validar capacidad disponible
// Calcular costo
```

**T3: Service - Persistencia (15ms)**
```java
Reservation saved = repository.save(reservation);
```

**T4: Service - Construcción de Evento (1ms)**
```java
ReservationUpdatedPayload payload = ...
EventEnvelope envelope = ...
```

**T5: EventPublisher - Envío a RabbitMQ (2ms)**
```java
rabbitTemplate.convertAndSend(exchange, routingKey, envelope);
```

**T6: Response HTTP (23ms total)**
```json
{
  "id": 123,
  "userId": 10,
  "status": "ACTIVA",
  ...
}
```

**T7: RabbitMQ - Enrutamiento (1ms)**
```
Exchange "comedor.events" 
  → Routing key "comedor.reservation.updated"
  → Queue "comedor.reservation.updated"
```

**T8: EventListener - Consumo (50ms después)**
```java
@RabbitListener(queues = "comedor.reservation.updated")
public void handleReservationUpdated(EventEnvelope event) {
    logger.info("🔔 Evento recibido: {}", event);
    
    switch (payload.getAction()) {
        case created:
            sendConfirmationEmail(userId);  // 200ms
            createCalendarEvent();          // 100ms
            updateMetrics();                // 50ms
            break;
    }
}
```

**Tiempo total percibido por el usuario**: 23ms
**Tiempo total real de procesamiento**: 23ms + 350ms = 373ms

Pero el usuario NO espera los 350ms adicionales! ✨

---

## 🌐 Consumir Eventos desde Otros Servicios

### Opción 1: Servicio en Java (Spring Boot)

**1. Dependencias (pom.xml)**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

**2. Configuración (application.properties)**
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

**3. Listener**
```java
@Service
public class NotificationService {
    
    @RabbitListener(queues = "comedor.reservation.updated")
    public void handleReservation(EventEnvelope<ReservationUpdatedPayload> event) {
        ReservationUpdatedPayload payload = event.getPayload();
        
        if (payload.getAction() == ReservationAction.created) {
            String userEmail = getUserEmail(event.getActor().getUserId());
            emailService.send(userEmail, "Reserva confirmada", ...);
        }
    }
}
```

### Opción 2: Servicio en Python

**1. Instalar dependencias**
```bash
pip install pika python-dotenv
```

**2. Consumidor**
```python
import pika
import json
from datetime import datetime

# Conectar a RabbitMQ
connection = pika.BlockingConnection(
    pika.ConnectionParameters('localhost', 5672, '/', 
        pika.PlainCredentials('guest', 'guest'))
)
channel = connection.channel()

# Declarar la cola (idempotente)
channel.queue_declare(queue='comedor.reservation.updated', durable=True)

def callback(ch, method, properties, body):
    """Callback llamado cuando llega un mensaje"""
    try:
        # Deserializar JSON
        event = json.loads(body)
        payload = event['payload']
        
        print(f"🔔 Evento recibido: {event['eventType']}")
        print(f"📋 Reservation ID: {payload['reservationId']}")
        print(f"🎬 Action: {payload['action']}")
        
        # Lógica de negocio
        if payload['action'] == 'created':
            user_id = event['actor']['userId']
            send_confirmation_email(user_id, payload)
        
        # ACK manual
        ch.basic_ack(delivery_tag=method.delivery_tag)
        
    except Exception as e:
        print(f"❌ Error: {e}")
        # NACK para reintentar
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)

# Configurar consumidor
channel.basic_qos(prefetch_count=1)  # Procesar 1 mensaje a la vez
channel.basic_consume(
    queue='comedor.reservation.updated',
    on_message_callback=callback,
    auto_ack=False  # ACK manual
)

print('🎧 Esperando mensajes...')
channel.start_consuming()
```

### Opción 3: Servicio en Node.js

**1. Instalar dependencias**
```bash
npm install amqplib
```

**2. Consumidor**
```javascript
const amqp = require('amqplib');

async function startConsumer() {
    try {
        // Conectar
        const connection = await amqp.connect('amqp://guest:guest@localhost:5672');
        const channel = await connection.createChannel();
        
        // Declarar cola
        const queue = 'comedor.reservation.updated';
        await channel.assertQueue(queue, { durable: true });
        
        console.log('🎧 Esperando mensajes...');
        
        // Consumir
        channel.consume(queue, async (msg) => {
            if (msg !== null) {
                try {
                    // Parsear JSON
                    const event = JSON.parse(msg.content.toString());
                    const payload = event.payload;
                    
                    console.log(`🔔 Evento: ${event.eventType}`);
                    console.log(`📋 Reservation ID: ${payload.reservationId}`);
                    
                    // Lógica de negocio
                    if (payload.action === 'created') {
                        await updateAnalyticsDashboard(payload);
                        await incrementMetrics(payload.locationId);
                    }
                    
                    // ACK
                    channel.ack(msg);
                    
                } catch (error) {
                    console.error('❌ Error:', error);
                    // NACK con requeue
                    channel.nack(msg, false, true);
                }
            }
        }, { noAck: false });
        
    } catch (error) {
        console.error('Error conectando a RabbitMQ:', error);
    }
}

startConsumer();
```

---

## 🔬 Patrones Avanzados

### 1. Outbox Pattern (Garantía de Entrega)

**Problema**: ¿Qué pasa si guardas en BD pero RabbitMQ está caído?

```
[Guardar Reservation] ✅
[Publicar Evento] ❌ RabbitMQ DOWN
→ La reserva existe pero nadie se enteró
```

**Solución**: Outbox Pattern

**Paso 1: Crear tabla de outbox**
```sql
CREATE TABLE event_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id VARCHAR(36) UNIQUE,
    event_type VARCHAR(100),
    payload JSON,
    status ENUM('PENDING', 'SENT', 'FAILED'),
    created_at TIMESTAMP,
    sent_at TIMESTAMP,
    retry_count INT DEFAULT 0
);
```

**Paso 2: Guardar evento en misma transacción**
```java
@Transactional
public Reservation createReservation(...) {
    // Guardar reserva
    Reservation saved = repository.save(reservation);
    
    // Guardar evento en outbox (MISMA TRANSACCIÓN)
    EventOutbox outbox = new EventOutbox();
    outbox.setEventId(UUID.randomUUID().toString());
    outbox.setEventType("reservation.updated");
    outbox.setPayload(buildPayload(saved));
    outbox.setStatus(EventStatus.PENDING);
    outboxRepository.save(outbox);
    
    return saved;
}
```

**Paso 3: Scheduler que procesa outbox**
```java
@Scheduled(fixedDelay = 5000)  // Cada 5 segundos
public void processOutbox() {
    List<EventOutbox> pending = outboxRepository.findByStatus(PENDING);
    
    for (EventOutbox event : pending) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event.getPayload());
            
            event.setStatus(SENT);
            event.setSentAt(LocalDateTime.now());
            outboxRepository.save(event);
            
        } catch (Exception e) {
            event.setRetryCount(event.getRetryCount() + 1);
            
            if (event.getRetryCount() >= 3) {
                event.setStatus(FAILED);
                alertService.notifyAdmins("Evento no enviado: " + event.getEventId());
            }
            
            outboxRepository.save(event);
        }
    }
}
```

**Ventajas**:
- ✅ Garantía de que el evento se envió (eventualmente)
- ✅ No se pierden eventos si RabbitMQ está caído
- ✅ Auditoría completa en BD
- ✅ Reintentos automáticos

### 2. Dead Letter Queue (DLQ)

**Problema**: ¿Qué pasa si un mensaje falla repetidamente?

**Solución**: Enviar a una cola especial de "mensajes muertos"

**Configuración**:
```java
@Bean
public Queue mainQueue() {
    return QueueBuilder.durable("comedor.reservation.updated")
        .withArgument("x-dead-letter-exchange", "comedor.dlx")
        .withArgument("x-dead-letter-routing-key", "dlq.reservation")
        .build();
}

@Bean
public Queue deadLetterQueue() {
    return new Queue("comedor.reservation.dlq", true);
}

@Bean
public DirectExchange deadLetterExchange() {
    return new DirectExchange("comedor.dlx");
}

@Bean
public Binding deadLetterBinding() {
    return BindingBuilder
        .bind(deadLetterQueue())
        .to(deadLetterExchange())
        .with("dlq.reservation");
}
```

**Listener con reintentos**:
```java
@RabbitListener(queues = "comedor.reservation.updated")
public void handleEvent(EventEnvelope event, 
                        @Header(AmqpHeaders.REDELIVERED) boolean redelivered) {
    try {
        processEvent(event);
    } catch (Exception e) {
        if (redelivered) {
            // Ya se reintentó, enviarlo a DLQ
            logger.error("Enviando a DLQ después de fallo: {}", event.getEventId());
            throw new AmqpRejectAndDontRequeueException("Max retries reached", e);
        } else {
            // Primer intento, reintentar
            throw e;
        }
    }
}
```

**Monitor de DLQ**:
```java
@Scheduled(fixedDelay = 60000)  // Cada minuto
public void checkDLQ() {
    long count = rabbitAdmin.getQueueInfo("comedor.reservation.dlq")
        .getMessageCount();
    
    if (count > 0) {
        alertService.notifyAdmins(
            "⚠️ Hay " + count + " mensajes en DLQ que requieren atención"
        );
    }
}
```

### 3. Priority Queues

**Problema**: Algunos eventos son más urgentes que otros

**Solución**: Colas con prioridad

```java
@Bean
public Queue priorityQueue() {
    return QueueBuilder.durable("comedor.reservations")
        .withArgument("x-max-priority", 10)
        .build();
}

// Al publicar
rabbitTemplate.convertAndSend(
    exchange, 
    routingKey, 
    message,
    msg -> {
        msg.getMessageProperties().setPriority(event.isVIP() ? 10 : 5);
        return msg;
    }
);
```

### 4. Message TTL (Time To Live)

**Problema**: Eventos muy antiguos ya no son útiles

**Solución**: Auto-expiración

```java
@Bean
public Queue queueWithTTL() {
    return QueueBuilder.durable("comedor.notifications")
        .withArgument("x-message-ttl", 3600000)  // 1 hora
        .build();
}
```

---

## 🐛 Troubleshooting

### Error: "Connection refused"

**Síntoma**:
```
AMQPConnectionException: Connection refused
```

**Causa**: RabbitMQ no está corriendo

**Solución**:
```bash
# Docker
docker start rabbitmq

# Verificar
docker ps | grep rabbitmq

# Ver logs
docker logs rabbitmq
```

### Error: "Queue not declared"

**Síntoma**:
```
Channel shutdown: channel error; protocol method: #method<channel.close>
(reply-code=404, reply-text=NOT_FOUND - no queue 'comedor.reservation.updated')
```

**Causa**: La cola no existe en RabbitMQ

**Solución**:
1. Verificar que `RabbitMQConfig` tiene `@Configuration`
2. Verificar que los `@Bean` están correctos
3. Reiniciar la aplicación para que declare las colas
4. O crear manualmente en RabbitMQ UI

### Error: Mensajes se acumulan sin procesarse

**Síntoma**: En RabbitMQ UI ves "Messages: 100, Consumers: 0"

**Causa**: No hay consumidores activos

**Solución**:
1. Verificar que `EventListener` tiene `@Component`
2. Verificar que `@RabbitListener` no está comentado
3. Ver logs de la app buscando errores de conexión
4. Verificar que el nombre de la cola coincide exactamente

### Error: Consumer crashea continuamente

**Síntoma**: Mensajes se reprocesen infinitamente

**Causa**: Error en el código del listener que lanza excepción

**Solución**:
```java
@RabbitListener(queues = "...")
public void handle(Event event) {
    try {
        dangerousOperation(event);
    } catch (BusinessException e) {
        // Error esperado, loggear y ACK
        logger.warn("Error de negocio: {}", e);
        // No lanzar excepción → ACK automático
    } catch (Exception e) {
        // Error inesperado, loggear y NACK
        logger.error("Error crítico: {}", e);
        throw new AmqpRejectAndDontRequeueException(e);
    }
}
```

### Debugging: Ver mensajes sin consumirlos

**En RabbitMQ UI**:
1. Ir a pestaña "Queues"
2. Click en la cola
3. Scroll a "Get messages"
4. Ack mode: "Nack message requeue true"
5. Click "Get Message(s)"

**Resultado**: Ves el contenido del mensaje pero NO se consume (queda en la cola)

---

## 📊 Monitoreo y Métricas

### RabbitMQ Management UI

**URL**: http://localhost:15672

**Métricas importantes**:

**Overview**:
- Message rates: Mensajes/segundo publicados y consumidos
- Connections: Número de conexiones activas
- Channels: Número de canales abiertos

**Queues**:
- Messages: Total en cola
- Message rates: Tasa de entrada/salida
- Consumers: Número de consumidores activos
- Unacked: Mensajes en procesamiento (no han recibido ACK)

**Exchanges**:
- Message rates in/out
- Bindings: Qué colas están conectadas

### Alertas Recomendadas

```java
@Scheduled(fixedDelay = 60000)
public void checkRabbitMQHealth() {
    // 1. Verificar colas con muchos mensajes
    long pendingMessages = getQueueSize("comedor.reservation.updated");
    if (pendingMessages > 1000) {
        alert("⚠️ Cola con " + pendingMessages + " mensajes pendientes");
    }
    
    // 2. Verificar DLQ
    long dlqSize = getQueueSize("comedor.reservation.dlq");
    if (dlqSize > 0) {
        alert("🚨 " + dlqSize + " mensajes en DLQ");
    }
    
    // 3. Verificar consumidores
    int consumers = getConsumerCount("comedor.reservation.updated");
    if (consumers == 0) {
        alert("❌ No hay consumidores activos para reservations");
    }
}
```

---

## ✅ Checklist de Producción

Antes de llevar a producción:

- [ ] **Seguridad**: Cambiar usuario/password por defecto
- [ ] **Persistencia**: Configurar `durable=true` en queues
- [ ] **Monitoring**: Configurar alertas
- [ ] **DLQ**: Implementar Dead Letter Queues
- [ ] **Logging**: Logs estructurados con eventId
- [ ] **Retry**: Política de reintentos clara
- [ ] **Documentation**: Documentar cada tipo de evento
- [ ] **Versionado**: Strategy para evolucionar schemas
- [ ] **Testing**: Tests de integración con RabbitMQ embedded
- [ ] **Capacity**: Calcular throughput necesario
- [ ] **Backup**: Plan de backup de mensajes críticos
- [ ] **Disaster Recovery**: ¿Qué pasa si se pierde RabbitMQ?

---

¡Felicidades! Ahora tienes un sistema de eventos completo y production-ready 🎉
