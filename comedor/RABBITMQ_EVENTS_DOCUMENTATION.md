# 📨 Documentación de Eventos RabbitMQ - Módulo Comedor

## 🔧 Configuración RabbitMQ

### Credenciales
```properties
Host: ec2-54-225-17-132.compute-1.amazonaws.com
Port: 5672
Username: RabbitMQ_user
Password: aGzLNMYhU72P22
```

---

## 📊 Exchanges y Queues

### 1️⃣ **Facturas (Bills)**

**Exchange:**
- **Nombre:** `bill.event`
- **Tipo:** Topic
- **Durable:** Sí

**Routing Key:**
- `bill.created` - Se publica cuando se crea una nueva factura

**Queue sugerida para consumir:**
- **Nombre:** `comedor.queue` (o la que definas en tu módulo)
- **Binding:** `bill.created`

---

### 2️⃣ **Reservas (Reservations)**

**Exchange:**
- **Nombre:** `reservation.event`
- **Tipo:** Topic
- **Durable:** Sí

**Routing Key:**
- `reservation.created` - Se publica cuando se crea una nueva reserva

**Queue sugerida para consumir:**
- **Nombre:** `<tu-modulo>.reservation.queue` (define según tu módulo)
- **Binding:** `reservation.created`

---

## 📤 Evento: Factura Creada

### **Routing Key:** `bill.created`
### **Exchange:** `bill.event`

### **¿Cuándo se dispara?**
Cuando un usuario confirma un carrito, automáticamente se genera una factura y se publica este evento.

### **Estructura del Mensaje (JSON):**

```json
{
  "billId": 123,
  "userId": "00debe32-abd2-45a8-bece-3d3b752fa140",
  "cartId": 456,
  "reservationId": 789,
  "subtotal": 150.00,
  "totalWithDiscount": 125.00,
  "totalWithoutDiscount": 150.00,
  "createdAt": "2025-12-09T20:30:00",
  "products": [
    {
      "productId": 1,
      "name": "Milanesa con puré",
      "price": 75.00
    },
    {
      "productId": 2,
      "name": "Coca Cola",
      "price": 50.00
    },
    {
      "productId": 3,
      "name": "Flan casero",
      "price": 25.00
    }
  ]
}
```

### **Campos del Evento:**

| Campo | Tipo | Descripción | Puede ser null |
|-------|------|-------------|----------------|
| `billId` | Long | ID único de la factura | No |
| `userId` | String (UUID) | ID del usuario que realizó la compra | No |
| `cartId` | Long | ID del carrito asociado | No |
| `reservationId` | Long | ID de la reserva si aplicó descuento | **Sí** |
| `subtotal` | BigDecimal | Subtotal (igual a totalWithDiscount) | No |
| `totalWithDiscount` | BigDecimal | Total final pagado (con descuento aplicado) | No |
| `totalWithoutDiscount` | BigDecimal | Total original sin descuentos | No |
| `createdAt` | LocalDateTime (ISO 8601) | Fecha y hora de creación | No |
| `products` | Array | Lista de productos comprados | No |
| `products[].productId` | Long | ID del producto | No |
| `products[].name` | String | Nombre del producto | No |
| `products[].price` | BigDecimal | Precio unitario del producto | No |

### **Notas importantes:**
- Si `reservationId` es `null`, significa que NO se aplicó descuento por reserva
- `totalWithDiscount` es el monto real cobrado al usuario
- `totalWithoutDiscount - totalWithDiscount` = descuento aplicado
- El descuento máximo es de $25 (costo de la reserva)
- `userId` es un UUID en formato string

---

## 📤 Evento: Reserva Creada

### **Routing Key:** `reservation.created`
### **Exchange:** `reservation.event`

### **¿Cuándo se dispara?**
Cuando un usuario crea una nueva reserva en el comedor.

### **Estructura del Mensaje (JSON):**

```json
{
  "reservationId": 45,
  "userId": "00debe32-abd2-45a8-bece-3d3b752fa140",
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-12-10T12:00:00",
  "status": "ACTIVA",
  "cost": 25.00,
  "createdAt": "2025-12-09T20:00:00",
  "slotStartTime": "12:00:00",
  "slotEndTime": "13:00:00"
}
```

### **Campos del Evento:**

| Campo | Tipo | Descripción | Puede ser null |
|-------|------|-------------|----------------|
| `reservationId` | Long | ID único de la reserva | No |
| `userId` | String (UUID) | ID del usuario que reservó | No |
| `locationId` | Long | ID de la ubicación (1=Norte, 2=Sur) | No |
| `mealTime` | String | Tipo de comida: `DESAYUNO`, `ALMUERZO`, `MERIENDA`, `CENA` | No |
| `reservationTimeSlot` | String | Slot específico (ej: `ALMUERZO_SLOT_1`) | **Sí** |
| `reservationDate` | LocalDateTime (ISO 8601) | Fecha y hora exacta de la reserva | No |
| `status` | String | Estado: `ACTIVA`, `CONFIRMADA`, `CANCELADA`, `AUSENTE` | No |
| `cost` | BigDecimal | Costo de la reserva (siempre $25) | No |
| `createdAt` | LocalDateTime (ISO 8601) | Fecha y hora de creación | No |
| `slotStartTime` | LocalTime | Hora de inicio del slot (ej: `12:00:00`) | No |
| `slotEndTime` | LocalTime | Hora de fin del slot (ej: `13:00:00`) | No |

### **Valores posibles para `mealTime`:**
- `DESAYUNO` (7:00 - 12:00)
- `ALMUERZO` (12:00 - 16:00)
- `MERIENDA` (16:00 - 20:00)
- `CENA` (20:00 - 23:00)

