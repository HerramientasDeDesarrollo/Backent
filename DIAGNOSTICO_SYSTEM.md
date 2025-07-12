# Sistema de Diagnóstico y Verificación de Resultados

## Resumen

Se ha implementado un sistema completo de diagnóstico para resolver problemas de estabilidad en el sistema de entrega de resultados. Este sistema permite:

1. **Detectar problemas** automáticamente
2. **Verificar integridad** de datos
3. **Generar reportes** de salud del sistema
4. **Intentar reparaciones** automáticas

## Nuevos Endpoints de Diagnóstico

### 1. Verificación Rápida de Postulación
```
GET /api/diagnostico/verificacion-rapida/{postulacionId}
```

**Propósito**: Verificación básica del estado de una postulación
**Acceso**: USUARIO (sus propias postulaciones) y EMPRESA
**Respuesta típica**:
```json
{
  "postulacion_id": 123,
  "postulacion_encontrada": true,
  "total_preguntas": 5,
  "total_evaluaciones": 5,
  "problemas_detectados": [],
  "estado": "SALUDABLE",
  "timestamp": "2025-07-11T18:30:00.000Z"
}
```

**Estados posibles**:
- `SALUDABLE`: Todo está funcionando correctamente
- `ADVERTENCIA`: Problemas menores detectados
- `CRITICO`: Problemas serios que requieren atención
- `ERROR`: Error durante la verificación

### 2. Diagnóstico Completo de Postulación
```
GET /api/diagnostico/postulacion/{postulacionId}
```

**Propósito**: Análisis detallado de todos los componentes de una postulación
**Acceso**: USUARIO (sus propias postulaciones) y EMPRESA
**Información incluida**:
- Estado de preguntas y evaluaciones
- Análisis de integridad de datos
- Problemas específicos detectados
- Sugerencias de reparación

### 3. Reporte de Salud del Sistema
```
GET /api/diagnostico/salud-sistema
```

**Propósito**: Vista general de la salud de todo el sistema
**Acceso**: Solo EMPRESA
**Información incluida**:
- Estadísticas generales
- Postulaciones problemáticas
- Porcentaje de salud general

### 4. Reparación Automática
```
POST /api/diagnostico/reparar/{postulacionId}
```

**Propósito**: Intenta reparar automáticamente problemas detectados
**Acceso**: Solo EMPRESA
**Acciones que puede realizar**:
- Limpiar datos corruptos
- Regenerar evaluaciones incompletas
- Corregir inconsistencias

### 5. Métricas de Calidad
```
GET /api/diagnostico/metricas-calidad
```

**Propósito**: Métricas clave sobre la calidad de los datos
**Acceso**: Solo EMPRESA

## Mejoras en el Sistema de Resultados

### Verificación Automática de Integridad

Ahora antes de entregar cualquier resultado, el sistema:

1. ✅ **Verifica que la postulación existe**
2. ✅ **Confirma que hay preguntas asociadas**
3. ✅ **Valida que existen evaluaciones**
4. ✅ **Revisa la integridad de los datos**
5. ✅ **Detecta inconsistencias automáticamente**

### Información de Diagnóstico en Respuestas

Todas las respuestas de resultados ahora incluyen una sección `verificacion` con:
```json
{
  "success": true,
  "verificacion": {
    "valida": true,
    "total_preguntas": 5,
    "total_evaluaciones": 5,
    "evaluaciones_incompletas": 0,
    "advertencias": []
  },
  // ... resto de los datos
}
```

## Cómo Usar el Sistema de Diagnóstico

### Para Diagnosticar Problemas de un Usuario

1. **Verificación Rápida** (para usuarios finales):
```bash
curl -H "Authorization: Bearer TOKEN" \
     "http://localhost:8080/api/diagnostico/verificacion-rapida/123"
```

2. **Diagnóstico Completo** (para análisis detallado):
```bash
curl -H "Authorization: Bearer TOKEN" \
     "http://localhost:8080/api/diagnostico/postulacion/123"
```

### Para Administradores del Sistema

1. **Revisar Salud General**:
```bash
curl -H "Authorization: Bearer EMPRESA_TOKEN" \
     "http://localhost:8080/api/diagnostico/salud-sistema"
```

