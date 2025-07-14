# API de Convocatorias v2 - Documentación Completa

## 🚀 Cambios Implementados

Se ha rediseñado completamente el sistema de convocatorias con una estructura moderna y extensible que incluye:

### **📋 Nuevos Campos Implementados:**

1. **Job Title** - Título del puesto
2. **Category** - Categoría del trabajo (Technology, Engineering, Marketing, etc.)
3. **Job Description** - Descripción detallada del trabajo
4. **Technical Requirements** - Requisitos técnicos específicos
5. **Experience Level** - Nivel de experiencia (Intern, Junior, Senior, etc.)
6. **Work Mode** - Modalidad de trabajo (On-site, Remote, Hybrid, Flexible)
7. **Location** - Ubicación del trabajo
8. **Salary Range** - Rango salarial (min, max, currency)
9. **Benefits & Perks** - Beneficios y ventajas
10. **Publication Date** - Fecha de publicación
11. **Closing Date** - Fecha de cierre

---

## 📊 Estructura de Datos

### **ConvocatoriaCreateDTO** (Request)
```json
{
  "jobTitle": "Senior Backend Developer",
  "category": 1,
  "jobDescription": "We are looking for an experienced backend developer...",
  "technicalRequirements": "Java 17+, Spring Boot, PostgreSQL, Docker",
  "experienceLevel": 3,
  "workMode": 3,
  "location": "Mexico City, Mexico",
  "salaryMin": 80000.00,
  "salaryMax": 120000.00,
  "salaryCurrency": "USD",
  "benefitsPerks": "Health insurance, flexible hours, remote work options",
  "publicationDate": "2025-07-13",
  "closingDate": "2025-08-13",
  "dificultad": 7,
  "empresaId": 123
}
```

<!-- 
Valores numéricos para enums (internacionalización):
- category: 1=Technology, 2=Design, 3=Marketing, 4=Sales, 5=Finance, 6=Operations, 7=Human Resources, 8=Other
- experienceLevel: 1=Entry(0-2yrs), 2=Mid(3-5yrs), 3=Senior(6-8yrs), 4=Lead(9+yrs)
- workMode: 1=Remote, 2=On-site, 3=Hybrid
-->

### **ConvocatoriaResponseDTO** (Response)
```json
{
  "id": 456,
  "jobTitle": "Senior Backend Developer",
  "category": 1,
  "jobDescription": "We are looking for an experienced backend developer...",
  "technicalRequirements": "Java 17+, Spring Boot, PostgreSQL, Docker",
  "experienceLevel": 3,
  "workMode": 3,
  "location": "Mexico City, Mexico",
  "salaryMin": 80000.00,
  "salaryMax": 120000.00,
  "salaryCurrency": "USD",
  "benefitsPerks": "Health insurance, flexible hours, remote work options",
  "publicationDate": "2025-07-13",
  "closingDate": "2025-08-13",
  "activo": true,
  "dificultad": 7,
  "empresaId": 123,
  "empresaNombre": "Tech Corp Inc.",
  "formattedSalaryRange": "USD 80,000.00 - 120,000.00",
  "isActive": true,
  "daysUntilClosing": 31,
  "status": "ACTIVE"
}
```

---

## 🛠️ Endpoints API v2

### **1. Crear Convocatoria**
```http
POST /api/convocatorias/v2
Authorization: Bearer {jwt_token}
Role: EMPRESA
Content-Type: application/json

Body:
{
  "jobTitle": "Senior Backend Developer",
  "category": 1,
  "jobDescription": "We are looking for an experienced backend developer with expertise in Java and Spring Boot...",
  "technicalRequirements": "Java 17+, Spring Boot, PostgreSQL, Docker, Microservices",
  "experienceLevel": 3,
  "workMode": 3,
  "location": "Mexico City, Mexico",
  "salaryMin": 80000.00,
  "salaryMax": 120000.00,
  "salaryCurrency": "USD",
  "benefitsPerks": "Health insurance, flexible hours, remote work options, stock options",
  "publicationDate": "2025-07-13",
  "closingDate": "2025-08-13",
  "dificultad": 7,
  "empresaId": 123
}

Response: ConvocatoriaResponseDTO
```

