IMAGE=cgenie/closure-api

all: build push

build: Dockerfile
	docker build --pull -t $(IMAGE) .
	docker tag $(IMAGE) $(IMAGE):latest

push: build
	docker push $(IMAGE)
	docker push $(IMAGE):latest
