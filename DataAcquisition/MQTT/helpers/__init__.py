# -*- coding: utf-8 -*-
#
# generic python package
#
# authors:    Philipp Huebner
# tags:       py35

from os.path import dirname, basename, isfile
import glob

# add all .py files to __all__ and skip files starting with _
modules = glob.glob(dirname(__file__)+"/*.py")
__all__ = [basename(f)[:-3] for f in modules if isfile(f) and not basename(f).startswith('_')]
del modules
