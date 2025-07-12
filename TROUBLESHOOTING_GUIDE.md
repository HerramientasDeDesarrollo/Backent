# Guía de Resolución de Problemas - Sistema de Resultados

## Problema: "No se generan bien los resultados de /mis-resultados"

### 🔍 Diagnóstico Paso a Paso

#### 1. Verificación Rápida
Antes de intentar ver los resultados, verifica el estado:

```bash
GET /api/evaluaciones/verificar-estado/{postulacionId}
```

**Respuestas posibles:**

✅ **Estado: DISPONIBLE**
```json
{
  "estado": "DISPONIBLE",
  "puede_mostrar_resultados": true,
  "detalles": {
    "valida": true,
    "total_preguntas": 5,
    "total_evaluaciones": 5,
    "evaluaciones_incompletas": 0,
    "advertencias": []
  }
}
```
→ Los resultados deben funcionar correctamente.

❌ **Estado: PROBLEMAS_DETECTADOS**
```json
{
  "estado": "PROBLEMAS_DETECTADOS",
  "puede_mostrar_resultados": false,
  "detalles": {
    "valida": false,
    "total_preguntas": 5,
    "total_evaluaciones": 3,
    "evaluaciones_incompletas": 1,
    "advertencias": [
      "Número de preguntas y evaluaciones no coincide",
      "Hay 1 evaluaciones con datos incompletos"
    ]
  }
}
```
→ Hay problemas que necesitan reparación.

#### 2. Diagnóstico Detallado
Si hay problemas, obtén más información:

```bash
GET /api/diagnostico/postulacion/{postulacionId}
```

Esta respuesta te dará:
- Análisis detallado de cada pregunta
- Estado de cada evaluación
- Problemas específicos detectados
- Sugerencias de reparación

#### 3. Intentar Reparación (Solo Empresas)
Si tienes permisos de empresa:

```bash
POST /api/diagnostico/reparar/{postulacionId}
```

## Problemas Específicos y Soluciones

### 🚨 Problema: "No hay evaluaciones registradas"

**Síntomas:**
- `/mis-resultados/{id}` retorna mensaje "No se encontraron evaluaciones"
- `total_evaluaciones: 0` en verificación

**Causas posibles:**
1. El proceso de evaluación nunca se ejecutó
2. Falló la conexión con OpenAI
3. Error al guardar las evaluaciones

**Solución:**
1. Verificar que las preguntas existen
2. Re-ejecutar el proceso de evaluación para cada pregunta
3. Verificar logs de errores en EvaluacionService

### 🚨 Problema: "Evaluaciones con datos incompletos"

**Síntomas:**
- `evaluaciones_incompletas > 0` en verificación
- Resultados muestran valores nulos o 0

**Causas posibles:**
1. Error al parsear respuesta de OpenAI
2. Respuesta de IA en formato incorrecto
3. Interrupción durante el guardado

**Solución:**
1. Usar endpoint de reparación automática
2. Verificar formato de respuesta de OpenAI
3. Re-evaluar preguntas específicas

### 🚨 Problema: "Número de preguntas y evaluaciones no coincide"

**Síntomas:**
- `total_preguntas != total_evaluaciones`
- Algunos resultados faltan

**Causas posibles:**
1. Proceso de evaluación interrumpido
2. Error al procesar algunas preguntas
3. Problemas de concurrencia

**Solución:**
1. Identificar preguntas sin evaluación
2. Evaluar solo las preguntas faltantes
3. Verificar integridad de datos

### 🚨 Problema: "Evaluaciones sin JSON válido"

**Síntomas:**
- Campo `evaluacionCompleta` nulo o vacío
- No se pueden extraer fortalezas/oportunidades

**Causas posibles:**
1. Error al guardar respuesta de OpenAI
2. Respuesta de IA truncada
3. Problemas de codificación de caracteres

**Solución:**
1. Re-ejecutar evaluación para esas preguntas
2. Verificar logs de EvaluacionService
3. Validar respuesta de OpenAI antes de guardar

## Flujo de Trabajo Recomendado

### Para Usuarios Finales:

1. **Antes de mostrar resultados:**
```javascript
// En tu frontend
const response = await fetch(`/api/evaluaciones/verificar-estado/${postulacionId}`);
const estado = await response.json();

if (estado.puede_mostrar_resultados) {
    // Mostrar resultados normalmente
    fetchResultados();
} else {
    // Mostrar mensaje de error con detalles
    mostrarMensajeError(estado.detalles.advertencias);
}
```

2. **Si hay problemas, informar al usuario:**
```javascript
// Mensaje amigable al usuario
"Estamos procesando tus resultados. Por favor intenta nuevamente en unos minutos."
```

### Para Administradores/Empresas:

1. **Monitoreo Diario:**
```bash
# Verificar salud general del sistema
curl GET /api/diagnostico/salud-sistema

# Si porcentaje_salud < 95%, investigar
curl GET /api/diagnostico/metricas-calidad
```

2. **Resolución de Problemas Reportados:**
```bash
# Paso 1: Diagnóstico
curl GET /api/diagnostico/postulacion/{id}

# Paso 2: Reparación automática
curl POST /api/diagnostico/reparar/{id}

# Paso 3: Verificar reparación
curl GET /api/evaluaciones/verificar-estado/{id}
```

3. **Análisis de Tendencias:**
```bash
# Revisar postulaciones problemáticas recurrentes
curl GET /api/diagnostico/salud-sistema | jq '.postulaciones_problematicas'
```

## Códigos de Error y Significados

| Estado | Significado | Acción Requerida |
|--------|-------------|------------------|
| `DISPONIBLE` | Todo funcionando correctamente | Ninguna |
| `PROBLEMAS_DETECTADOS` | Hay problemas pero no críticos | Revisar y posiblemente reparar |
| `ERROR` | Error grave del sistema | Investigación técnica requerida |
| `SALUDABLE` | Sistema operando normalmente | Ninguna |
| `ADVERTENCIA` | Problemas menores detectados | Monitoreo continuo |
| `CRITICO` | Problemas serios que requieren atención inmediata | Reparación urgente |

## Logs para Investigación

Buscar en logs por estos patrones:

```
# Errores de evaluación
grep "Error al evaluar respuesta" application.log

# Problemas de integridad
grep "Problemas de integridad detectados" application.log

# Errores de OpenAI
grep "Error al llamar a la API de OpenAI" application.log

# Reparaciones automáticas
grep "Reparación completada" application.log
```

## Prevención de Problemas

### 1. Validación Previa
Siempre usar el endpoint de verificación antes de mostrar resultados:
```bash
GET /api/evaluaciones/verificar-estado/{postulacionId}
```

### 2. Monitoreo Proactivo
Configurar alertas para:
- Porcentaje de salud del sistema < 90%
- Más de 10 postulaciones problemáticas
- Errores recurrentes en evaluaciones

### 3. Limpieza Regular
Ejecutar diagnósticos semanales:
```bash
GET /api/diagnostico/salud-sistema
```

## Contacto y Escalación

Si los problemas persisten después de seguir esta guía:

1. **Revisar logs detallados** del sistema
2. **Ejecutar diagnóstico completo** de la postulación problemática
3. **Verificar conectividad** con servicios externos (OpenAI)
4. **Consultar métricas de rendimiento** del sistema

El sistema ahora es mucho más **robusto** y **auto-diagnosticable**. La mayoría de problemas se pueden identificar y resolver automáticamente.
