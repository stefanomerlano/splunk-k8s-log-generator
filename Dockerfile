FROM ubuntu:22.04

# Avoid interactive installation prompts
ENV DEBIAN_FRONTEND=noninteractive

# Install Java JDK, wget, and necessary system utilities
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    wget \
    curl \
    tar \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Download and install Splunk Universal Forwarder
ARG SPLUNK_UF_VERSION=10.4.2
ARG SPLUNK_UF_BUILD=33c3bf42cd73
RUN ARCH="$(uname -m)" && \
    if [ "$ARCH" = "aarch64" ] || [ "$ARCH" = "arm64" ]; then \
        UF_ARCH="arm64"; \
    else \
        UF_ARCH="amd64"; \
    fi && \
    wget -O /tmp/splunkforwarder.tgz "https://download.splunk.com/products/universalforwarder/releases/${SPLUNK_UF_VERSION}/linux/splunkforwarder-${SPLUNK_UF_VERSION}-${SPLUNK_UF_BUILD}-linux-${UF_ARCH}.tgz" \
    && tar -xzf /tmp/splunkforwarder.tgz -C /opt \
    && rm /tmp/splunkforwarder.tgz

# Set working directory
WORKDIR /app

# Copy application files into container
COPY LogGenerator.java /app/
COPY start.sh /app/

# Compile Java code and set permissions
RUN javac LogGenerator.java \
    && chmod +x /app/start.sh

# Set startup script as default command
CMD ["/app/start.sh"]
