# Configuration Service

Microservicio de configuración disponible como imagen Docker.


## 📦 Descargar la imagen

```bash
docker pull angie14/configurationservice:latest
```

## 🚀 Ejecutar el contenedor

```bash
docker run -d -p 5004:5004 --name configurationservice angie14/configurationservice:latest
```

- El servicio estará disponible en `http://localhost:5004` después de iniciar el contenedor