# -*- coding: utf-8 -*-
#
# YAML config file
#
# authors:    Philipp Huebner
# tags:       py35, yaml

"""
YAML config file
"""

from __future__ import print_function  # py2/3 compatibility

import os.path
import yaml

__version__ = '1.3.2'  # version string
__doc__ += 'Version: {}'.format(__version__)  # add version to __doc__


def write_yml_file(filename='config.yml', default_dict=None):
    """
    Write dummy yaml config file
    :param filename: Filename of config file (default: 'config.yml')
    :type filename: str
    :param default_dict: Default dictionary to write (optional)
    :type default_dict: dict
    :return: None
    """
    data = default_dict if default_dict else dict(system={'key': 'value'})

    if not os.path.exists(filename):  # create folders if not yet existing
        if os.path.dirname(filename):  # only if a path is included
            os.makedirs(os.path.dirname(filename), exist_ok=True)

    with open(filename, 'w') as outfile:
        outfile.write(yaml.dump(data, default_flow_style=False))


def read_config_file(cfg_file='config.yml', default_dict=None):
    """
    Read data from yaml config file, checks for file existence and writes dummy if nothing found
    :param cfg_file: Filename of config file (default: 'config.yml')
    :type cfg_file: str
    :param default_dict: Default dictionary to write in case nothing is found (optional)
    :type default_dict: dict
    :return: config dict
    :rtype: dict
    """
    if not os.path.isfile(cfg_file):
        write_yml_file(cfg_file, default_dict)
        print('Warning: Config file {} not found, created from template. Edit your file!'.format(cfg_file))
    with open(cfg_file, 'r') as ymlfile:
        cfg = yaml.safe_load(ymlfile)
    if '_filename' not in cfg:
        cfg['_filename'] = cfg_file  # add filename to cfg
    return cfg

if __name__ == '__main__':
    read_config_file('config.yml')
