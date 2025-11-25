# Event-driven pipeline — Stream ingestion (tweets), processing and query architecture

This repository implements a  event-driven architecture that ingests data from an external source (X), publishes events to Apache Kafka, and processes those events with downstream services for search, analytics and query APIs.

> Current status: the X → Kafka ingestion service (x-to-kafka service) is completed and can publish mocked X events into the Kafka cluster.

## Overview

- Ingest streaming data from X (streaming data is mocked for now as X's developer api is paid).
- Publish events to Kafka (the event-store).
- Consume and process events with multiple services:
  - Index into Elasticsearch for search and query APIs.
  - Perform stream processing with Kafka Streams.
  - Aggregate analytics results into PostgreSQL.
- Provide a query API and web client for end users.
- Provide infra concerns such as configuration, discovery, gateway and auth, plus observability (metrics, tracing and logs).

## Logical components

- source-data — X stream (external source)
- x-to-kafka-service (Spring Boot) — ingests X and publishes to Kafka
- event-store — Apache Kafka (topics store event stream)
- kafka-to-elastic-service (Spring Boot consumer) — consumes Kafka topics and indexes documents into Elasticsearch
- query-store — Elasticsearch (searchable documents)
- query-service (Spring Boot) — REST API used by web client to serve queries
- web-client — UI consuming `query-service`
- kafka-streams-service — stream processing (Kafka Streams) for transformations and enrichment
- analytics-service (Spring Boot) — consumes events/aggregates and persists analytic results to DB
- analytics-store — PostgreSQL (analytics/results store)

Infrastructure and cross-cutting services:

- config-server — Spring Cloud Config (centralized config)
- api-gateway — Spring Cloud Gateway (edge routing)
- discovery-service — Netflix Eureka (service discovery)
- authz-server — Keycloak (authentication/authorization)

Observability and monitoring:

- Monitoring & visualization — Spring Boot Actuator + Prometheus + Grafana
- Distributed tracing — Spring Sleuth + Zipkin
- Centralized logging & aggregation — Logback → Logstash → Kibana (ELK)

## Technologies

- Java 25 (project uses Spring Boot & related ecosystem)
- Spring Boot (microservices)
- Spring Cloud (Config, Gateway)
- Netflix Eureka (service discovery)
- Keycloak (identity / authZ)
- Apache Kafka (event store, messaging)
- Kafka Streams (stream processing)
- Elasticsearch (query-store)
- PostgreSQL (analytics store)
- Prometheus + Grafana (metrics & visualization)
- Zipkin / Sleuth (distributed tracing)
- Logstash + Kibana (log aggregation / search)
- Docker / Docker Compose (local infra orchestration)

## Current module status

- `x-to-kafka-service`: Completed — ingests X and publishes events to Kafka topics.
- Remaining work :
  - `kafka-to-elastic-service` — consume and index into Elasticsearch
  - `query-service` + `web-client` — implement search API and UI
  - `kafka-streams-service` — advanced stream processing/topology
  - `analytics-service` → `analytics-store` (Postgres) for aggregated metrics
  - Integrate config-server, discovery-service, api-gateway and keycloak
  - Add Docker Compose / Helm manifests for full local/dev stack and CI/CD
  - Add monitoring dashboards and tracing configuration

## Quick start (local / dev)

1. Start dependencies with Docker Compose (example):

```bash
# from docker-compose/ directory (adjust files as needed)
docker compose up -d
```

2. Build and run the `x-to-kafka-service` locally or use its Docker image:

```bash
# build (skip tests in dev)
cd x-to-kafka-service
mvn clean package -DskipTests
# run locally
java -jar target/*-SNAPSHOT.jar
```

3. Verify messages reach Kafka with `kcat`.


