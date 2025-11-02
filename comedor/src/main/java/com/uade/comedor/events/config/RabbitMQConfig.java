package com.uade.comedor.events.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configuración de RabbitMQ para el sistema de eventos del comedor.
 * 
 * ¿Qué es cada componente?
 * 
 * 1. EXCHANGE (Intercambiador): Es como una "oficina de correos" que recibe mensajes
 *    y decide a qué cola(s) enviarlos según el routing key.
 *    
 * 2. QUEUE (Cola): Es donde se almacenan los mensajes esperando ser procesados.
 *    Otros servicios pueden "suscribirse" a estas colas para recibir los eventos.
 *    
 * 3. BINDING (Enlace): Conecta un exchange con una queue usando un routing key.
 *    Es como decir: "si el mensaje tiene este routing key, envíalo a esta cola"
 *    
 * 4. ROUTING KEY: Es una "etiqueta" que lleva cada mensaje para que el exchange
 *    sepa a qué cola(s) enviarlo.
 * 
 * En este sistema:
 * - Usamos un TOPIC EXCHANGE porque permite patrones de routing flexibles
 * - Cada tipo de evento tiene su propia queue
 * - Los routing keys siguen el patrón: "modulo.entidad.accion"
 *   Ej: "comedor.reservation.updated", "comedor.bill.created"
 */
@Configuration
public class RabbitMQConfig {
    
    // Nombre del exchange principal del comedor
    public static final String COMEDOR_EXCHANGE = "comedor.events";
    
    // Nombres de las colas
    public static final String RESERVATION_UPDATED_QUEUE = "comedor.reservation.updated";
    public static final String BILL_CREATED_QUEUE = "comedor.bill.created";
    
    // Routing keys (etiquetas de los mensajes)
    public static final String RESERVATION_UPDATED_ROUTING_KEY = "comedor.reservation.updated";
    public static final String BILL_CREATED_ROUTING_KEY = "comedor.bill.created";
    
    /**
     * Define el Exchange principal tipo TOPIC.
     * 
     * Topic Exchange permite routing patterns como:
     * - "comedor.reservation.*" (cualquier acción sobre reservas)
     * - "comedor.*" (cualquier evento del comedor)
     * - "*.reservation.*" (cualquier acción sobre reservas de cualquier módulo)
     */
    @Bean
    public TopicExchange comedorExchange() {
        return new TopicExchange(COMEDOR_EXCHANGE);
    }
    
    /**
     * Cola para eventos de reservas actualizadas.
     * 
     * durable(true) = la cola sobrevive a reinicios del servidor RabbitMQ
     */
    @Bean
    public Queue reservationUpdatedQueue() {
        return new Queue(RESERVATION_UPDATED_QUEUE, true);
    }
    
    /**
     * Cola para eventos de facturas creadas.
     */
    @Bean
    public Queue billCreatedQueue() {
        return new Queue(BILL_CREATED_QUEUE, true);
    }
    
    /**
     * Enlaza la cola de reservas con el exchange.
     * 
     * Esto dice: "Todos los mensajes con routing key 'comedor.reservation.updated'
     * que lleguen al exchange 'comedor.events' deben ir a la cola 'reservation.updated'"
     */
    @Bean
    public Binding reservationUpdatedBinding(Queue reservationUpdatedQueue, TopicExchange comedorExchange) {
        return BindingBuilder
            .bind(reservationUpdatedQueue)
            .to(comedorExchange)
            .with(RESERVATION_UPDATED_ROUTING_KEY);
    }
    
    /**
     * Enlaza la cola de facturas con el exchange.
     */
    @Bean
    public Binding billCreatedBinding(Queue billCreatedQueue, TopicExchange comedorExchange) {
        return BindingBuilder
            .bind(billCreatedQueue)
            .to(comedorExchange)
            .with(BILL_CREATED_ROUTING_KEY);
    }
    
    /**
     * ObjectMapper configurado para manejar fechas Java 8+ (LocalDateTime, etc.)
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Desactivar escritura de fechas como timestamps
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    /**
     * Convertidor de mensajes JSON usando Jackson.
     * Permite serializar/deserializar objetos Java a/desde JSON.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    /**
     * Configura el RabbitTemplate para enviar mensajes.
     * 
     * El MessageConverter JSON permite enviar objetos Java directamente
     * sin tener que convertirlos manualmente a String.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
