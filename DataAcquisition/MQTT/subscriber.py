# -*- coding: utf-8 -*-
#
# MQTT test subscriber
#
# authors:  Philipp Huebner
# tags:     py35, mqtt, paho

"""
MQTT test - subscriber file
"""

import paho.mqtt.client as mqtt
import ssl

__version__ = '0.3.0'  # version string
__doc__ += 'Version: {}'.format(__version__)  # add version to __doc__


class subscriber(object):
    """
    Subscriber
    """
    @staticmethod
    def on_connect(client, userdata, flags, rc):
        print('Subscriber connected to port {} via {} with result code {}'
              .format(client._port, client._transport, str(rc)))

    @staticmethod
    def on_subscribe(client, userdata, mid, granted_qos):
        print('Subscribed: {} {}'.format(str(mid), str(granted_qos)))

    @staticmethod
    def on_message(client, userdata, msg):
        print('{} {}'.format(msg.topic, str(msg.payload.decode('utf-8'))))

    @staticmethod
    def on_disconnect(client, userdata, rc):
        if rc != 0:
            print('Unexpected disconnection of subscriber.')
        else:
            print('Subscriber disconnected.')

    def __init__(self, config=None):
        """
        :param config: Configuration dictionary
        :type config: dict
        :return: None
        """
        config = config if config is not None else dict()

        self.server = config['server'] if 'server' in config else 'test.mosquitto.org'
        self.topic = config['topic'] if 'topic' in config else 'test/mqtt/a'
        self.transport = config['transport'] if 'transport' in config else 'tcp'
        self.port = config['port'] if 'port' in config else 1883
        self.client = mqtt.Client(transport=self.transport)
        self.config = config

        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.client.on_subscribe = self.on_subscribe
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
        self.client.subscribe('{}/#'.format(self.topic))

        self.client.loop_forever()

if __name__ == '__main__':
    with subscriber() as subs:
        subs.start()