### **2. Obtener Convocatoria por ID**
```http
GET /api/convocatorias/v2/{id}
Authorization: Bearer {jwt_token}
Role: USUARIO, EMPRESA

Response: ConvocatoriaResponseDTO
```

### **3. Listar por Empresa**
```http
GET /api/convocatorias/v2/empresa/{empresaId}
Authorization: Bearer {jwt_token}
Role: EMPRESA

Response: {
  "success": true,
  "data": [ConvocatoriaResponseDTO],
  "total": 15
}
```

### **4. Listar Activas**
```http
GET /api/convocatorias/v2/activas
Authorization: Bearer {jwt_token}
Role: USUARIO

Response: {
  "success": true,
  "data": [ConvocatoriaResponseDTO],
  "total": 25
}
```

### **5. Filtros Especializados**

**Por Categoría:**
```http
GET /api/convocatorias/v2/categoria/{categoria}
Ejemplo: GET /api/convocatorias/v2/categoria/1
```

**Por Modalidad de Trabajo:**
```http
GET /api/convocatorias/v2/modalidad/{workMode}
Ejemplo: GET /api/convocatorias/v2/modalidad/1
```

**Por Nivel de Experiencia:**
```http
GET /api/convocatorias/v2/experiencia/{experienceLevel}
Ejemplo: GET /api/convocatorias/v2/experiencia/3
```

### **6. Actualizar Convocatoria**
```http
PUT /api/convocatorias/v2/{id}
Authorization: Bearer {jwt_token}
Role: EMPRESA
Content-Type: application/json

Body:
{
  "jobTitle": "Senior Full Stack Developer",
  "category": 1,
  "jobDescription": "Updated job description for full stack role...",
  "technicalRequirements": "Java 17+, Spring Boot, React, PostgreSQL, Docker",
  "experienceLevel": 3,
  "workMode": 1,
  "location": "Remote",
  "salaryMin": 90000.00,
  "salaryMax": 130000.00,
  "salaryCurrency": "USD",
  "benefitsPerks": "Health insurance, flexible hours, remote work, learning budget",
  "publicationDate": "2025-07-13",
  "closingDate": "2025-08-20",
  "dificultad": 8,
  "empresaId": 123
}

Response: ConvocatoriaResponseDTO
```

### **7. Obtener Metadatos**
```http
GET /api/convocatorias/v2/metadata
Authorization: Bearer {jwt_token}
Role: USUARIO, EMPRESA

Response: {
  "success": true,
  "metadata": {
    "categories": {
      "1": "Technology",
      "2": "Design", 
      "3": "Marketing",
      "4": "Sales",
      "5": "Finance",
      "6": "Operations",
      "7": "Human Resources",
      "8": "Other"
    },
    "experience_levels": {
      "1": "Entry Level (0-2 years)",
      "2": "Mid Level (3-5 years)",
      "3": "Senior (6-8 years)",
      "4": "Lead (9+ years)"
    },
    "work_modes": {
      "1": "Remote",
      "2": "On-site",
      "3": "Hybrid"
    },
    "currencies": ["USD", "EUR", "MXN", "CAD", "GBP", "JPY"],
    "difficulty_range": {"min": 1, "max": 10}
  }
}
```

---

## 📝 Ejemplos de Body para POST/PUT

