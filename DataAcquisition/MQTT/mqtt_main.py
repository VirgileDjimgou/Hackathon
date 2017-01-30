# -*- coding: utf-8 -*-
#
# MQTT test script
#
# authors:  Philipp Huebner
# tags:     py35, mqtt, paho

"""
MQTT test - main file
"""

# global imports
from threading import Thread
import time

# local imports
from helpers.config_file import read_config_file
from sensor import start_sensor
from subscriber import subscriber

__version__ = '0.3.0'  # version string
__doc__ += 'Version: {}'.format(__version__)  # add version to __doc__

# TODO: proxy? socks?


def main():
    """Main routine"""
    # Read config.yml - write default config.yml if no config file is found
    cfg = read_config_file('config.yml', dict(server='test.mosquitto.org',
                                              topic='test/mqtt/a',
                                              transport='tcp',  # 'websockets'
                                              port=1883,  # 8080
                                              ssl={'use_ssl': False}))

    # Start sensors
    for i in range(3):
        Thread(target=start_sensor, kwargs={'config': cfg}).start()
        time.sleep(0.21)

    # Start subscriber
    with subscriber(config=cfg) as subs:
        subs.start()

if __name__ == '__main__':
    main()
