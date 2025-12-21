# μPACS Docker Deployment Guide

## Quick Start

### Using Docker Compose (Recommended)

1. **Build the JAR file:**
```bash
./gradlew clean build
```

2. **Start the container:**
```bash
docker-compose up -d
```

3. **Access the application:**
   - Web Interface: http://localhost:8080
   - DICOM Service: localhost:8104

4. **Stop the container:**
```bash
docker-compose down
```

### Using Dockerfile

1. **Build the JAR file:**
```bash
./gradlew clean build
```

2. **Build the Docker image:**
```bash
docker build -t famst/micropacs:0.0.1-SNAPSHOT .
```

3. **Run the container:**
```bash
docker run -d \
  --name mupacs \
  -p 8080:8080 \
  -p 8104:8104 \
  -v mupacs-data:/app/data \
  -v mupacs-archive:/app/archive \
  -v mupacs-import:/app/import \
  -v mupacs-logs:/app/log \
  famst/micropacs:0.0.1-SNAPSHOT
```

### Using Spring Boot Buildpacks

1. **Build the image using Gradle:**
```bash
./gradlew bootBuildImage
```

2. **Run the container:**
```bash
docker run -d \
  --name mupacs \
  -p 8080:8080 \
  -p 8104:8104 \
  -v mupacs-data:/app/data \
  -v mupacs-archive:/app/archive \
  -v mupacs-import:/app/import \
  -v mupacs-logs:/app/log \
  famst/micropacs:0.0.1-SNAPSHOT
```

## Exposed Ports

- **8080**: HTTP port for the web interface and REST API
- **8104**: DICOM C-STORE SCP port for receiving DICOM images

## Volumes

The following volumes are used to persist data:

- `/app/data`: Application database and configuration
- `/app/archive`: DICOM image archive storage
- `/app/import`: Directory for importing DICOM files
- `/app/log`: Application logs

## Environment Variables

You can customize the application behavior using environment variables:

```bash
docker run -d \
  --name mupacs \
  -p 8080:8080 \
  -p 8104:8104 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e JAVA_OPTS="-Xmx1g -Xms512m" \
  famst/micropacs:0.0.1-SNAPSHOT
```

Common environment variables:
- `SPRING_PROFILES_ACTIVE`: Spring profile to activate (default, production, etc.)
- `JAVA_OPTS`: JVM options for memory configuration
- `SERVER_PORT`: Override HTTP port (default: 8080)
- `DICOM_PORT`: Override DICOM port (default: 8104)

## Health Check

Check if the application is running:

```bash
curl http://localhost:8080/actuator/health
```

## Viewing Logs

```bash
# Using Docker
docker logs mupacs

# Follow logs in real-time
docker logs -f mupacs

# Using Docker Compose
docker-compose logs -f
```

## Stopping and Removing

```bash
# Stop the container
docker stop mupacs

# Remove the container
docker rm mupacs

# Remove the image
docker rmi famst/micropacs:0.0.1-SNAPSHOT
```

## Troubleshooting

### Port already in use

If port 8080 or 8104 is already in use, you can map to different host ports:

```bash
docker run -d \
  --name mupacs \
  -p 9090:8080 \
  -p 11112:8104 \
  famst/micropacs:0.0.1-SNAPSHOT
```

### Check running containers

```bash
docker ps
```

### Access container shell

```bash
docker exec -it mupacs /bin/bash
```

## Production Deployment

For production deployments, consider:

1. **Use specific version tags** instead of SNAPSHOT versions
2. **Configure persistent volumes** with backup strategies
3. **Set resource limits**:
```bash
docker run -d \
  --name mupacs \
  -p 8080:8080 \
  -p 8104:8104 \
  --memory="1g" \
  --cpus="2" \
  famst/micropacs:0.0.1-SNAPSHOT
```

4. **Use docker-compose for orchestration**
5. **Set up monitoring and logging**
6. **Configure reverse proxy** (nginx, traefik) for HTTPS

## Network Configuration

To connect multiple containers or use with other services:

```bash
# Create a network
docker network create mupacs-net

# Run container on the network
docker run -d \
  --name mupacs \
  --network mupacs-net \
  -p 8080:8080 \
  -p 8104:8104 \
  famst/micropacs:0.0.1-SNAPSHOT
```

