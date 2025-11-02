# 🚀 Quick Start - Probar RabbitMQ

## 📋 Resumen de lo que implementamos

1. ✅ **Sistema de eventos asíncrono** con RabbitMQ
2. ✅ **2 tipos de eventos**:
   - `reservation.updated` (crear, confirmar, cancelar reservas)
   - `bill.created` (generar facturas)
3. ✅ **EventPublisher** que publica eventos
4. ✅ **EventListener** que consume eventos (para testing)
5. ✅ **Integración completa** en ReservationService y BillService

## 🎯 Pasos para Probar

### 1️⃣ Instalar RabbitMQ (Opción más fácil: Docker)

```bash
# Descargar e iniciar RabbitMQ
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# Verificar que está corriendo
docker ps

# Deberías ver algo como:
# CONTAINER ID   IMAGE                    STATUS
# abc123...      rabbitmq:3-management    Up 2 seconds
```

**Alternativa sin Docker**: Usa CloudAMQP (gratis)
- Ir a https://www.cloudamqp.com/
- Crear cuenta → Crear instancia gratuita
- Copiar URL de conexión
- Actualizar `application.properties`:
  ```properties
  spring.rabbitmq.addresses=amqps://usuario:password@servidor.cloudamqp.com/vhost
  ```

### 2️⃣ Acceder a RabbitMQ UI

```
URL: http://localhost:15672
Usuario: guest
Password: guest
```

Deberías ver el dashboard de RabbitMQ.

### 3️⃣ Iniciar tu Aplicación

```bash
# En una terminal NUEVA (no la que tiene la app corriendo)
cd /Users/adriel/Documents/github/TPO-DESA2-BK/comedor
./mvnw spring-boot:run
```

**Busca en los logs**:
```
✅ Started ComedorApplication in X seconds
✅ Conectado a RabbitMQ
✅ Exchange 'comedor.events' declarado
✅ Cola 'comedor.reservation.updated' declarada
✅ Cola 'comedor.bill.created' declarada
```

### 4️⃣ Verificar en RabbitMQ UI

1. Ir a http://localhost:15672
2. Pestaña **"Exchanges"** → Deberías ver `comedor.events`
3. Pestaña **"Queues"** → Deberías ver:
   - `comedor.reservation.updated` (0 mensajes)
   - `comedor.bill.created` (0 mensajes)

### 5️⃣ Crear una Reserva (Genera Evento)

```bash
curl -X POST http://localhost:4002/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 10,
    "locationId": 1,
    "mealTime": "ALMUERZO",
    "reservationDate": "2025-11-06T12:00:00"
  }'
```

**Qué pasa internamente**:
1. ✅ ReservationService guarda la reserva
2. 📤 EventPublisher publica evento a RabbitMQ
3. 🔔 EventListener lo recibe y procesa
4. 📊 RabbitMQ UI muestra el mensaje procesado

**En los logs verás**:
```
✅ Evento 'reservation.updated' publicado exitosamente. EventID: d6703cc8-...

═══════════════════════════════════════════════════════
🔔 EVENTO RECIBIDO: reservation.updated
═══════════════════════════════════════════════════════
📋 Event ID: d6703cc8-9e79-415d-ac03-a4dc7f6ab43c
⏰ Occurred At: 2025-11-01T21:00:00
📡 Emitted At: 2025-11-01T21:00:01
🏢 Organization: UADE
🏫 Campus: CENTRO
👤 User: 10 (student)
-----------------------------------------------------------
📦 PAYLOAD:
   🆔 Reservation ID: 123
   🎬 Action: created
   📍 Location: Norte
   🕐 Start: 2025-11-06T12:00:00
   🕑 End: 2025-11-06T13:00:00
   📝 Description: Reserva de almuerzo creada en Norte
-----------------------------------------------------------
✨ ACCIÓN: Nueva reserva creada
   📧 Enviando email de confirmación a usuario 10
═══════════════════════════════════════════════════════
✅ Evento procesado exitosamente
═══════════════════════════════════════════════════════
```

### 6️⃣ Ver el Evento en RabbitMQ UI

1. Ir a pestaña **"Queues"**
2. Click en `comedor.reservation.updated`
3. Si el listener lo procesó, verás:
   - **Messages**: 0 (ya fue consumido)
   - **Message rates**: Muestra que hubo actividad
4. En la sección **"Message rates"** verás un gráfico de los mensajes procesados

### 7️⃣ Probar Confirmación de Reserva

```bash
# Reemplaza {id} con el ID de la reserva creada
curl -X PUT http://localhost:4002/reservations/{id}/confirm
```

**En los logs**:
```
✅ Evento 'reservation.updated' publicado exitosamente...

🔔 EVENTO RECIBIDO: reservation.updated
...
   🎬 Action: confirmed
...
✅ ACCIÓN: Reserva confirmada por el usuario
   ⏰ Programando recordatorio 1 hora antes de ...
```