2. **Obtener Métricas**:
```bash
curl -H "Authorization: Bearer EMPRESA_TOKEN" \
     "http://localhost:8080/api/diagnostico/metricas-calidad"
```

3. **Reparar Problemas**:
```bash
curl -X POST -H "Authorization: Bearer EMPRESA_TOKEN" \
     "http://localhost:8080/api/diagnostico/reparar/123"
```

## Problemas Comunes y Soluciones

### ❌ Problema: "No se encontraron evaluaciones"
**Diagnóstico**: La postulación tiene preguntas pero no evaluaciones
**Causa**: Falló el proceso de evaluación con IA
**Solución**: Re-ejecutar las evaluaciones

### ❌ Problema: "Evaluaciones con datos nulos"
**Diagnóstico**: Evaluaciones guardadas pero con campos vacíos
**Causa**: Error en el parsing de respuesta de IA
**Solución**: Regenerar evaluaciones afectadas

### ❌ Problema: "Número de preguntas y evaluaciones no coincide"
**Diagnóstico**: Proceso de evaluación incompleto
**Causa**: Interrupción durante evaluación
**Solución**: Evaluar preguntas faltantes

### ❌ Problema: "Evaluaciones sin JSON válido"
**Diagnóstico**: Campo evaluacionCompleta corrupto
**Causa**: Error al guardar respuesta de IA
**Solución**: Re-procesar evaluación

## Monitoreo Proactivo

### Métricas Clave a Vigilar

1. **Porcentaje de Salud del Sistema**: Debe estar > 95%
2. **Postulaciones Problemáticas**: Deben ser < 5% del total
3. **Evaluaciones Incompletas**: Deben ser 0

### Alertas Recomendadas

- Si porcentaje de salud < 90% → Investigar inmediatamente
- Si hay > 10 postulaciones problemáticas → Revisar proceso de evaluación
- Si aparecen errores recurrentes → Verificar conectividad con OpenAI

## Ejemplos de Uso en Producción

### 1. Usuario reporta "No veo mis resultados"

```bash
# Paso 1: Verificación rápida
curl GET /api/diagnostico/verificacion-rapida/123

# Si estado != "SALUDABLE":
# Paso 2: Diagnóstico detallado
curl GET /api/diagnostico/postulacion/123

# Paso 3: Mostrar problemas específicos al usuario
# Paso 4: Si es empresa, intentar reparación
curl POST /api/diagnostico/reparar/123
```

### 2. Monitoreo Diario del Sistema

```bash
# Cada mañana, revisar salud general
curl GET /api/diagnostico/salud-sistema

# Si hay problemas, obtener detalles
curl GET /api/diagnostico/metricas-calidad

# Reparar postulaciones problemáticas encontradas
for id in problematic_ids; do
  curl POST /api/diagnostico/reparar/$id
done
```

### 3. Antes de Presentaciones Importantes

```bash
# Verificar que todo esté funcionando
curl GET /api/diagnostico/salud-sistema

# Confirmar métricas de calidad
curl GET /api/diagnostico/metricas-calidad

# Si porcentaje_salud < 95%, investigar y reparar
```

## Logging Mejorado

El sistema ahora registra automáticamente:

- ✅ Cada verificación de integridad realizada
- ✅ Problemas detectados automáticamente
- ✅ Reparaciones intentadas y su resultado
- ✅ Métricas de rendimiento de consultas

Los logs incluyen contexto detallado para facilitar el debugging.

## Próximos Pasos

1. **Dashboard de Monitoreo**: Interface visual para métricas
2. **Alertas Automáticas**: Notificaciones cuando se detecten problemas
3. **Reparación Automática**: Más tipos de reparaciones automáticas
4. **Análisis Predictivo**: Detectar problemas antes de que ocurran
5. **Cache Inteligente**: Reducir carga en consultas frecuentes

## Compatibilidad

✅ **Totalmente compatible** con el sistema existente
- No se modificaron los endpoints originales
- Los usuarios no necesitan cambiar nada
- El sistema de diagnóstico es completamente adicional