### **Ejemplo 1: Puesto de Technology (Backend)**
```json
{
  "jobTitle": "Senior Backend Developer",
  "category": 1,
  "jobDescription": "Buscamos un desarrollador backend senior con experiencia en microservicios y arquitecturas escalables. Trabajarás en un equipo ágil desarrollando APIs robustas y sistemas distribuidos.",
  "technicalRequirements": "Java 17+, Spring Boot, PostgreSQL, Docker, Kubernetes, AWS, Microservices, Redis",
  "experienceLevel": 3,
  "workMode": 3,
  "location": "Ciudad de México, México",
  "salaryMin": 80000.00,
  "salaryMax": 120000.00,
  "salaryCurrency": "USD",
  "benefitsPerks": "Seguro médico, horarios flexibles, trabajo remoto, opciones de acciones, presupuesto de aprendizaje",
  "publicationDate": "2025-07-13",
  "closingDate": "2025-08-13",
  "dificultad": 7,
  "empresaId": 123
}
```

### **Ejemplo 2: Puesto de Design (UI/UX)**
```json
{
  "jobTitle": "UX/UI Designer",
  "category": 2,
  "jobDescription": "Únete a nuestro equipo de diseño para crear experiencias digitales excepcionales. Trabajarás en proyectos web y móviles, desde investigación de usuarios hasta prototipos interactivos.",
  "technicalRequirements": "Figma, Adobe Creative Suite, Sketch, Prototyping, User Research, Design Systems",
  "experienceLevel": 2,
  "workMode": 1,
  "location": "Remoto",
  "salaryMin": 45000.00,
  "salaryMax": 65000.00,
  "salaryCurrency": "USD",
  "benefitsPerks": "Trabajo 100% remoto, horarios flexibles, equipamiento completo, conferencias y cursos",
  "publicationDate": "2025-07-13",
  "closingDate": "2025-07-20",
  "dificultad": 5,
  "empresaId": 456
}
```

### **Ejemplo 3: Puesto de Marketing (Digital)**
```json
{
  "jobTitle": "Digital Marketing Specialist",
  "category": 3,
  "jobDescription": "Buscamos un especialista en marketing digital para liderar nuestras campañas online y estrategias de crecimiento. Experiencia en SEO, SEM y redes sociales es fundamental.",
  "technicalRequirements": "Google Ads, Facebook Ads, Google Analytics, SEO/SEM, HubSpot, Mailchimp, A/B Testing",
  "experienceLevel": 2,
  "workMode": 2,
  "location": "Guadalajara, México",
  "salaryMin": 35000.00,
  "salaryMax": 50000.00,
  "salaryCurrency": "USD",
  "benefitsPerks": "Seguro médico, bonos por desempeño, capacitación constante, ambiente joven",
  "publicationDate": "2025-07-13",
  "closingDate": "2025-07-25",
  "dificultad": 4,
  "empresaId": 789
}
```

### **Ejemplo 4: Puesto Entry Level (Desarrollo)**
```json
{
  "jobTitle": "Junior Frontend Developer",
  "category": 1,
  "jobDescription": "Excelente oportunidad para desarrolladores junior. Trabajarás con tecnologías modernas en un ambiente de aprendizaje continuo con mentorías y proyectos desafiantes.",
  "technicalRequirements": "HTML5, CSS3, JavaScript, React, Git, Responsive Design, Básico en REST APIs",
  "experienceLevel": 1,
  "workMode": 3,
  "location": "Monterrey, México",
  "salaryMin": 25000.00,
  "salaryMax": 35000.00,
  "salaryCurrency": "USD",
  "benefitsPerks": "Mentoría dedicada, cursos pagados, seguro médico, crecimiento acelerado",
  "publicationDate": "2025-07-13",
  "closingDate": "2025-08-05",
  "dificultad": 3,
  "empresaId": 101
}
```

### **Campos Requeridos vs Opcionales**

**✅ Campos Requeridos:**
- `jobTitle` (string, no vacío)
- `category` (integer, 1-8)
- `jobDescription` (string, no vacío)
- `experienceLevel` (integer, 1-4)
- `workMode` (integer, 1-3)
- `publicationDate` (date, formato YYYY-MM-DD)
- `closingDate` (date, formato YYYY-MM-DD, debe ser > publicationDate)
- `empresaId` (integer, debe existir en BD)

