FROM clojure:lein

COPY CHANGELOG.md /usr/src/app/
COPY LICENSE /usr/src/app/
COPY README.md /usr/src/app/
COPY project.clj /usr/src/app/
COPY src /usr/src/app/src
WORKDIR /usr/src/app
RUN lein deps
CMD ["lein", "run"]
