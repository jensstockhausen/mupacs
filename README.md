# MuPACS - Minimal PACS System

**MuPACS** (Minimal Picture Archiving and Communication System) is a lightweight, Java-based DICOM archive system designed for storing, managing, and retrieving medical imaging data.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![dcm4che](https://img.shields.io/badge/dcm4che-5.34.1-blue.svg)](https://www.dcm4che.org/)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [DICOM Services](#dicom-services)
- [Web Interface](#web-interface)
- [Testing](#testing)

---

## 🎯 Overview

MuPACS provides a complete DICOM server implementation with the following capabilities:
- **DICOM C-STORE SCP**: Receive and store DICOM images from modalities
- **DICOM C-ECHO SCP/SCU**: Verify DICOM connectivity
- **DICOM C-FIND SCP**: Query stored images (Patient, Study, Series levels)
- **Folder Import**: Import DICOM files from local filesystem
- **Web-based Management**: Browse and manage stored images through a modern web interface
- **Embedded Database**: H2 database for metadata storage with file-based persistence

The system follows the DICOM hierarchy: **Patient → Study → Series → Instance**

---

## ✨ Key Features

### DICOM Networking
- **C-STORE SCP**: Automatic reception and archival of DICOM images
- **C-ECHO SCP/SCU**: Connectivity verification with remote DICOM nodes
- **C-FIND SCP**: Query/Retrieve support at Patient, Study, and Series levels
- **Application Entity (AET) Management**: Configure and test remote DICOM nodes

### Data Management
- **Hierarchical Storage**: Organized file system structure by StudyInstanceUID/SeriesInstanceUID
- **Metadata Database**: Fast querying using H2 embedded database
- **Unique Constraint Validation**: Patient ID as unique identifier (DICOM standard compliant)
- **Data Integrity**: Ensures uniqueness across all DICOM UIDs
- **Folder Import**: Batch import from filesystem with recursive directory scanning

### Web Interface
- **Patient List**: Paginated view of all patients with expandable study details
- **Hierarchical View**: All patient data (demographics, studies, series, instances) in one page
- **Modular Templates**: Reusable Thymeleaf fragments for maintainable UI
- **DICOM Configuration**: Manage server settings and remote AETs
- **Import Management**: Trigger and monitor folder imports
- **Log Viewer**: Real-time log monitoring with auto-refresh
- **Responsive Design**: Works on desktop and mobile browsers

### Technical Features
- **Spring Boot Framework**: Modern Java application framework
- **Thymeleaf Templates**: Server-side rendering with reusable fragments
- **REST API**: RESTful endpoints for programmatic access
- **Async Processing**: Non-blocking import and DICOM operations
- **Transaction Management**: ACID compliance for database operations
- **Property Dump**: Automatic configuration verification on startup
- **Error Handling**: Enhanced error messages for common issues (SSL/TLS, connectivity)
- **Comprehensive Testing**: Unit and integration tests with >80% coverage

---

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 4.0.0
- **DICOM Toolkit**: dcm4che 5.34.1
- **Database**: H2 (embedded, file-based)
- **Web Framework**: Spring MVC + Thymeleaf
- **Build Tool**: Gradle (Kotlin DSL)
- **Java Version**: 17

### Project Structure
```
mupacs/
├── src/main/java/de/famst/
│   ├── controller/          # Web controllers and REST services
│   ├── data/               # JPA entities and repositories
│   ├── dcm/                # DICOM service implementations
│   ├── service/            # Business logic and services
│   ├── config/             # Spring configuration classes
│   ├── exception/          # Global exception handlers
│   └── MuPACSApplication.java
├── src/main/resources/
│   ├── application.properties
│   ├── logback.xml
│   ├── static/            # CSS, JS, images
│   └── templates/         # Thymeleaf HTML templates
│       └── fragments/     # Reusable template fragments
├── archive/               # DICOM file storage (created at runtime)
├── database/              # H2 database files (created at runtime)
├── import/                # Folder import staging area
└── log/                   # Application logs
```

### Data Model
```
PatientEty
  ├── StudyEty
  │     ├── SeriesEty
  │     │     └── InstanceEty
```

Each entity stores relevant DICOM attributes (UIDs, dates, descriptions, etc.)

---

## 📦 Prerequisites

- **Java Development Kit (JDK) 17** or higher
- **Gradle** (wrapper included, no separate installation needed)
- **Network ports**:
  - Web interface: `8080` (configurable)
  - DICOM services: `8104` (configurable)

---

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd mupacs
```

### 2. Build the Application
```bash
./gradlew build
```

### 3. Run the Application
```bash
./gradlew bootRun
```

Or run the JAR directly:
```bash
java -jar build/libs/mupacs-0.0.1-SNAPSHOT.jar
```

### 4. Access the Web Interface
Open your browser and navigate to:
```
http://localhost:8080
```

---

## ⚙️ Configuration

### Application Properties
Edit `src/main/resources/application.properties`:

```properties
# Server Port (default: 8080)
server.port=8080

# DICOM Service Configuration
mupacs.dicom.ae-title=MUPACS
mupacs.dicom.scp.host=0.0.0.0
mupacs.dicom.scp.port=8104

# DICOM Client Timeouts
mupacs.dicom.scu.connect-timeout=5000
mupacs.dicom.scu.response-timeout=10000

# Remote AET Configuration (semicolon-separated)
# Format: aet@host:port;aet@host:port
mupacs.dicom.aet=AET@127.0.0.1:104

# Database Location
spring.datasource.url=jdbc:h2:file:./database/mupacs;MODE=MSSQLServer;AUTO_SERVER=TRUE

# Archive Storage Path
mupacs.archive=./archive

# Import Directory
mupacs.cstore.scp.import=./import

# REST API Base Path
spring.data.rest.basePath=/api

# Logging (optional, defaults to console)
logging.level.de.famst=INFO
logging.file.name=./log/mupacs.log
```

### Important Ports
- **HTTP Web Interface**: `8080` (configurable via `server.port`)
- **DICOM Services**: `8104` (configurable via `mupacs.dicom.scp.port`)

### DICOM Configuration
- **AE Title**: Application Entity Title for this PACS (default: `MUPACS`)
- **SCP Host**: Network interface to bind to (`0.0.0.0` = all interfaces)
- **SCP Port**: DICOM service listening port (default: `8104`)
- **Remote AETs**: Configure known DICOM nodes via web interface or properties file

---

## 📖 Usage

### Receiving DICOM Images

#### From DICOM Modality
Configure the modality to send images to:
- **AE Title**: `MUPACS` (or your configured AE Title)
- **Host**: Your server's IP address
- **Port**: `8104` (or your configured port)

The system will automatically:
1. Receive the DICOM images (C-STORE)
2. Extract metadata and store in database
3. Save files to `archive/StudyInstanceUID/SeriesInstanceUID/SOPInstanceUID.dcm`

#### From Folder
1. Navigate to **Import** page in web interface
2. Enter the folder path containing DICOM files
3. Click "Start Import"
4. Monitor import progress

### Browsing Images

#### Patient List
The Patient List page provides a comprehensive hierarchical view:
- **Left Panel**: Paginated list of all patients
  - Click on any patient to view details
  - Shows patient name, ID, birth date, sex, and study count
- **Right Panel**: Detailed patient information
  - Demographics (name, ID, birth date, age, sex, height, weight, etc.)
  - Medical information (alerts, allergies, pregnancy status, comments)
  - Studies with expandable series information
  - Each series shows:
    - Modality, series number, description
    - Instance count
    - Detailed series attributes (body part, protocol, performing physician, etc.)
    - Complete instance list

The hierarchical display eliminates the need to navigate between separate pages - all information is accessible from the patient list.


### Managing Remote AETs

1. Navigate to **DICOM Config** page
2. View current DICOM service settings
3. Add/Edit/Delete remote Application Entities
4. Test connectivity with C-ECHO button

### Viewing Logs

Navigate to **Logs** page to:
- View most recent log entries
- Auto-refresh every 5 seconds
- Monitor system activity and errors

---

## 🔧 DICOM Services

### C-STORE SCP (Storage Service)
Automatically accepts and stores DICOM images from any configured SCU.

**Implementation**: `DcmStoreSCP.java`

### C-ECHO SCP/SCU (Verification Service)
Tests DICOM connectivity.

**Implementation**: `DcmEchoSCP.java`, `DcmClient.java`

**Usage via Web UI**: Click "C-ECHO" button in AET table

### C-FIND SCP (Query Service)
Supports DICOM queries at:
- **Patient Level**: Query by Patient ID, Name
- **Study Level**: Query by Study UID, Date, Description
- **Series Level**: Query by Series UID, Modality

**Implementation**: `DcmFindSCP.java`

---

## 🌐 Web Interface

### Available Pages

| Page | URL | Description |
|------|-----|-------------|
| Home | `/` | Landing page with navigation and system overview |
| Patient List | `/patientlist` | Browse all patients with hierarchical study/series/instance view |
| Import | `/importlist` | Trigger folder imports |
| DICOM Config | `/dicomconfig` | Configure DICOM services and AETs |
| Logs | `/logs` | View application logs with auto-refresh |

### Common UI Elements
- **Navigation Menu**: Present on all pages for easy navigation
- **Footer**: Consistent branding and information
- **Responsive Design**: Works on desktop and mobile browsers

---

## 🛠️ Development

### Project Setup in IDE
1. Import as Gradle project
2. Enable annotation processing
3. Set Java SDK to 17

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "de.famst.data.RepositoryTest"

# Generate test report
./gradlew test
# Open: build/reports/tests/test/index.html
```

### Code Structure

#### Controllers
- Handle HTTP requests
- Return Thymeleaf views or REST responses
- Located in `de.famst.controller`

#### Services
- Business logic layer
- DICOM operations, imports, AET management
- Located in `de.famst.service` and `de.famst.dcm`

#### Repositories
- JPA data access layer
- Spring Data JPA repositories
- Located in `de.famst.data`

#### Entities
- JPA entity classes mapping to database tables
- Follow DICOM hierarchy
- Located in `de.famst.data`

### Adding New Features

#### Add New DICOM Service
1. Create SCP class implementing `DicomService`
2. Register in `DcmServiceRegistry`
3. Update configuration if needed

#### Add New Web Page
1. Create controller method with `@GetMapping`
2. Create Thymeleaf template in `templates/`
3. Add navigation link in `menu.html` fragment

---

## 🧪 Testing

### Test Coverage
- **Unit Tests**: Controllers, services, utilities
- **Integration Tests**: Repository layer with in-memory database
- **DICOM Tests**: C-STORE simulation, service initialization

### Test Categories

#### Repository Tests (`RepositoryTest.java`)
- CRUD operations for all entities
- Custom query methods
- Transaction management

#### Controller Tests (`*ControllerTest.java`)
- HTTP request/response handling
- View rendering
- Model attribute validation

#### DICOM Tests (`DcmStoreSCPTest.java`)
- Service initialization
- C-STORE association handling
- File storage verification

#### Constraint Tests (`UniqueConstraintsTest.java`)
- Unique UID enforcement
- Data integrity validation

### Running Specific Test Suites
```bash
# Data layer tests
./gradlew test --tests "de.famst.data.*"

# Controller tests
./gradlew test --tests "de.famst.controller.*"

# DICOM tests
./gradlew test --tests "de.famst.dcm.*"
```

---

## 🐛 Known Issues

### Spring Boot 4.0.0
This project currently uses Spring Boot 4.0.0, which is an early-access/milestone version. For production use, consider downgrading to Spring Boot 3.3.x for better stability and complete test support.

To downgrade, modify `build.gradle.kts`:
```kotlin
id("org.springframework.boot") version "3.3.6"
```

---

## 🐳 Docker Deployment

MuPACS can be deployed using Docker for easy containerization and deployment.

### Quick Start with Docker Compose
```bash
# Build the application
./gradlew clean build

# Start with Docker Compose
docker-compose up -d
```

### Manual Docker Build
```bash
# Build the JAR
./gradlew clean build

# Build Docker image
docker build -t famst/micropacs:0.0.1-SNAPSHOT .

# Run container
docker run -d \
  --name mupacs \
  -p 8080:8080 \
  -p 8104:8104 \
  -v mupacs-data:/app/data \
  -v mupacs-archive:/app/archive \
  famst/micropacs:0.0.1-SNAPSHOT
```

### Exposed Ports
- **8080**: HTTP web interface
- **8104**: DICOM C-STORE SCP

📖 **See [DOCKER.md](DOCKER.md) for complete Docker deployment guide**

---

## 🔧 Troubleshooting

### Common Issues

#### SSL/TLS Error: "Invalid character found in method name"
This error occurs when accessing the server with `https://` instead of `http://`.

**Solution**: Always use `http://localhost:8080` (not `https://`)

#### DICOM Connection Issues
- Verify firewall allows port 8104
- Check DICOM client AE Title configuration
- Use C-ECHO to test connectivity

#### Database Issues
- Check `database/` directory permissions
- Ensure H2 database files are not corrupted
- Review logs for SQL errors

#### Import Issues
- Verify the import folder path is accessible
- Check DICOM file validity
- Monitor logs for import errors

📖 **See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for detailed troubleshooting guide**

---

## 🔮 Roadmap

- [ ] C-MOVE SCP/SCU implementation
- [ ] WADO (Web Access to DICOM Objects) support
- [ ] Image thumbnail generation
- [ ] DICOM viewer integration (Weasis, OHIF)
- [ ] Advanced query filters
- [ ] Export functionality (DICOM, JPEG, PNG)
- [ ] User authentication and access control
- [ ] Audit logging
- [ ] HTTPS support
- [ ] Multi-tenant support

---

## 📚 Additional Documentation

- [Docker Deployment Guide](DOCKER.md) - Complete guide for Docker deployment
- [Troubleshooting Guide](TROUBLESHOOTING.md) - Detailed troubleshooting steps
- [Property Dump Feature](Property_Dump_Feature.md) - Configuration verification on startup
- [Protocol Mismatch Detection](Protocol_Mismatch_Detection_Fixed.md) - SSL/TLS error handling

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

### Development Guidelines
- Follow existing code style
- Add tests for new features
- Update documentation
- Ensure all tests pass before submitting

---

## 📄 License

This project is licensed under the terms specified in the repository.

---

**MuPACS** - Simple, efficient, and reliable DICOM archiving.

