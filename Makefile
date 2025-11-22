.PHONY: help build up down logs clean restart

help:
    @echo "FruitShop Docker Commands:"
    @echo "  make build    - Build all containers"
    @echo "  make up       - Start all services"
    @echo "  make down     - Stop all services"
    @echo "  make logs     - View logs"
    @echo "  make clean    - Remove all containers and volumes"
    @echo "  make restart  - Restart all services"

build:
    docker-compose --env-file .env.docker build

up:
    docker-compose --env-file .env.docker up -d

down:
    docker-compose --env-file .env.docker down

logs:
    docker-compose --env-file .env.docker logs -f

clean:
    docker-compose --env-file .env.docker down -v
    docker system prune -f

restart:
    make down
    make build
    make up