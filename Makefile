# Portable run surface — the standard entrypoint (loop.md baseline; springboot.md §2 / angular.md §2).
# A project built to these conventions runs on a clean machine (Docker the only prerequisite) via these
# targets. This is the Spring-Boot / Maven default binding: swap `./mvnw` → `./gradlew` for Gradle, and
# a frontend stack binds the same targets to its native idiom (ng serve / build / test). → ./Makefile
.PHONY: run test build down dev

run:   ## Clean-machine entrypoint: build + start the app and its backing services (real engine)
	docker compose up --build

test:  ## Fast suite — NO Docker (unit + web slices + in-memory H2); §10 bulk tier
	./mvnw -B -ntp verify

build: ## Build the deployable image
	docker compose build

down:  ## Stop and remove the stack + volumes
	docker compose down -v

dev:   ## Native inner-loop for fast reload: datastore in Docker, app on the host toolchain
	docker compose up -d db
	./mvnw spring-boot:run
