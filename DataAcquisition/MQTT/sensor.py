# -*- coding: utf-8 -*-
#
# MQTT test sensor
#
# authors:  Philipp Huebner
# tags:     py35, mqtt, paho

"""
MQTT test - sensor file
"""

import time
import random
import json
import paho.mqtt.client as mqtt
import ssl

__version__ = '0.3.0'  # version string
__doc__ += 'Version: {}'.format(__version__)  # add version to __doc__


class sensor(object):
    """
    Sensor with random ID, sending messages with timestamps
    """

    @staticmethod
    def on_connect(client, userdata, flags, rc):
        print('Sensor {:02d} connected to port {} via {} with result code {}'
              .format(userdata['sensor_id'], client._port, client._transport, str(rc)))

    @staticmethod
    def on_publish(client, userdata, mid):
        print('Client {} published message with id = {}'.format(client._client_id, str(mid)))

    @staticmethod
    def on_disconnect(client, userdata, rc):
        if rc != 0:
            print('Unexpected disconnection of {}.'.format(client._client_id))
        else:
            print('Client {} disconnected.'.format(client._client_id))

    def __init__(self, config=None):
        """
        :param config: Configuration dictionary
        :type config: dict
        :return: None
        """
        config = config if config is not None else dict()
        self._sensor_id = random.randint(1, 42)

        self.server = config['server'] if 'server' in config else 'test.mosquitto.org'
        self.topic = config['topic'] if 'topic' in config else 'test/mqtt/a'
        self.transport = config['transport'] if 'transport' in config else 'tcp'
        self.port = config['port'] if 'port' in config else 1883

        self.client = mqtt.Client(client_id='sensor_{:02d}'.format(self._sensor_id),
                                  userdata={'sensor_id': self._sensor_id}, transport=self.transport)
        self.config = config

        self.client.on_connect = self.on_connect
        self.client.on_publish = self.on_publish
        self.client.on_disconnect = self.on_disconnect

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.client.disconnect()

    def start(self):
        if self.config['ssl']['use_ssl']:
            self.client.tls_set(ca_certs=self.config['ssl']['caPath'],
                                certfile=self.config['ssl']['certPath'],
                                keyfile=self.config['ssl']['keyPath'],
                                cert_reqs=ssl.CERT_REQUIRED,
                                tls_version=ssl.PROTOCOL_TLSv1_2,
                                ciphers=None)

        self.client.connect(host=self.server, port=self.port, keepalive=60)

        self.client.loop_start()

        while True:
            time.sleep(random.random()*1.5 + 0.5)
            msg = {"time": format(time.strftime('%Y-%m-%d %H:%M:%S')), "temp": random.randint(-4, 42)}
            self.client.publish('{}/{:02d}/temperature'.format(self.topic, self._sensor_id), json.dumps(msg))


def start_sensor(config=None):
    """
    Helper function for threading
    :param config: Configuration dictionary
    :type config: dict
    :return: None
    """
    with sensor(config=config) as sens:
        sens.start()

if __name__ == '__main__':
    with sensor() as sens:
        sens.start()
