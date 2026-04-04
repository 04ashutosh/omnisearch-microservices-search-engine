# 🌐 OmniSearch v2.1 — Distributed Search Engine

> OmniSearch is a high-performance distributed search engine designed to simulate real-world search infrastructure at scale. It demonstrates how modern systems handle web crawling, event streaming, indexing, and low-latency retrieval using a microservices-based architecture.
>
> Built with **Java**, **Spring Boot**, **Kafka**, **Elasticsearch**, and **Redis**, OmniSearch emphasizes scalability, resilience, and observability.

---

## 🏗️ Architecture Overview

OmniSearch is composed of loosely coupled microservices connected through an event-driven pipeline.

```
Client → API Gateway → Search Service → Redis / Elasticsearch
                      ↑
               Indexer Service ← Kafka ← Crawl Service
```

---

### 🌐 1. API Gateway — Entry & Protection Layer

| Property | Detail |
|---|---|
| **Port** | `8765` |
| **Tech** | Spring Cloud Gateway (WebFlux), Spring Security, JWT |

**Responsibilities:**
- Central entry point for all requests
- Stateless authentication using JWT
- Redis-backed rate limiting (5 req/sec)
- Routing requests to downstream services

---

### 🕷️ 2. Crawl Service — Discovery Engine

| Property | Detail |
|---|---|
| **Port** | `8081` |
| **Tech** | Jsoup, Java 24, Virtual Threads (Project Loom) |

**Responsibilities:**
- Recursively crawls web pages starting from seed URLs
- Extracts content and hyperlinks
- Uses lightweight virtual threads for massive concurrency

**Output:** Publishes `PageCrawledEvent` to Kafka

---

### ⚙️ 3. Indexer Service — Processing Layer

| Property | Detail |
|---|---|
| **Port** | `8083` |
| **Tech** | Spring Kafka, Spring Data Elasticsearch |

**Responsibilities:**
- Consumes crawl events from Kafka
- Cleans and transforms content
- Indexes documents into Elasticsearch

**Reliability Features:**
- Dead Letter Queue (DLQ)
- Retry with exponential backoff
- Fault isolation

---

### 🔍 4. Search Service — Retrieval Engine

| Property | Detail |
|---|---|
| **Port** | `8082` |
| **Tech** | Spring Data Elasticsearch, Redis |

**Responsibilities:**
- Handles full-text search queries
- Uses BM25 scoring for relevance ranking
- Implements Redis caching for hot queries

**Performance:** Sub-5ms response time for cached queries

---

## ⚡ Key System Features

| Feature | Description |
|---|---|
| 🚀 **High Throughput Processing** | Event-driven architecture using Kafka; parallel crawl + index pipeline |
| 🧵 **Massive Concurrency** | Virtual Threads enable thousands of concurrent crawl operations |
| ⚡ **Low Latency Search** | Redis caching for frequently searched queries; optimized Elasticsearch queries |
| 🛡️ **Security & Protection** | Stateless JWT authentication; gateway-level rate limiting; service isolation |
| 🔄 **Fault Tolerance** | DLQ for failed messages; retry strategies; loose coupling between services |
| 📊 **Observability** | Distributed tracing with Zipkin; metrics via Micrometer |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 24, Spring Boot 3 |
| Frontend | React 18, Vite |
| Messaging | Apache Kafka |
| Search Engine | Elasticsearch |
| Cache | Redis |
| Security | Spring Security, JWT |
| Observability | Zipkin, Micrometer |
| Infra | Docker Compose |

---

## 🚀 Getting Started

### 1. Prerequisites

- Docker Desktop
- Java 24
- Node.js & npm

### 2. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- Kafka
- Zookeeper
- Elasticsearch
- Redis
- Zipkin

### 3. Run Microservices

Start services in order:

1. API Gateway
2. Crawl Service
3. Indexer Service
4. Search Service

### 4. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Access: [http://localhost:3000](http://localhost:3000)

---

## 🔐 Authentication Flow

**Login via Gateway:**
```http
POST /login?username=admin&password=admin123
```

**Use token:**
```http
Authorization: Bearer <JWT_TOKEN>
```

---

## 🔎 End-to-End Flow

### 🔁 Crawling Pipeline

```
Crawl Service fetches web pages
        ↓
Emits events to Kafka
        ↓
Indexer consumes events
        ↓
Data indexed into Elasticsearch
```

### 🔍 Search Pipeline

```
Client sends query to Gateway
        ↓
Gateway validates JWT + rate limits
        ↓
Search Service checks Redis cache
        ↓
On cache miss → query Elasticsearch
        ↓
Results returned with relevance ranking
```

### 📊 Observability

Monitor the full request lifecycle at [http://localhost:9411](http://localhost:9411)

- Trace requests across services
- Debug latency bottlenecks
- Analyze service dependencies

---

## 🧪 Design Decisions

### Why Kafka?
- Decouples crawling and indexing
- Enables horizontal scalability
- Provides durability and replay capability

### Why Elasticsearch?
- Full-text search with BM25 scoring
- Fast inverted index lookups
- Built for distributed querying

### Why Redis?
- Ultra-fast caching layer
- Reduces load on Elasticsearch
- Enables rate limiting at Gateway

### Why Virtual Threads?
- Lightweight concurrency model
- Avoids thread exhaustion
- Ideal for IO-heavy crawling tasks