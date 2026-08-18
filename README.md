# Fintech - Audit Log Generator & Splunk Monitoring Showcase

This repository contains an end-to-end containerized audit log generation and Splunk monitoring demonstration system. It features a continuous Java log generator microservice coupled with a Splunk Universal Forwarder sidecar/embedded process, Kubernetes deployment manifests, and a ready-to-import Splunk Dashboard Studio specification.

---

## 🏗️ Architecture & Features

### 1. Java Log Generator (`LogGenerator.java`)

- Generates synthetic audit logs written to `/var/log/myapp/app.log`.
- Produces structured key-value log entries containing fields such as:
  - `created`: ISO offset timestamp of the event.
  - `import_time`: Ingestion/logging timestamp.
  - `host`, `id`: Transaction/Session identifier (simulates occasional duplicate IDs for anomaly tracking).
  - `user`: Simulated user accounts (`admin`, `johndoe`, `alice`, `bob`, `eva`).
  - `operation`: System operations (`LOGIN`, `LOGOUT`, `PURCHASE`, `UPDATE_PROFILE`, `PASSWORD_RESET`).
  - `text`, `details`: Operation context and status codes (`status=success code=200`).
- Configurable log interval via the `LOG_INTERVAL_MS` environment variable (default: `10ms`).

### 2. Containerization (`Dockerfile` & `start.sh`)

- Multi-architecture support (`amd64` and `arm64`) based on Ubuntu 22.04 and OpenJDK 17.
- Embeds **Splunk Universal Forwarder 10.4.2**.
- Startup orchestrator (`start.sh`) initializes the forwarder, attaches a monitor to `/var/log/myapp/app.log`, connects to the Splunk receiver on port `9997`, and runs the Java application.

### 3. Kubernetes Deployment (`*.yaml`)

- **`splunk-k8s.yaml`**: Deploys Splunk Enterprise in the `splunk` namespace with persistent storage (`PersistentVolumeClaim`), readiness/liveness probes, and a `LoadBalancer` service exposing:
  - Port `8000`: Splunk Web UI.
  - Port `9997`: Splunk Receiver Port.
- **`loggenerator-k8s.yaml`**: Deploys the `loggenerator` application in the `apps` namespace.

### 4. Splunk Dashboard Studio (`splunk-dashboard.yaml`)

Pre-configured dashboard titled **"Fintech - Audit Log"** featuring 3 tabs:

1. **Events**: Single-value total event counter and detailed event breakdown table.
2. **Daily Summary**: Daily event volume column chart (`timechart span=1d`).
3. **Duplicate IDs**: Anomaly tracking view displaying total duplicate ID count and an analysis table highlighting duplicate occurrences, involved users, operations, and first/last seen timestamps.

---

## 📁 Repository Structure

```
.
├── Dockerfile              # Docker build file for Log Generator + Splunk Universal Forwarder
├── LogGenerator.java       # Core Java audit log generator application
├── start.sh                # Container entrypoint script initializing Splunk UF & app
├── loggenerator-k8s.yaml   # Kubernetes deployment for the Log Generator
├── splunk-k8s.yaml         # Kubernetes manifest for Splunk Enterprise (Namespace, PVC, Deployment, Service)
├── splunk-dashboard.yaml   # Splunk Dashboard Studio JSON definition
└── README.md               # Project documentation
```

---

## 🚀 Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & [Docker Desktop](https://www.docker.com/products/docker-desktop/) or `nerdctl`/`podman`
- [Kubernetes Cluster](https://kubernetes.io/) (minikube, k3s, kind, or EKS/GKE/AKS)
- `kubectl` CLI tool configured

---

## 🐳 Building and Running Locally with Docker

1. **Build the Docker Image**:

   ```bash
   docker build -t log-generator:latest .
   ```

2. **Run Splunk Enterprise (if not running on Kubernetes)**:

   ```bash
   docker run -d --name splunk \
     -p 8000:8000 -p 9997:9997 \
     -e "SPLUNK_START_ARGS=--accept-license" \
     -e "SPLUNK_PASSWORD=SplunkAdmin123!" \
     splunk/splunk:latest
   ```

3. **Run the Log Generator Container**:
   ```bash
   docker run -d --name log-generator \
     -e SPLUNK_HOST="<SPLUNK_SERVER_IP>" \
     -e SPLUNK_PORT="9997" \
     -e LOG_INTERVAL_MS="100" \
     log-generator:latest
   ```

---

## ☸️ Deploying to Kubernetes

1. **Deploy Splunk Enterprise**:

   ```bash
   kubectl apply -f splunk-k8s.yaml
   ```

   _Verify deployment:_

   ```bash
   kubectl get pods -n splunk -w
   ```

2. **Deploy the Log Generator App**:

   ```bash
   kubectl apply -f loggenerator-k8s.yaml
   ```

3. **Access Splunk Web UI**:
   - URL: `http://<NODE_IP_OR_LOADBALANCER>:8000`
   - Default Username: `admin`
   - Default Password: `SplunkAdmin123!`

---

## 📊 Importing the Splunk Dashboard

1. Log into the Splunk Web UI (`http://localhost:8000`).
2. Navigate to **Dashboards** > **Create New Dashboard**.
3. Select **Dashboard Studio**.
4. Switch to **Source Code** view (`</>`).
5. Copy the JSON content from [`splunk-dashboard.yaml`](splunk-dashboard.yaml) and paste it into the editor.
6. Click **Save** and view the dashboard.

---

## 🔧 Environment Variables

| Variable          | Default Value                             | Description                                                  |
| :---------------- | :---------------------------------------- | :----------------------------------------------------------- |
| `SPLUNK_HOST`     | `splunk-service.splunk.svc.cluster.local` | Hostname or IP of the Splunk receiver                        |
| `SPLUNK_PORT`     | `9997`                                    | Port of the Splunk Universal Forwarder receiver              |
| `LOG_INTERVAL_MS` | `10`                                      | Sleep interval in milliseconds between generated log entries |

---

## 📝 License & Contributing

This project is created for demonstration and showcase purposes. Feel free to modify and extend the manifests or log generation logic for your specific observability requirements.