### **Valores posibles para `status`:**
- `ACTIVA` - Reserva creada pero no confirmada
- `CONFIRMADA` - Reserva confirmada (cuando se confirma el carrito)
- `CANCELADA` - Reserva cancelada por el usuario
- `AUSENTE` - Usuario no asistió (marcada automáticamente)

### **Notas importantes:**
- `slotStartTime` y `slotEndTime` indican el rango horario exacto de la reserva
- El costo de reserva es siempre $25
- Una reserva puede dar descuento solo si se confirma con un carrito
- `userId` es un UUID en formato string

---

## 📥 Cómo Consumir los Eventos

### **Opción 1: Spring Boot con RabbitMQ**

```java
@Component
public class BillEventListener {
    
    @RabbitListener(queues = "tu-modulo.bill.queue")
    public void handleBillCreated(BillEventDTO event) {
        System.out.println("Nueva factura recibida: " + event.getBillId());
        System.out.println("Usuario: " + event.getUserId());
        System.out.println("Total: $" + event.getTotalWithDiscount());
        
        // Tu lógica aquí...
    }
}

@Component
public class ReservationEventListener {
    
    @RabbitListener(queues = "tu-modulo.reservation.queue")
    public void handleReservationCreated(ReservationEventDTO event) {
        System.out.println("Nueva reserva recibida: " + event.getReservationId());
        System.out.println("Usuario: " + event.getUserId());
        System.out.println("Fecha: " + event.getReservationDate());
        
        // Tu lógica aquí...
    }
}
```

### **Opción 2: Configuración Manual de Queue**

```java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue billQueue() {
        return new Queue("tu-modulo.bill.queue", true);
    }
    
    @Bean
    public Queue reservationQueue() {
        return new Queue("tu-modulo.reservation.queue", true);
    }
    
    @Bean
    public Binding billBinding() {
        return BindingBuilder
            .bind(billQueue())
            .to(new TopicExchange("bill.event"))
            .with("bill.created");
    }
    
    @Bean
    public Binding reservationBinding() {
        return BindingBuilder
            .bind(reservationQueue())
            .to(new TopicExchange("reservation.event"))
            .with("reservation.created");
    }
}
```

---

## 🔍 Ejemplo de Flujo Completo

### **Escenario: Usuario hace una compra CON reserva**

1. **Usuario crea reserva** → Evento `reservation.created` publicado
```json
{
  "reservationId": 10,
  "userId": "00debe32-abd2-45a8-bece-3d3b752fa140",
  "status": "ACTIVA",
  "cost": 25.00,
  ...
}
```

2. **Usuario crea carrito con productos y asocia la reserva**

3. **Usuario confirma el carrito** → Evento `bill.created` publicado
```json
{
  "billId": 5,
  "userId": "00debe32-abd2-45a8-bece-3d3b752fa140",
  "reservationId": 10,
  "totalWithoutDiscount": 150.00,
  "totalWithDiscount": 125.00,
  ...
}
```

**Resultado:** Usuario pagó $125 en lugar de $150 (descuento de $25 por la reserva)

---

### **Escenario: Usuario hace una compra SIN reserva**

1. **Usuario crea carrito con productos (sin reserva)**

2. **Usuario confirma el carrito** → Evento `bill.created` publicado
```json
{
  "billId": 6,
  "userId": "00debe32-abd2-45a8-bece-3d3b752fa140",
  "reservationId": null,
  "totalWithoutDiscount": 150.00,
  "totalWithDiscount": 150.00,
  ...
}
```

**Resultado:** Usuario pagó el precio completo ($150)

---

## 📋 DTOs Completos para tu Módulo

Si necesitas las clases completas en Java:

### **BillEventDTO.java**
```java
public class BillEventDTO {
    private Long billId;
    private String userId;
    private Long cartId;
    private Long reservationId;
    private BigDecimal subtotal;
    private BigDecimal totalWithDiscount;
    private BigDecimal totalWithoutDiscount;
    private LocalDateTime createdAt;
    private List<ProductEventDTO> products;
    
    // Getters, setters, constructors...
    
    public static class ProductEventDTO {
        private Long productId;
        private String name;
        private BigDecimal price;
        // Getters, setters...
    }
}
```

### **ReservationEventDTO.java**
```java
public class ReservationEventDTO {
    private Long reservationId;
    private String userId;
    private Long locationId;
    private String mealTime;
    private String reservationTimeSlot;
    private LocalDateTime reservationDate;
    private String status;
    private BigDecimal cost;
    private LocalDateTime createdAt;
    private LocalTime slotStartTime;
    private LocalTime slotEndTime;
    
    // Getters, setters, constructors...
}
```

---

## ❓ Preguntas Frecuentes

**P: ¿Los eventos se publican en transacción?**
R: No, se publican después de guardar en DB pero sin transacción. Si falla el evento, no afecta la creación de la factura/reserva.

**P: ¿Qué pasa si mi módulo no está escuchando?**
R: Los mensajes se quedan en la cola hasta que los consumas (si la queue es durable).

**P: ¿Puedo recibir el mismo evento dos veces?**
R: Sí, asegúrate de manejar idempotencia usando `billId` o `reservationId`.

**P: ¿El formato de fecha es compatible con todos los lenguajes?**
R: Sí, usamos ISO 8601: `2025-12-09T20:30:00`

**P: ¿Necesito crear los exchanges?**
R: No, ya existen en el servidor RabbitMQ. Solo necesitas crear tus queues y bindings.

---

## 📞 Contacto

Si necesitas más info o hay algún cambio en la estructura, avisame!

**Desarrollador:** [Tu nombre]
**Módulo:** Comedor
**Última actualización:** 9 de Diciembre 2025
