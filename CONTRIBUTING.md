# Contributing to incore-services

Start with the [IN-CORE developer guide](https://github.com/IN-CORE/.github/blob/main/DEVELOPING.md) (dev access, CORS, PR rules).

| Service | Context path | Mongo DB (default) |
|---------|--------------|--------------------|
| data | `/data` | `mongodb://localhost:27017/datadb` |
| dfr3 | `/dfr3` | `.../dfr3db` |
| hazard | `/hazard` | `.../hazarddb` |
| space | `/space` | `.../spacedb` |
| semantics | `/semantics` | `.../semanticsdb` |
| project | `/project` | `.../projectdb` |

Shared code: `server/incore-common`, `server/tools-common`. API docs: https://tools.in-core.org/doc/api/

## Prerequisites

- JDK 11+ (`sourceCompatibility = 11`)
- MongoDB on `localhost:27017` (defaults work, no env needed)
- IntelliJ IDEA (Community is fine)

```bash
docker run -d --name incore-mongo -p 27017:27017 mongo:6
```

## Run

```bash
cd server
export DATA_REPO_DATA_DIR=$HOME/incore-data                     # dataset file storage (data, hazard)
export SERVICES_URL=http://localhost:8080                       # hazard → data calls
export GEOSERVER_ENABLE=false
export SERVICES_ALLOW_ORIGIN=http://localhost:3000              # CORS for local web clients
export SERVICES_ALLOW_METHODS=GET,POST,DELETE,PUT,PATCH,OPTIONS
export SERVICES_ALLOW_HEADERS=authorization,content-type,accept
./gradlew farmRun                                               # all services on :8080
```

Check: `curl http://localhost:8080/data/api/datasets -H 'x-auth-userinfo: {"preferred_username":"incrtest"}'`

Single service: `./gradlew :hazard-service:appRun` · Tests: `./gradlew test` or `./gradlew :dfr3-service:test`

> Always set `SERVICES_ALLOW_ORIGIN`. If unset, any browser request (it sends an `Origin` header) fails with a server error.

## IntelliJ

1. **Open** `server/` (folder with `settings.gradle`) → trust Gradle project.
2. *Settings → Build Tools → Gradle*: Gradle JVM = JDK 11+.
3. Run config: *Gradle* → task `farmRun`, add the env vars above.
4. Debug: run `farmDebug` (or `appDebug` per service), then *Remote JVM Debug* attach to port `5005`.

## Auth when running locally

Local services have no Keycloak in front. They trust identity headers:

```
x-auth-userinfo:  {"preferred_username": "incrtest"}
x-auth-usergroup: {"groups": ["incore_ncsa"]}
```

pyincore / scripts:

```python
IncoreClient(service_url="http://localhost:8080", local=True,
             username="incrtest", usergroups=["incore_ncsa"])
```

### Web clients (incore-ui, Studio, Playbook) against local services

Web clients log in via Keycloak at `<INCORE_REMOTE_HOSTNAME>/auth` and send a Bearer token, so you need a proxy that serves `/auth` and turns the token into `x-auth-*` headers via [incore-auth](https://github.com/IN-CORE/incore-auth) (same as deployed envs):

| Piece | Setting |
|-------|---------|
| Proxy (e.g. Traefik) on `:8000` | `/auth` → Keycloak; `/data`, `/dfr3`, `/hazard`, `/space`, `/semantics`, `/project` → `localhost:8080` with forward-auth |
| incore-auth | Forward-auth target; returns `x-auth-userinfo`, `x-auth-usergroup` (see `templates/middleware.yaml` in [incore-helm](https://github.com/IN-CORE/incore-helm)) |
| Web client | `INCORE_REMOTE_HOSTNAME=http://localhost:8000` |

Pick a Keycloak:

| Option | Keycloak | incore-auth `KEYCLOAK_URL` | Notes |
|--------|----------|----------------------------|-------|
| a. Local | Container with realm imported from `keycloak-incore-realm.json` (incore-helm) | `http://localhost:<kc-port>/auth/realms/In-core/` | Fully offline; create your own test users |
| b. Dev | Proxy `/auth` → `https://dev.in-core.org/auth` | `https://dev.in-core.org/auth/realms/In-core/` | Needs dev access; your dev account logs in |

## Seed data

Local Mongo starts empty. For starter datasets, fragility curves, mappings and hazards, talk to the IN-CORE devs: [Slack](https://in-core.slack.com) `#developer` or incore-dev@lists.illinois.edu.

## Other env vars (only if needed)

| Var | Used by | Purpose |
|-----|---------|---------|
| `<SVC>_MONGODB_URI` | each service | Override Mongo URI (`DATA_`, `DFR3_`, `HAZARD_`, `SPACE_`, `SEMANTICS_`, `PROJECT_`) |
| `COMMON_MONGODB_URI` | dfr3, hazard | Shared `commondb` |
| `DATA_SERVICE_URL`, `HAZARD_SERVICE_URL`, `SEMANTICS_SERVICE_URL` | data, space, project | Cross-service base URLs |
| `GEOSERVER_URL/_USER/_PW/_WORKSPACE` | data | When `GEOSERVER_ENABLE=true` (needed for map layers in web clients) |
| `DATAWOLF_URL`, `DATAWOLF_USER` | hazard, project | Workflow execution (Studio) |
| `PG_HOST/PORT/USER/PASSWORD/DATABASE` | data | Postgres-backed features |

## Docker

```bash
./docker.sh     # builds incore/{data,dfr3,hazard,space,semantics}-jetty
```

Deployment: Helm charts in [incore-helm](https://github.com/IN-CORE/incore-helm).

## Pull requests

Branch from `develop`, PR to `develop`, link the issue, update `CHANGELOG.md`, `./gradlew test` passes.
