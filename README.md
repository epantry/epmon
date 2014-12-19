
# epmon

Simple app that reports the depth of a Celery redis queue to Librato Metrics once per minute.

## Running Locally

First set the relevant environmental variables:

```
$ export LIBRATO_TOKEN=<token>
$ export LIBRATO_USER=<user>
$ export REDIS_URI=redis://name:password@server.com:19928
$ export METRIC_NAME=celery_depth
$ export QUEUE_NAME=celery
```

And run:

```
$ git clone https://github.com/epantry/epmon.git
$ cd epmon
$ lein repl
  user=> (require 'epmon.web)
  user=> (def server (epmon.web/-main))
```

Visiting [localhost:5000](http://localhost:5000/) will simply yield a 404 but you can see the queue length logged on the commandline every minute.

## Deploying

This is configured as a Heroku app (see the profile). Simply set the corresponding environmental variables you set locally and deploy:

```
$ heroku config:set LIBRATO_TOKEN=<token>
$ heroku config:set LIBRATO_USER=<user>
$ heroku config:set REDIS_URI=redis://name:password@server.com:19928
$ heroku config:set METRIC_NAME=celery_depth
$ heroku config:set QUEUE_NAME=celery

$ git push heroku master
```
