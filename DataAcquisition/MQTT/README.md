# MQTT test repository

Based on https://codebase.zeiss.org/IoT/MQTT by zophue

## Testing
Test with browser application (websockets) at http://mitsuruog.github.io/what-mqtt/

Working example `config.yml` file (direct internet access necessary, not working via proxy):

```
server: test.mosquitto.org
port: 1883
topic: test/mqtt/a
transport: tcp
ssl:
  use_ssl: false
```

## Issues
- Proxy
 - Socks proxy necessary for MQTT http://stackoverflow.com/a/27839095/5276734 (WebSockets?)
 - "MQTT and HTTP Proxies seem to be one of the hardest challenges nowadays in corporate networks" https://groups.google.com/forum/#!topic/mqtt/LDdrV5k2nek

## Documentation
- paho MQTT Python client https://eclipse.org/paho/clients/python/docs/
- MQTT https://en.wikipedia.org/wiki/MQTT
- Websockets proxy traversal https://en.wikipedia.org/wiki/WebSocket#Proxy_traversal

Sources:
- https://www.dinotools.de/2015/04/12/mqtt-mit-python-nutzen
- https://www.heise.de/developer/artikel/Kommunikation-ueber-MQTT-3238975.html

## AWS IoT Suite
Follow the instructions from https://github.com/mariocannistra/python-paho-mqtt-for-aws-iot

Read http://docs.aws.amazon.com/iot/latest/developerguide/protocols.html for MQTT protocols and ports at AWS

Working example `config.yml` file (direct internet access necessary, not working via proxy):
```
server: 'data.iot.eu-central-1.amazonaws.com'
topic: 'test/mqtt/a'
transport: 'tcp'
port: 8883
ssl:
 use_ssl: True
 caPath: 'lib/aws-iot-rootCA.crt'
 certPath: 'lib/certificate.pem'
 keyPath: 'lib/private.pem'
```

## Python dependencies
The file [environment.yml](environment.yml) contains all necessary packages for the Python code in this repository.

Use `$ conda env create --file environment.yml` to create a new conda environment for this repository or update your current root environment with `$ conda env update --file environment.yml --name root`.