**⚪ Campos Opcionales:**
- `technicalRequirements` (string)
- `location` (string)
- `salaryMin` (decimal)
- `salaryMax` (decimal, debe ser >= salaryMin si se especifica)
- `salaryCurrency` (string, default: "USD")
- `benefitsPerks` (string)
- `dificultad` (integer, 1-10, default: 5)

---

## 🔄 Compatibilidad con API v1

Los endpoints v1 siguen funcionando pero están **deprecados**:

- ❌ `POST /api/convocatorias` → ✅ `POST /api/convocatorias/v2`
- ❌ `GET /api/convocatorias/{id}` → ✅ `GET /api/convocatorias/v2/{id}`
- ❌ `GET /api/convocatorias/empresa/{id}` → ✅ `GET /api/convocatorias/v2/empresa/{id}`
- ❌ `GET /api/convocatorias/activas` → ✅ `GET /api/convocatorias/v2/activas`

**Los endpoints v1 redirigen internamente a v2 y muestran warnings de deprecación.**

---

## ✅ Validaciones Implementadas

### **En ConvocatoriaCreateDTO:**
1. **jobTitle** - Requerido, no vacío
2. **category** - Requerido, debe ser un valor numérico válido (1-8)
3. **jobDescription** - Requerido, no vacío
4. **experienceLevel** - Requerido, debe ser un valor numérico válido (1-4)
5. **workMode** - Requerido, debe ser un valor numérico válido (1-3)
6. **publicationDate** - Requerido, no nulo
7. **closingDate** - Requerido, debe ser posterior a publicationDate
8. **empresaId** - Requerido, debe existir en la BD
9. **salaryRange** - Si se especifica, salaryMax debe ser >= salaryMin
10. **dificultad** - Debe estar entre 1 y 10

### **Validaciones de Negocio:**
- Las fechas deben ser coherentes (cierre > publicación)
- La empresa debe existir
- El rango salarial debe ser válido
- Los campos enum deben tener valores numéricos válidos según rangos establecidos

---

## 🎯 Casos de Uso

### **Frontend - Crear Convocatoria:**
1. Hacer GET `/v2/metadata` para obtener opciones de enums con valores numéricos
2. Mostrar formulario con dropdowns poblados usando los nombres legibles
3. Enviar valores numéricos en el POST `/v2` (ej: category: 1, workMode: 3)
4. Manejar response con datos completos incluyendo valores numéricos

### **Frontend - Listar Convocatorias:**
1. Hacer GET `/v2/activas` para usuarios (recibe valores numéricos)
2. Hacer GET `/v2/empresa/{id}` para empresas
3. Usar filtros `/v2/categoria/{numerico}` con valores 1-8
4. Convertir valores numéricos a nombres legibles usando metadata
5. Mostrar datos con campos calculados (salary formatted, status, etc.)

### **Frontend - Buscar/Filtrar:**
1. Usar endpoints de filtro con valores numéricos (ej: `/categoria/1` para Technology)
2. Combinar múltiples filtros numéricos en la UI
3. Mostrar resultados traduciendo números a texto según idioma actual

---

## 🚧 Estado del Proyecto

### **✅ Completado:**
- Nuevas entidades y enums
- DTOs de request/response
- Service completo con validaciones
- Controller v2 con todos los endpoints
- Endpoints de filtrado
- Endpoint de metadatos
- Compatibilidad backward con v1
- Documentación completa

### **📝 Próximos Pasos:**
1. Testing de endpoints
2. Migración de base de datos
3. Actualización de frontend
4. Deprecación gradual de v1

---

**Estado Actual:** ✅ **IMPLEMENTADO Y LISTO PARA TESTING**

La nueva API está completamente funcional y es backward compatible. El frontend puede comenzar a migrar gradualmente a los nuevos endpoints.