### 8️⃣ Probar Cancelación de Reserva

```bash
curl -X PUT http://localhost:4002/reservations/{id}/cancel
```

**En los logs**:
```
🔔 EVENTO RECIBIDO: reservation.updated
...
   🎬 Action: cancelled
...
❌ ACCIÓN: Reserva cancelada
   📊 Liberando capacidad para ...
```

### 9️⃣ Generar una Factura

```bash
# Primero crear un carrito (si no tienes uno)
curl -X POST http://localhost:4002/carts \
  -H "Content-Type: application/json" \
  -d '{"userId": 10}'

# Agregar productos al carrito
curl -X POST http://localhost:4002/carts/{cartId}/products/{productId}

# Confirmar carrito (genera factura)
curl -X POST http://localhost:4002/carts/{cartId}/confirm
```

**En los logs**:
```
✅ Evento 'bill.created' publicado exitosamente...

═══════════════════════════════════════════════════════
🔔 EVENTO RECIBIDO: bill.created
═══════════════════════════════════════════════════════
📦 PAYLOAD:
   🆔 Bill ID: bill-123
   📅 Date: 2025-11-01T21:00:00
   💰 Subtotal: $15000.00
   📦 Products: 2 items
   ------- Productos -------
      • Pastel de papa - $10000.00 (PLATO)
      • Coca Cola - $5000.00 (BEBIDA)
-----------------------------------------------------------
💼 ACCIONES:
   💼 Registrando factura bill-123 en sistema contable
   📦 Actualizando inventario (2 productos)
      ⬇️ Producto Pastel de papa vendido
      ⬇️ Producto Coca Cola vendido
   📊 Actualizando reporte de ventas ($15000.00 total)
═══════════════════════════════════════════════════════
✅ Evento procesado exitosamente
═══════════════════════════════════════════════════════
```

## 🎨 Bonus: Ver Eventos sin Consumirlos

Si quieres ver los eventos en RabbitMQ **SIN** que tu listener los consuma:

1. **Desactivar el listener temporalmente**:
   ```java
   // En EventListener.java, comenta esta línea:
   // @Component  ← Comentar esto
   public class EventListener {
   ```

2. **Reiniciar la app**

3. **Crear reserva**

4. **En RabbitMQ UI**:
   - Ir a la cola
   - Verás "1 message"
   - Click en "Get messages"
   - Ack mode: "Nack message requeue true"
   - Click "Get Message(s)"
   - Verás el JSON completo sin consumirlo

## 📊 Monitoreo

### RabbitMQ UI - Información útil

**Pestaña Overview**:
- Conexiones activas
- Canales abiertos
- Tasa de mensajes por segundo

**Pestaña Queues**:
- Mensajes en cola
- Tasa de consumo
- Mensajes no procesados (unacked)

**Pestaña Connections**:
- Apps conectadas
- Estado de conexión
- Usuario conectado

## 🐛 Troubleshooting

### ❌ Error: "Connection refused"
**Problema**: RabbitMQ no está corriendo

**Solución**:
```bash
# Si usas Docker
docker start rabbitmq

# Verificar
docker ps
```

### ❌ Error: "Queue not declared"
**Problema**: La cola no existe

**Solución**: La app debería crearlas automáticamente al iniciar. Verifica que `RabbitMQConfig` se esté cargando correctamente.

### ❌ No veo logs del listener
**Problema**: 
- El listener está desactivado (@Component comentado)
- Los mensajes se están consumiendo muy rápido

**Solución**: Verifica que `@Component` esté descomentado en `EventListener.java`

### ❌ Mensajes se acumulan en la cola
**Problema**: El listener tiene un error y rechaza los mensajes

**Solución**:
1. Ver logs de la app para encontrar el error
2. Corregir el error
3. Reiniciar la app
4. Los mensajes se reprocesarán

## 📝 Siguiente Paso: Consumir desde Otro Servicio

Una vez que funcione en tu app, puedes crear otro servicio (en Java, Python, Node.js, etc.) que consuma estos eventos.

Ver `RABBITMQ_GUIDE.md` para ejemplos de código de consumidores en otros lenguajes.

---

## ✅ Checklist

- [ ] RabbitMQ instalado y corriendo
- [ ] Puedo acceder a http://localhost:15672
- [ ] Mi app inicia sin errores
- [ ] Veo los exchanges y queues creados en RabbitMQ UI
- [ ] Creé una reserva y vi el evento en logs
- [ ] Confirmé una reserva y vi el evento
- [ ] Cancelé una reserva y vi el evento
- [ ] Generé una factura y vi el evento

¡Todo listo! 🎉
