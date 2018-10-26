# closure-api

A Closure Ring server that acts as a transparent Google Closure compiler.

You can access the Docker image of this repository [here]().

## Usage

Set your nginx like this:

```
upstream backend-closure-api { server closure-api:7070; }

server {
      ...
  
      location ~ ^/(.*\.js)$ {
        proxy_pass http://backend-closure-api;
        proxy_redirect     off;
        proxy_set_header    X-Forwarded-Host $host;
        proxy_set_header    X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header    X-Forwarded-Proto $scheme;
        proxy_set_header    X-Real-IP       $remote_addr;
    } 
}
```

This will transparently compile and serve JS files.
The API has a builtin SQlite cache.

## Configuration

`closure-api` supports configuration via env variables:

- `DEFAULT_HOST`: This is the underlying service with the JS files.
- `DEFAULT_PASSTHRU`: You can turn off the compilation by setting this variable.
  Also, compilation can be turned off then you add `_passthru=1` to the query
  string arguments.

## TODO
- periodic cache expiration
- compiling static JS files on the fly (`DEFAULT_HOST` in format of `file://...`)

## License

Distributed under the GNU General Public License either version 3.0.
